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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Music;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.entity.*;
import edu.cornell.gdiac.temporary.GameObject.ObjectType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller to handle gameplay interactions.
 *
 * This controller also acts as the root class for all the models.
 */
public class GameplayController {
	int currentFrame;
	// Graphics assets for the entities
	/** Texture for all stars, as they look the same */
	private Texture starTexture;
	/** Texture for cat note, as they look the same */
	private Texture catNoteTexture;
	/** Data for level */
	private JsonValue levelData;

	/** Song!!! music */
	private Music music;
	/** Reference to the current level */
	private Level level;
	/** Reference to the current level's bandMembers (just for ease of access lol) */
	private BandMember[] bandMembers;
	/** Reference to the current Level's notes THAT ARE CURRENTLY ACTIVE, by BAND MEMBER */
	private ArrayList<Fish> activeNotes;

	/** Handle playing music and spawning notes */
	private MusicController musicController;

	/** Time elapsed in song */
	private float time;

	/** The minimum velocity factor (x shell velocity) of a newly created star */
	private static final float MIN_STAR_FACTOR = 0.1f;
	/** The maximum velocity factor (x shell velocity) of a newly created star */
	private static final float MAX_STAR_FACTOR = 0.2f;
	/** The minimum velocity offset (+ shell velocity) of a newly created star */
	private static final float MIN_STAR_OFFSET = -3.0f;
	/** The maximum velocity offset (+ shell velocity) of a newly created star */
	private static final float MAX_STAR_OFFSET = 3.0f;
	/** The amount of health gained when hitting a note weakly */
	private static final int WEAK_HIT_HEALTH = 1;
	/** The amount of health gained when hitting a note strongly */
	private static final int STRONG_HIT_HEALTH = 2;
	/** The amount of health lost when missing a note */
	private static final int MISS_HIT_HEALTH = 2;

	/** Maximum amount of health */
	final int MAX_HEALTH = 30;

	/** Number of band member lanes */
	int NUM_LANES;

	/** Number of notes present in repeating rhythm */
	private static final int NUM_NOTES = 60;
	/** Note count for the display in window corner */
	private int shellCount;

	/** health of each band member */
	private int[] health;

	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<GameObject> objects;
	/** The backing set for garbage collection */
	private Array<GameObject> backing;
	/** Active lane currently */

	HashMap<Integer, Note> noteCoords = new HashMap<>();

	/**
	 * Index of the currently active band member
	 */
	int currentBandMember;

	/** where notes are spawned and died */
	public float noteSpawnY;
	public float noteDieY;

	/**
	 * The base hp increment. Hp will incrememnt on a destroyed note by the product of this value and its hit status
	 */
	final int recovery = 3;

	private void setCoords(float width, float height) {
		// note appears every two seconds if we have a 30 second loop
		// 1800
		for (int i = 0; i < NUM_NOTES; i++) {
			Note s = new Note(i%4, Note.NType.BEAT);
			s.setX(width/8 + (i % 4) * width/4);
			s.setTexture(catNoteTexture);
			s.setY(height);
			s.setVX(0);
			s.setVY(-5f);
			noteCoords.put(i * 30, s);
		}
	}

	/**
	 * Indicates whether or not we want to use randomly generated notes
	 */
	public boolean randomnotes;

	/**
	 * The minimum x value margin
	 */
	 public float LEFTBOUND;
	/**
	 * The maximum x value margin
	 */
	public float RIGHTBOUND;
	/**
	 * The maximum y value margin
	 */
	public float TOPBOUND;
	/**
	 * The minimum y value margin
	 */
	public float BOTTOMBOUND;
	/**
	 * Width of an inact band member's lane
	 */

	public float smallwidth;
	/**
	 * Width of an active band member's lane
	 */
	public float largewidth;
	/**
	 * Width between each band member lane
	 */
	public float inBetweenWidth;

	/**
	 * Y value of the hit bar
	 */
	public float hitbarY;

	/**
	 * Maximum width of an HP bar
	 */
	public float hpwidth;

	/**
	 * Width between each HP bar
	 */
	public float hpbet;

	public GameplayController(int lanes, int linesPerLane, float width, float height){
		NUM_LANES = lanes;
		shellCount = 0;
		//initializeHealth(); // HEALTH SHOULD BE INDEPENDENT AND HANDLED BY BAND MEMBERS. todo: delete?
		//hsflag = false;
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		currentBandMember = 0; //default

		//randomnotes = true;
		//Set margins so there is a comfortable amount of space between play area and screen boundaries
		//Values decided by pure look
		LEFTBOUND = width/10f;
		RIGHTBOUND = 9*width/10f;
		TOPBOUND = 19f*height/20f;
		BOTTOMBOUND = height/5f;
		//The space in between two lanes is 1/4 the width of a small lane
		//the width of the large lane is 10x the width of a small lane
		//In total, we have NUM_LANES - 1 small lanes, 1 large lane, and n - 1 in between segments
		//Therefore, the width of the small lane shall be 1/(5NUM_LANES/4 + 35/4) of the available total width
		//Values decided by pure look
		smallwidth = (RIGHTBOUND - LEFTBOUND)/(5f * NUM_LANES/4f + 35f/4f);
		inBetweenWidth = smallwidth/4f;
		largewidth = 10f*smallwidth;
		//initiate default active band member to 0
		currentBandMember = 0;
		//Have the y value be a bit above the bottom of the play area, but not too close
		hitbarY = BOTTOMBOUND + 3*height/20f;
		//There are NUM_LANES hp bars, and the width between each one shall be 1/4 their length
		//The width will then be 1/(5NUMLANES/4 - 1/4) of the total available width
		hpwidth = (RIGHTBOUND - LEFTBOUND)/(5f*NUM_LANES/4f - 0.25f);
		//Width between each HP bar shall be 1/4 of the width of an HP bar
		hpbet = hpwidth/4f;
		//instantiate other variables
		heldPresent = new boolean[linesPerLane];
		triggers = new boolean[linesPerLane];
		lpl = 1;
		noteSpawnY = TOPBOUND + smallwidth/2;
		noteDieY = BOTTOMBOUND - smallwidth/2;
		triggers = new boolean[linesPerLane];
		switches = new boolean[lanes];

		activeNotes = new ArrayList<Fish>();
	}

	// TODO: DELETE?
	/*private void initializeHealth() {
		health = new int[NUM_LANES];
		for (int i = 0; i < NUM_LANES; i++) {
			health[i] = MAX_HEALTH;
		}
	}*/

	/*public boolean checkHealth(boolean dec) {
		for (int i = 0; i < NUM_LANES; i++) {
			health[i] = Math.min(MAX_HEALTH, health[i]);
			if(dec){
				--health[i];
			}

			if(health[i] <= 0){
				return true;
			}
		}
		return false;
	}*/

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
		catNoteTexture = directory.getEntry("catnote", Texture.class);
		music = directory.getEntry("flagship", Music.class);
		levelData = directory.getEntry("level_flagship", JsonValue.class);

		// initialize objects
		populateLevel();
	}

	/** Add object to list of objeccts */
	public void addObject(GameObject obj){
		objects.add(obj);
	}

	/** Initialize level and band member data */
	public void populateLevel(){
		level = new Level(levelData); // initializes level AND BAND MEMBERS
		bandMembers = level.getBandMembers();
		musicController = new MusicController(music, level, bandMembers);

		// TODO: THIS IS NOT EFFICIENT AT ALL LMAO
	}

	/** Initialize each band member's drawable location */
	public void setupBandMembers(Color[] c){
		float XCoor = LEFTBOUND;

		for(int i = 0; i < NUM_LANES; i++){
			bandMembers[i].bottomLeftCorner.x = XCoor;
			bandMembers[i].bottomLeftCorner.y = BOTTOMBOUND;
			bandMembers[i].width = i == 0 ? largewidth : smallwidth;
			bandMembers[i].lineHeight = i == 0 ? TOPBOUND - BOTTOMBOUND : 0;
			bandMembers[i].height = TOPBOUND - BOTTOMBOUND;
			bandMembers[i].numLines = lpl;
			bandMembers[i].maxCompetency = MAX_HEALTH;
			bandMembers[i].competency = MAX_HEALTH;
			XCoor += bandMembers[i].width + inBetweenWidth;
		}
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
	 * Returns the line healths.
	 *
	 * @return the line healths.
	 */
	public int[] getHealth() {return health;}

	/**
	 * Returns the amount of lines.
	 *
	 * @return The amount of lines.
	 */
	public int lineAmount(){
		return health.length;
	}

	/** get bandMembers */
	public BandMember[] getBandMembers() {
		return bandMembers;
	}

	/**
	 * Starts a new game.
	 *
	 * This method creates a single player, but does nothing else.
	 *
	 * @param r Option to use random notes or not
	 */
	public void start() {

		setupBandMembers(new Color[]{Color.BLUE, Color.GOLDENROD, Color.CORAL,Color.MAROON});
		currentBandMember = 0;
		currentFrame = 0;

		// play music
		music.play();
		//randomnotes = r;
	}

	/***/
	public void update(){
		checkDeadNotes();

		for(BandMember bandMember : bandMembers){
			// update notes by current frame
			// bandMember.updateNotes(currentFrame);

			// spawn notes by current frame
			// bandMember.spawnNotes(currentFrame);
			if(currentFrame % 120 == 1){
				bandMember.updateCompetency(-1);
			}
		}
		for(GameObject o : objects){
			o.update(0f);
		}
		currentFrame++;
	}

	/** Update competency of all band members.
	 *
	 * @return true if one of them died, false if not
	 *
	 * */
	public boolean checkAllCompetencies(boolean decreasing) {
		for (BandMember bandMember : bandMembers) {
			bandMember.updateCompetency(decreasing);
			if (bandMember.isCaught()) {
				return true;
			}
		}
		return false;
	}

	public void updateBandMemberCoords(){
		if(currentPhase == playPhase.PLAYING){
			//If we are in the notes phase, we set the width of the active lane to goal, and everything else to small
			//We also set the line height of everything to 0 except for the active lane
			float XCoord = LEFTBOUND;
			for(int i = 0; i < bandMembers.length; ++i){
				bandMembers[i].bottomLeftCorner.x = XCoord;
				if(i == currentBandMember){
					bandMembers[i].width = largewidth;
					bandMembers[i].lineHeight = TOPBOUND - BOTTOMBOUND;
				}
				else{
					bandMembers[i].width = smallwidth;
					bandMembers[i].lineHeight = 0f;
				}
				XCoord += bandMembers[i].width + inBetweenWidth;
			}
		}
		else{
			//Otherwise we must be in transition
			float progressFrac = t_progress/(float)T_SwitchPhases;
			float XCoord = LEFTBOUND;
			for(int i = 0; i < bandMembers.length; ++i){
				bandMembers[i].bottomLeftCorner.x = XCoord;
				if(i == currentBandMember){
					bandMembers[i].width = (largewidth - smallwidth)*(1-progressFrac) + smallwidth;
					bandMembers[i].lineHeight = (TOPBOUND - BOTTOMBOUND)*(1-progressFrac);
				}
				else if(i == goalBandMember){
					bandMembers[i].width = (largewidth - smallwidth)*(progressFrac) + smallwidth;
					bandMembers[i].lineHeight = (TOPBOUND - BOTTOMBOUND)*(progressFrac);
				}
				else{
					bandMembers[i].width = smallwidth;
					bandMembers[i].lineHeight = 0f;
				}
				XCoord += bandMembers[i].width + inBetweenWidth;
			}
		}
	}

	/**
	 * Resets the game, deleting all objects and setting their parameters to their default values.
	 * todo: RESET FUNCTIONS IN LEVEL, FISH, BAND MEMBER
	 */
	public void reset() {
		currentFrame = 0;
		objects.clear();
		//initializeHealth();
	}

	/**
	 * Array to indicate whether or not a certain line of the active band member lane contains an active held note
	 * We need this so that we do not spawn a  note in the middle of a held note if we are using random
	 */
	boolean[] heldPresent;

	/**
	 * The number of lines per lane
	 */
	public int lpl;

	/** TODO: WHAT */
	public void checkDeadNotes(){
		for(int i = 0; i < bandMembers.length; ++i){
			for(Fish n : bandMembers[i].hitNotes){
				if(n.getY() < noteDieY && n.hitStatus == 0){
					n.hitStatus = -2;
					n.setDestroyed(true);
				}
				if(n.isDestroyed()){
					if(i == currentBandMember || i == goalBandMember){
						bandMembers[i].updateCompetency(n.hitStatus);
					}
				}
			}
		}
	}


	/** Add new notes to the game using MusicController
	 *
	 * @param height current game height
	 * @param frame frame / tick?? idk actually lol*/
	public void updateNotes(float delta, float height, int frame){
		// check with music controller
		ArrayList<Fish> newNotes = musicController.update(currentBandMember, delta);

		if(newNotes.size() > 0){
			for(Fish note : newNotes){
				activeNotes.add(note);
				note.setPosition(height, currentBandMember, catNoteTexture, smallwidth, largewidth, inBetweenWidth, LEFTBOUND);
				objects.add(note);
			}
		}

		System.out.println(objects);
	}


	/**
	 * Adds a new shell to the game.
	 *
	 * A shell is generated at the top with a random horizontal position. Notice that
	 * this allocates memory to the heap.  If we were REALLY worried about performance,
	 * we would use a memory pool here.
	 *
	 * @param height Current game height
	 */
	public void addNote(float width, float height, int frame) {
		randomnotes = true;
		if(randomnotes){
//			int lane = RandomController.rollInt(0,lpl-1);
//			int dur = RandomController.rollInt(1, 3);
//			if(frame % 250 == 0&& curP == play_phase.NOTES && !heldPresent[lane]){
//				Note h = new Note(lane, Note.NType.HELD);
//				heldPresent[lane] = true;
//				h.setX(LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/(2*lpl) + lane*(largewidth/lpl));
//				h.bx = LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/(2*lpl) + lane*(largewidth/lpl);
//				h.startFrame = frame;
//				h.holdFrame = 60 + (15 * dur);
//				h.setY(height);
//				h.by = height;
//				h.tail_thickness = 15f;
//				h.setTexture(greenTexture);
//				h.setTailTexture(catNoteTexture);
//				objects.add(h);
//				++shellCount;
//			}
//			if(frame%45 == 0 && curP == play_phase.NOTES){
//				int det = RandomController.rollInt(0,lpl);
//				if(det < 4 && !heldPresent[det]){
//					Note s = new Note(det, Note.NType.BEAT);
//					s.setX(LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/(2*lpl) + det*(largewidth/lpl));
//					s.setTexture(catNoteTexture);
//					s.setY(height);
//					s.setVX(0);
//					objects.add(s);
//					++shellCount;
//				}
//			}
//			if(frame%450 == 0 && curP == play_phase.NOTES){
//				int det = RandomController.rollInt(0,NUM_LANES - 1);
//				if(det != currentLane){
//					Note s = new Note(det, Note.NType.SWITCH);
//					s.setX(LEFTBOUND + (det * (smallwidth + inBetweenWidth) + (det > currentLane ? largewidth - smallwidth : 0)) + smallwidth/2f);
//					s.setTexture(greenTexture);
//					s.setY(height);
//					s.setVX(0);
//					objects.add(s);
//					++shellCount;
//				}
//			}
		}
		else{
			//add notes in fixed pattern
//			Note s = noteCoords.get(frame);
//			if (s != null) {
//				objects.add(s);
//				shellCount++;
//
//				if (shellCount == NUM_NOTES) {
//					setCoords(width, height);
//					shellCount = 0;
//				}
//			}
		}
	}

	/**
	 * Garbage collects all deleted objects.
	 *
	 * This method works on the principle that it is always cheaper to copy live objects
	 * than to delete dead ones.  Deletion restructures the list and is O(n^2) if the
	 * number of deletions is high.  Since Add() is O(1), copying is O(n).
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
		for (BandMember bm : bandMembers) {
			bm.garbageCollect();
		}
	}

	/**
	 * Process specialized destruction functionality
	 *
	 * Some objects do something special (e.g. explode) on destruction. That is handled
	 * in this method.
	 *
	 * Notice that this allocates memory to the heap.  If we were REALLY worried about
	 * performance, we would use a memory pool here.
	 *
	 * @param o Object to destroy
	 */
	protected void destroy(GameObject o) {
		switch (o.getType()) {
			case FISH:
				// Create some stars if hit on beat - more stars if more accurate
//				if (((Fish) o).getNoteType() == Fish.NoteType.HELD) {
//					System.out.println("HELD NOTE DESTROYED");
//					heldPresent[((Fish) o).line] = false;
//				}
				spawnStars(((Fish) o).hitStatus, o.getX(), o.getY(), o.getVX(), o.getVY());
				int hpUpdate = ((Fish) o).getNoteType() == Fish.NoteType.SWITCH ? goalBandMember : currentBandMember;
				if(hpUpdate == 1) {bandMembers[currentBandMember].addHealth();}
//				health[hpUpdate] += ((Note) o).hitStatus * recovery;
//				health[hpUpdate] = Math.min(MAX_HEALTH, health[hpUpdate]);
//				health[hpUpdate] = Math.max(0, health[hpUpdate]);
				break;
			default:
				break;
		}
	}

	public void spawnStars(int k, float x, float y, float vx0, float vy0){
		for(int i = 0; i < k; ++i){
			for (int j = 0; j < 5; j++) {
				Star s = new Star();
				s.setTexture(starTexture);
				s.getPosition().set(x, y);
				float vx = vx0 * RandomController.rollFloat(MIN_STAR_FACTOR, MAX_STAR_FACTOR)
						+ RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
				float vy = vy0 * RandomController.rollFloat(MIN_STAR_FACTOR, MAX_STAR_FACTOR)
						+ RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
				s.getVelocity().set(vx,vy);
				objects.add(s);
			}
		}
	}

	/**
	 * Enum to determine whether or not we are in a phase of hitting notes or switching to another band member
	 */
	public enum playPhase {
		PLAYING,
		TRANSITION
	}

	/**
	 * initiate to NOTES phase
	 */
	playPhase currentPhase = playPhase.PLAYING;

	/**
	 * Total progress needed before we declare ourselves fully transitioned
	 */
	int T_SwitchPhases = 20;

	/**
	 * The band member lane index that we are trying to switch to
	 */
	int goalBandMember;

	/**
	 * Whether or not a trigger for a certain line was pressed
	 */
	public boolean[] triggers;

	/**
	 * The current transition progress
	 */
	int t_progress;

	/**
	 * Whether or not we have indicated we want to switch to a certain lane
	 */
	public boolean[] switches;

	public void handleActions(InputController input){
		switches = input.switches();
		triggers = input.didTrigger();
		boolean[] lifted = input.triggerLifted;
		//First handle the switches
		if(currentPhase == playPhase.PLAYING){
			for(int i = 0; i < switches.length; ++i){
				if(switches[i] && i != currentBandMember){
					for(Fish n : bandMembers[i].switchNotes){
						float dist = Math.abs(hitbarY - n.getY())/n.h;
						if(dist < 1.5){
							n.hitStatus = dist < 0.75 ? 4 : 2;
							spawnStars(n.hitStatus, n.x, n.y, 0, n.vy);
							n.setDestroyed(true);
						}
					}
					goalBandMember = i;
					currentPhase = playPhase.TRANSITION;
					t_progress = 0;
					return;
				}
			}
		}
		else{
			//Otherwise we must be in transition
			++t_progress;

			if(t_progress == T_SwitchPhases){
				currentPhase = playPhase.PLAYING;
				currentBandMember = goalBandMember;
			}
			updateBandMemberCoords();
		}
		//Now check for hit and held notes
		//This array tells us if a hit has already been registered in this frame for the ith bm.
		//We do not want one hit to count for two notes that are close together.
		boolean[] hitReg = new boolean[triggers.length];
		int checkBM = currentPhase == playPhase.PLAYING ? currentBandMember : goalBandMember;
		for(Fish n : bandMembers[checkBM].hitNotes){
			if(n.getNoteType() == Fish.NoteType.SINGLE){
				if(triggers[n.line] && !hitReg[n.line]){
					float dist = Math.abs(hitbarY - n.y)/n.h;
					if(dist < 1.5){
						n.hitStatus = dist < 0.75 ? 2 : 1;
						spawnStars(n.hitStatus, n.x, n.y, 0, n.vy);
						n.setDestroyed(true);
						hitReg[n.line] = true;
					}
				}
			}
			else{
				//If it's not a beat and its in the hitNotes its gotta be a hold note
				if(triggers[n.line] && !hitReg[n.line]){
					float dist = Math.abs(hitbarY - n.by)/n.h;
					if(dist < 1.5){
						n.hitStatus += dist < 0.75 ? 2 : 1;
						spawnStars(n.hitStatus, n.x, n.y, 0, n.vy);
						hitReg[n.line] = true;
					}
				}
				if(lifted[n.line]){
					float dist = Math.abs(hitbarY - n.y)/n.h;
					if(dist < 1.5){
						n.hitStatus += dist < 0.75 ? 3 : 1;
						spawnStars(n.hitStatus, n.x, n.y, 0, n.vy);
						n.setDestroyed(true);
					}
				}
			}
		}
	}


	/**
	 * Resolves state changes into and out of the TRANSITION phase
	 */
	public void resolvePhase(InputController input){
		//If we are currently in a note phase, detect for switch presses
		if(currentPhase == playPhase.PLAYING){
			switches = input.switches();
			// If switch was made, check if there was a switch note present. this will increase health by a bit TODO
			for(int i = 0; i < switches.length; ++i){
				//For each active switch, check if it is not the current active band member lane.
				//If it is, do nothing
				if(switches[i] && i != currentBandMember){
					//If it is not, initiate change to TRANSITION phase
					//First set the goal band member to the detected switch
					goalBandMember = i;
					//Change the phase to TRANSITION
					currentPhase = playPhase.TRANSITION;
					//Start transition progress at 0
					t_progress = 0;

					// Check switch notes amongst objects TODO is this the most efficient way to do this?
					for(GameObject o : objects){
						if(o.getType() == ObjectType.FISH){
							//For all NOTE objects, check for switch notes
							//This variable is to make sure we do not attempt to destroy a switch note twice
							boolean switchToggle = false;
							if(((Fish)o).getNoteType() == Fish.NoteType.SWITCH){
								if(switches[((Fish)o).line]){
									//If there is a switch note on this line and within bounds, destroy it and reward HP points
									//Also set the switchTog variable to true
									if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
										((Fish) o).hitStatus = 4;
										switchToggle = true;
										o.setDestroyed(true);
									} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
										((Fish) o).hitStatus = 2;
										switchToggle = true;
										o.setDestroyed(true);
									}
									else {
										((Fish) o).hitStatus = 0;
									}
								}
							}
							// if didn't switch, set note to be destroyed.
							if(!switchToggle){
								//If this note was not a switch that was hit on time, destroy it
								o.setDestroyed(true);
							}
						}
					}
					//Once we have detected one switch, just break out of the loop. We do not want to attempt to
					//switch twice in the same frame
					break;
				}
			}
		}
		else{
			//If we are already in the TRANSITION PHASE, check to see if we are done transitioning
			if (t_progress == T_SwitchPhases){
				//If we are, set the currentLane to the previous goal lane
				currentBandMember = goalBandMember;
				//Change phase to NOTES phase
				currentPhase = playPhase.PLAYING;
			}
		}
	}

	/**
	 * Handle actions other than switching (mainly pressing and holding)
	 * @param input
	 * @param delta
	 * @param frame
	 */
	public void resolveActions(InputController input, float delta, int frame) {
		if(currentPhase == playPhase.PLAYING){
			//If we are in the NOTES phase, get trigger input
			triggers = input.didTrigger();
			// Process the objects.
			for (GameObject o : objects) {
				//Objects may have been destroyed but not despawned by the resolvePhases method, which gets called before
				if(o.destroyed){
					//If so, ignore these objects
					continue;
				}
				if(o.getType() == ObjectType.FISH){
					//If the object is a note, first update the note
					((Fish)o).update(delta, frame);
					//If the note is a BEAT, detect whether we have a trigger pressed on its line
					if(((Fish)o).getNoteType() == Fish.NoteType.SINGLE){
						if(triggers[((Fish)o).getLane()]){
							//If the trigger is pressed while the BEAT is in appropriate bounds, destroy the note
							//and award HP points.
							//We need to return after each one so that we don't register 1 trigger click for two notes
							//that spawned close together
							if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								((Fish) o).hitStatus = 2;
								o.setDestroyed(true);
								return;

							} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								((Fish) o).hitStatus = 1;
								o.setDestroyed(true);
								return;
							}
							else {
								//Otherwise set its hit value to 0
								((Fish) o).hitStatus = 0;
							}
						}
					}
					else if(((Fish)o).getNoteType() == Fish.NoteType.HOLD){
//						//If the note is a held note, detect if we have a trigger on its  line
//						if(triggers[((Fish)o).line]){
//							//If the bottom of the note is within bounds, spawn some stars
//							//We need to return again for similar reasons to the beat notes
//							if(((Fish)o).by <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
//								System.out.println("Good hold start");
//								spawnStars(3, ((Fish)o).bx, ((Fish)o).by, 0, Fish.descentSpeed);
//								return;
//
//							} else if (((Fish)o).by <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
//								System.out.println("hold start");
//								spawnStars(1, ((Fish)o).bx, ((Fish)o).by, 0, Fish.descentSpeed);
//								return;
//							}
//							else{
//								System.out.println("hold missed");
//								((Fish)o).hitStatus = 0;
//							}
//						}
//						//Now detect if a trigger was lifted
//						// TODO: remove print statements
//						if(input.triggerLifted[((Fish)o).line]){
//							System.out.println("let go");
//							//If the trigger was lifted when the tail of the held note is near the hitbarY value,
//							//destroy the note and reward some HP
//							if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
//								System.out.println("Good hold end");
//								((Fish)o).hitStatus = 4;
//								o.setDestroyed(true);
//								return;
//							} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
//								System.out.println("hold end");
//								((Fish)o).hitStatus = 2;
//								o.setDestroyed(true);
//								return;
//							}
//							else{
//								System.out.println("hold end missed");
//							}
//						}
					}
				}
				else {
					o.update(delta);
				}
			}
		}
		else{
			//If we are not in the NOTES phase, we must be in TRANSITION
			//update the progress
			//Update all the objects, unless they were destroyed
			++t_progress;
			for (GameObject o : objects) {
				if (o.destroyed) {
					continue;
				}
				o.update(delta);
			}
		}
	}
}