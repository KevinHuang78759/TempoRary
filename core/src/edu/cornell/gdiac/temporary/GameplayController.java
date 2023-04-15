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
	// note, currently not being used
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

	/** Index of the currently active band member */
	public int activeBandMember;
	/** Level object, stores bandMembers */
	public Level level;
	/** Indicates whether or not we want to use randomly generated notes */
	public boolean randomNotes;
	/** The Y coordinate at which a note will spawn. Notes should spawn completely invisible. */
	public float noteSpawnY;
	/**
	 * The y coordinate at which notes are considered "out of bounds." By the time a note reaches this y value
	 * it should already be completely invisible.
	 */
	public float noteDieY;
	/** The calibration offset (int samples */
	public int offset;
	/** Base offset for leniency in samples */
	private int baseLeniency;

	/** The minimum x value margin */
	public float LEFTBOUND;
	/** The maximum x value margin */
	public float RIGHTBOUND;
	/** The maximum y value margin */
	public float TOPBOUND;
	/** The minimum y value margin */
	public float BOTTOMBOUND;
	/** Width of an inact band member's lane */
	public float smallwidth;
	/** Width of an active band member's lane */
	public float largewidth;
	/** Width between each band member lane */
	public float inBetweenWidth;
	/** Y value of the hit bar */
	public float hitbarY;

	/** Maximum width of an HP bar */
	public float hpwidth;
	/** Width between each HP bar*/
	public float hpbet;


	/**
	 * Create gameplaycontroler
	 * @param width
	 * @param height
	 */
	public GameplayController(float width, float height){
		objects = new Array<>();
		backing = new Array<>();
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
		// 70 is referring to ms
		baseLeniency = (int) ((70f / 1000f) * level.getMusic().getSampleRate());

		//The space in between two lanes is 1/4 the width of a small lane
		//the width of the large lane is 10x the width of a small lane
		//In total, we have NUM_LANES - 1 small lanes, 1 large lane, and n - 1 in between segments
		//Therefore, the width of the small lane shall be 1/(5NUM_LANES/4 + 35/4) of the available total width
		//Values decided by pure look
		smallwidth = (RIGHTBOUND - LEFTBOUND)/(5f * NUM_LANES/4f + 35f/4f);
		inBetweenWidth = smallwidth/4f;
		largewidth = 10f*smallwidth;
		//initiate default active band member to 0
		activeBandMember = 0;
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
	 * Sets the offset in determining beat calculation, converting it to samples
	 * @param offset offset from CalibrationMode in milliseconds
	 */
	public void setOffset(int offset) {
		// need to convert to seconds first
		this.offset = (int) (((float) offset / 1000) * level.getMusic().getSampleRate());
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
//					n.setHitStatus(-2);
					n.setDestroyed(true);
				}
				if(n.isDestroyed()){
					//if this note is destroyed we need to increment the competency of the
					//lane it was destroyed in by its hitstatus
					if(i == activeBandMember || i == goalBandMember){
//						System.out.println("hit gained: " + n.getHitStatus());
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
					if(i == activeBandMember || i == goalBandMember){
						level.getBandMembers()[i].compUpdate(n.getHitStatus());
					}
				}
			}
		}
	}

	/**
	 * Helper function to determine whether a band member has 0 competency for losing
	 * @return returns if at least bone band members has 0 competency
	 */
	public boolean hasZeroCompetency() {
		for (BandMember bandMember : level.getBandMembers()) {
			if (bandMember.getCurComp() == 0) {
				return true;
			}
		}
		return false;
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
		activeBandMember = 0;
		goalBandMember = 0;
		updateBandMemberCoords();
		curframe = 0;
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
	public void updateBandMemberCoords(){
		if(curP == PlayPhase.NOTES){
			//If we are in the notes phase, we use setActiveProperties
			level.setActiveProperties(activeBandMember, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND);
		}
		else{
			//Otherwise we must be in transition, so set the transition properties
			float progressFrac = t_progress/(float)T_SwitchPhases;
			level.setTransitionProperties(activeBandMember, goalBandMember, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND, progressFrac);
		}
		//finally, set the bottom left
		level.setBandMemberBl(new Vector2(LEFTBOUND, BOTTOMBOUND), inBetweenWidth);
	}

	/**
	 * Resets the game, deleting all objects.
	 */
	public void reset() {
		curframe = 0;
		objects.clear();
		curP = PlayPhase.NOTES;
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
		for (BandMember bandMember : level.getBandMembers()) {
			bandMember.garbageCollect();
		}
	}

	/**
	 * Spawns stars at location x, y with center of mass velocity vx0 and vy0.
	 * Directions are randomzed, and more stars are spawned with a higher value of k
	 */
	public void spawnHitEffect(int k, float x, float y){
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
	public enum PlayPhase {
		NOTES,
		TRANSITION
	}

	/** initiate to NOTES phase*/
	PlayPhase curP = PlayPhase.NOTES;
	/** Total progress needed before we declare ourselves fully transitioned */
	int T_SwitchPhases = 20;
	/** The band member lane index that we are trying to switch to */
	int goalBandMember;
	/** The current transition progress */
	int t_progress;

	/** Switch inputs */
	public boolean[] switches;
	/** Trigger inputs */
	public boolean[] triggers;

	/**
	 * Handles logic of a hit, whether it is on beat or not, etc.
	 * @param note the note that we are trying to hit
	 * @param currentSample the current sample of the song
	 * @param hitReg the hitReg array
	 * @param lifted whether the note was lifted (if held, can only be true if NoteType == HELD)
	 */
	public void checkHit(Note note,
						 long currentSample,
						 int onBeatGain, int offBeatGain,
						 float spawnEffectY,
						 boolean destroy,
						 boolean[] hitReg,
						 boolean lifted) {
		// check for precondition that lifted is true iff note type is HELD
		assert !lifted || note.getNoteType() == Note.NoteType.HELD;

		//Check for all the switch notes of this lane, if one is close enough destroy it and
		//give it positive hit status
		long adjustedPosition = currentSample - this.offset;
		long dist = lifted ? Math.abs(adjustedPosition - (note.getHitSample() + note.getHoldSamples()))
				: Math.abs(adjustedPosition - note.getHitSample());

		// check if note was hit or on beat
		if(dist < 25000) {
			if (dist < 18000) {
				//If so, destroy the note and set a positive hit status. Also set that we
				//have registered a hit for this line for this click. This ensures that
				//We do not have a single hit count for two notes that are close together
				boolean isOnBeat = dist < baseLeniency;

				note.setHitStatus(isOnBeat ? onBeatGain : offBeatGain);
				spawnHitEffect(note.getHitStatus(), note.getX(), spawnEffectY);
				if (note.getLine() != -1) hitReg[note.getLine()] = true;
				note.setDestroyed(destroy);
			}
			else {
				// lose some competency since you played a bit off beat
				// TODO: REWORK THIS
				note.setHitStatus(-1);
			}
		}
	}

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

		//This array tells us if a hit has already been registered in this frame for the ith bm.
		//We do not want one hit to count for two notes that are close together.
		boolean[] hitReg = new boolean[triggers.length];

		// SWITCH NOTE HIT HANDLING
		if (curP == PlayPhase.NOTES){
			for (int i = 0; i < switches.length; ++i){
				if (switches[i] && i != activeBandMember){
					//Check only the lanes that are not the current active lane
					for(Note n : level.getBandMembers()[i].getSwitchNotes()) {
						checkHit(n, currentSample, 8, 5, n.getY(),true, hitReg, false);
					}
					//set goalBM
					goalBandMember = i;
					//change phase
					curP = PlayPhase.TRANSITION;
					//reset progress
					t_progress = 0;
					return;
				}
			}
		}
		else {
			//Otherwise we must be in transition

			//Increment progress
			++t_progress;

			//Check if we are done, if so set active BM and change phase
			if(t_progress == T_SwitchPhases){
				curP = PlayPhase.NOTES;
				activeBandMember = goalBandMember;
			}
			//During this phase we need to change the BL and widths of each BM
			updateBandMemberCoords();
		}
		//Now check for hit and held notes
		int checkBandMember = curP == PlayPhase.NOTES ? activeBandMember : goalBandMember;
		for(Note n : level.getBandMembers()[checkBandMember].getHitNotes()){
			if(n.getNoteType() == Note.NoteType.BEAT){
				if(triggers[n.getLine()] && !hitReg[n.getLine()]){
					//Check for all the notes in this line and in the active band member
					//See if any are close enough
					checkHit(n, currentSample, 3, 1, n.getY(),true, hitReg, false);
				}
			}
			else{
				// HOLD NOTE
				//Check if we hit the trigger down close enough to the head
				if(triggers[n.getLine()] && !hitReg[n.getLine()]){
					checkHit(n, currentSample, 4, 2, n.getBottomY(),false, hitReg, false);
				}
				//check if we lifted close to the end
				if(lifted[n.getLine()]){
					checkHit(n, currentSample, 4, 2, n.getY(),true, hitReg, true);
				}
			}
		}
	}
}