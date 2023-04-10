/*
 * GameplayController.java
 *
 * For many of you, this class will seem like the most unusual one in the entire project.
 * It implements a lot of functionality that looks like it should go into the various
 * GameObject subclasses. However, a lot of this functionality involves the creation or
 * destruction of objects.  We cannot do this without a lot of cyclic dependencies,
 * which are bad.
 *
 * You will notice that gameplay-wise, most of the features in this class are
 * interactions, not actions. This demonstrates why a software developer needs to
 * understand the difference between these two.
 *
 * You will definitely need to modify this file in Part 2 of the lab. However, you are
 * free to modify any file you want.  You are also free to add new classes and assets.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.entity.*;

/**
 * Controller to handle gameplay interactions.
 * </summary>
 * <remarks>
 * This controller also acts as the root class for all the models.
 */
public class GameplayController {
	int curframe;
	// Graphics assets for the entities
	/** Texture for all stars, as they look the same */
	private Texture starTexture;

	/** The minimum velocity factor (x shell velocity) of a newly created star */
	private static final float MIN_STAR_FACTOR = 0.1f;
	/** The maximum velocity factor (x shell velocity) of a newly created star */
	private static final float MAX_STAR_FACTOR = 0.2f;
	/** The minimum velocity offset (+ shell velocity) of a newly created star */
	private static final float MIN_STAR_OFFSET = -3.0f;
	/** The maximum velocity offset (+ shell velocity) of a newly created star */
	private static final float MAX_STAR_OFFSET = 3.0f;


	/** Maximum amount of health */
	final int MAX_HEALTH = 30;

	/** Number of band member lanes */
	int NUM_LANES;


	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<GameObject> objects;
	/** The backing set for garbage collection */
	private Array<GameObject> backing;


	/**
	 * Index of the currently active band member
	 */
	int activeBM;

	/**
	 * Level object, stores bandMembers
	 */
	Level level;

	/**
	 * Indicates whether or not we want to use randomly generated notes
	 */
	public boolean randomNotes;

	/**
	 * The Y coordinate at which a note will spawn. Notes should spawn completely invisible.
	 */
	float noteSpawnY;
	/**
	 * The y coordinate at which notes are considered "out of bounds." By the time a note reaches this y value
	 * it should already be completely invisible.
	 */
	float noteDieY;



	/**
	 * The minimum x value margin
	 */
	float LEFTBOUND;
	/**
	 * The maximum x value margin
	 */
	float RIGHTBOUND;
	/**
	 * The maximum y value margin
	 */
	float TOPBOUND;
	/**
	 * The minimum y value margin
	 */
	float BOTTOMBOUND;
	/**
	 * Width of an inact band member's lane
	 */

	float smallwidth;
	/**
	 * Width of an active band member's lane
	 */
	float largewidth;
	/**
	 * Width between each band member lane
	 */
	float inBetweenWidth;

	/**
	 * Y value of the hit bar
	 */
	float hitbarY;

	/**
	 * Maximum width of an HP bar
	 */

	float hpwidth;
	/**
	 * Width between each HP bar
	 */
	float hpbet;

	/**
	 * Create gameplaycontroler
	 * @param width
	 * @param height
	 */
	public GameplayController(float width, float height){
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		randomNotes = true;
		//Set margins so there is a comfortable amount of space between play area and screen boundaries
		//Values decided by pure look
		LEFTBOUND = width/10f;
		RIGHTBOUND = 9*width/10f;
		TOPBOUND = 19f*height/20f;
		BOTTOMBOUND = height/5f;


	}

	/**
	 * Loads a level
	 */
	public void loadLevel(JsonValue levelData, AssetDirectory directory){
		level = new Level(levelData, directory);
		NUM_LANES = level.getBandMembers().length;

		//The space in between two lanes is 1/4 the width of a small lane
		//the width of the large lane is 10x the width of a small lane
		//In total, we have NUM_LANES - 1 small lanes, 1 large lane, and n - 1 in between segments
		//Therefore, the width of the small lane shall be 1/(5NUM_LANES/4 + 35/4) of the available total width
		//Values decided by pure look
		smallwidth = (RIGHTBOUND - LEFTBOUND)/(5f * NUM_LANES/4f + 35f/4f);
		inBetweenWidth = smallwidth/4f;
		largewidth = 10f*smallwidth;
		//initiate default active band member to 0
		activeBM = 0;
		//Have the y value be a bit above the bottom of the play area, but not too close
		hitbarY = BOTTOMBOUND + 3*(TOPBOUND - BOTTOMBOUND)/20f;
		//There ar e NUM_LANES hp bars, and the width between each one shall be 1/4 their length
		//The width will then be 1/(5NUMLANES/4 - 1/4) of the total available width
		hpwidth = (RIGHTBOUND - LEFTBOUND)/(5f*NUM_LANES/4f - 0.25f);
		//Width between each HP bar shall be 1/4 of the width of an HP bar
		hpbet = hpwidth/4f;
		//instantiate other variables
		noteSpawnY = TOPBOUND + smallwidth/2;
		noteDieY = BOTTOMBOUND - smallwidth/2;
		switches = new boolean[NUM_LANES];
		triggers = new boolean[lpl];


	}

	/**
	 * Sets up the colors, max competency, lines and other default values as well as creates each band member object
	 */
	public void setupBandMembers(){
		level.setActiveProperties(0, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND);
		level.setBandMemberBl(new Vector2(LEFTBOUND, BOTTOMBOUND), inBetweenWidth);
	}


	/**
	 * Check for dead notes and out of bounds notes. Competency incremeting due to
	 * destroyed notes is also done in here.
	 */
	public void checkDeadNotes(){
		for(int i = 0; i < level.getBandMembers().length; ++i){
			for(Note n : level.getBandMembers()[i].getHitNotes()){
				//If a note is out of bounds and it has not been hit, we need to mark it destroyed and assign
				//a negative hit status
				if(n.getY() < noteDieY && !n.isDestroyed()){
					n.setHitStatus(-2);
					n.setDestroyed(true);
				}
				if(n.isDestroyed()){
					//if this note is destroyed we need to increment the competency of the
					//lane it was destroyed in by its hitstatus
					if(i == activeBM || i == goalBM){
						level.getBandMembers()[i].compUpdate(n.getHitStatus());
					}
				}
			}

			for(Note n : level.getBandMembers()[i].getSwitchNotes()){
				//If a note is out of bounds and it has not been hit, we need to mark it destroyed and assign
				//a negative hit status
				if(n.getY() < noteDieY && !n.isDestroyed()){
					n.setHitStatus(0);
					n.setDestroyed(true);
				}
				if(n.isDestroyed()){
					//if this note is destroyed we need to increment the competency of the
					//lane it was destroyed in by its hitstatus
					if(i == activeBM || i == goalBM){
						level.getBandMembers()[i].compUpdate(n.getHitStatus());
					}
				}
			}
		}
	}



	/**
	 * Populates this mode from the given the directory.
	 *
	 * The asset directory is a dictionary that maps string keys to assets.
	 * Assets can include images, sounds, and fonts (and more). This
	 * method delegates to the gameplay controller
	 *
	 * @param directory 	Reference to the asset directory.
	 */
	public void populate(AssetDirectory directory) {
		starTexture = directory.getEntry("star", Texture.class);
	}

	/**
	 * Returns the list of the currently active (not destroyed) game objects
	 *
	 * As this method returns a reference and Lists are mutable, other classes can
	 * technical modify this list.  That is a very bad idea.  Other classes should
	 * only mark objects as destroyed and leave list management to this class.
	 *
	 * @return the list of the currently active (not destroyed) game objects
	 */
	public Array<GameObject> getObjects() {
		return objects;
	}



	/**
	 * Starts level
	 */
	public void start() {
		setupBandMembers();
		activeBM = 0;
		goalBM = 0;
		updateBMCoords();
		curframe = 0;
		level.startmusic();
	}

	/**
	 * Updates the state.
	 *
	 */
	public void update(){
		//First, check for dead notes and remove them from active arrays
		checkDeadNotes();

		//Then, update the notes for each band member and spawn new notes
		level.updateBandMemberNotes();
		//Update the objects of this class (mostly stars)
		for(GameObject o : objects){
			o.update(0f);
		}
		//increment the current frame.
		++curframe;

	}


	/**
	 * Update the coordinates
	 */
	public void updateBMCoords(){
		if(curP == play_phase.NOTES){
			//If we are in the notes phase, we use setActiveProperties
			level.setActiveProperties(activeBM, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND);
		}
		else{
			//Otherwise we must be in transition, so set the transition properties
			float progressFrac = t_progress/(float)T_SwitchPhases;
			level.setTransitionProperties(activeBM, goalBM, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND, progressFrac);
		}
		//finally, set the bottom left
		level.setBandMemberBl(new Vector2(LEFTBOUND, BOTTOMBOUND), inBetweenWidth);
	}

	/**
	 * Resets the game, deleting all objects.
	 */
	public void reset() {
		//player = null;
		curframe = 0;
		objects.clear();
		curP = play_phase.NOTES;
	}
	/**
	 * The maximum number of lines per lane
	 */
	public int lpl = 4;

	/**
	 * Garbage collects all deleted objects.
	 *
	 * First perform garbage collection on the objects in here. Then perform garbage
	 * collection for each band member.
	 */
	public void garbageCollect() {
		// INVARIANT: backing and objects are disjoint
		for (GameObject o : objects) {
			if (!o.isDestroyed()) {
				backing.add(o);
			}
		}
		// Swap the backing store and the objects.
		// This is essentially stop-and-copy garbage collection
		Array<GameObject> tmp = backing;
		backing = objects;
		objects = tmp;
		backing.clear();
		for (BandMember bm : level.getBandMembers()) {
			bm.garbageCollect();
		}
	}


	/**
	 * Spawns stars at location x, y with center of mass velocity vx0 and vy0.
	 * Directions are randomzed, and more stars are spawned with a higher value of k
	 */
	public void spawnStars(int k, float x, float y){
		for(int i = 0; i < k; ++i){
			for (int j = 0; j < 5; j++) {
				Star s = new Star();
				s.setTexture(starTexture);
				s.getPosition().set(x, y);
				float vx = RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
				float vy = RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
				s.getVelocity().set(vx,vy);
				objects.add(s);
			}
		}
	}

	/**
	 * Enum to determine whether or not we are in a phase of hitting notes or switching to another band member
	 */
	public enum play_phase{
		NOTES,
		TRANSITION
	}

	/**
	 * initiate to NOTES phase
	 */
	play_phase curP = play_phase.NOTES;
	/**
	 * Total progress needed before we declare ourselves fully transitioned
	 */
	int T_SwitchPhases = 20;
	/**
	 * The band member lane index that we are trying to switch to
	 */
	int goalBM;


	/**
	 * The current transition progress
	 */
	int t_progress;

	/**
	 * Switch inputs
	 */
	public boolean[] switches;
	/**
	 * Trigger inputs
	 */
	public boolean[] triggers;

	/**
	 * Handle transitions and inputs
	 * @param input
	 */
	public void handleActions(InputController input){
		//Read in inputs
		switches = input.switches();
		triggers = input.didTrigger();
		boolean[] lifted = input.triggerLifted;
		long currentSample = level.getCurrentSample();
		//First handle the switches
		if(curP == play_phase.NOTES){
			for(int i = 0; i < switches.length; ++i){
				if(switches[i] && i != activeBM){
					//Check only the lanes that are not the current active lane
					for(Note n : level.getBandMembers()[i].getSwitchNotes()){
						//Check for all the switch notes of this lane, if one is close enough destroy it and
						//give it positive hit status
						long dist = Math.abs(currentSample - n.getHitSample());
						if(dist < 25000){
							n.setHitStatus(dist < 10000 ? 4 : 2);
							spawnStars(n.getHitStatus(), n.getX(), n.getY());
							n.setDestroyed(true);
						}
					}
					//set goalBM
					goalBM = i;
					//change phase
					curP = play_phase.TRANSITION;
					//reset progress
					t_progress = 0;
					return;
				}
			}
		}
		else{
			//Otherwise we must be in transition

			//Increment progress
			++t_progress;

			//Check if we are done, if so set active BM and change phase
			if(t_progress == T_SwitchPhases){
				curP = play_phase.NOTES;
				activeBM = goalBM;
			}
			//During this phase we need to change the BL and widths of each BM
			updateBMCoords();
		}
		//Now check for hit and held notes
		//This array tells us if a hit has already been registered in this frame for the ith bm.
		//We do not want one hit to count for two notes that are close together.
		boolean[] hitReg = new boolean[triggers.length];
		int checkBM = curP == play_phase.NOTES ? activeBM : goalBM;
		for(Note n : level.getBandMembers()[checkBM].getHitNotes()){
			if(n.getNoteType() == Note.NoteType.BEAT){
				if(triggers[n.getLine()] && !hitReg[n.getLine()]){
					//Check for all the notes in this line and in the active band member
					//See if any are close enough
					long dist = Math.abs(currentSample - n.getHitSample());
					if(dist < 25000){
						//If so, destroy the note and set a positive hit status. Also set that we
						//have registered a hit for this line for this click. This ensures that
						//We do not have a single hit count for two notes that are close together
						n.setHitStatus(dist < 10000 ? 2 : 1);
						spawnStars(n.getHitStatus(), n.getX(), n.getY());
						n.setDestroyed(true);
						hitReg[n.getLine()] = true;
					}
				}
			}
			else{
				//If it's not a beat and its in the hitNotes its gotta be a hold note

				//Check if we hit the trigger down close enough to the head
				if(triggers[n.getLine()] && !hitReg[n.getLine()]){
					long dist = Math.abs(currentSample - n.getHitSample());
					if(dist < 25000){
						n.setHitStatus(dist < 10000 ? 2 : 1);
						spawnStars(n.getHitStatus(), n.getX(), n.getBottomY());
						hitReg[n.getLine()] = true;
					}
				}

				//check if we lifted close to the end
				if(lifted[n.getLine()]){
					long dist = Math.abs(currentSample - (n.getHitSample() + n.getHoldSamples()));
					if(dist < 25000){
						n.setHitStatus(n.getHitStatus() + dist < 10000 ? 3 : 1);
						spawnStars(n.getHitStatus(), n.getX(), n.getY());
						n.setDestroyed(true);
					}
				}
			}
		}
	}
}