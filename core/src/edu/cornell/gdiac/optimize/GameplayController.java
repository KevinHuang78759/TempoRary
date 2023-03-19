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
package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.optimize.entity.*;
import edu.cornell.gdiac.optimize.GameObject.ObjectType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller to handle gameplay interactions.
 * </summary>
 * <remarks>
 * This controller also acts as the root class for all the models.
 */
public class GameplayController {
	// Graphics assets for the entities
	/** Texture for all ships, as they look the same */
	private Texture beetleTexture;
	/** Texture for all stars, as they look the same */
	private Texture starTexture;
	/** Texture for all bullets, as they look the same */
	private Texture bulletTexture;
	/** Texture for green shells, as they look the same */
	private Texture greenTexture;
	/** Texture for red shells, as they look the same */
	private Texture redTexture;





	/** The minimum x-velocity of a newly generated shell */
	private static final float MIN_SHELL_VX = 3;
	/** The maximum y-velocity of a newly generated shell */
	private static final float MAX_SHELL_VX = 10;
	/** The y-position offset of a newly generated bullet */
	private static final float BULLET_OFFSET = 5.0f;
	/** The vertical speed of a newly generated bullet */
	private static final float BULLET_SPEED  = 10.0f;
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

	float[] curWidths;

	/** Number of band member lanes */
	int NUM_LANES;

	/** Number of notes present in repeating rhythm */
	private static final int NUM_NOTES = 60;

	/** Reference to player (need to change to allow multiple players) */
	//private Ship player;
	/** Note count for the display in window corner */
	private int shellCount;

	/** health of each band member */
	private int[] health;

	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<GameObject> objects;
	/** The backing set for garbage collection */
	private Array<GameObject> backing;

	HashMap<Integer, Note> noteCoords = new HashMap<>();

	/**
	 * Index of the currently active band member
	 */
	int currentLane;

	/**
	 * The base hp increment. Hp will incrememnt on a destroyed note by the product of this value and its hit status
	 */
	final int recovery = 3;

	/**
	 * Indicates whether or not we want to use randomly generated notes
	 */
	public boolean randomnotes;
	/**
	 * Creates a new GameplayController with no active elements.
	 */
	public GameplayController(boolean rn, int lanes) {
		curWidths = new float[lanes];
		NUM_LANES = lanes;
		shellCount = 0;
		initializeHealth();
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		currentLane = 0;
		randomnotes = rn;
	}

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
	float curHeight;
	public GameplayController(boolean rn, int lanes, int linesPerLane, float width, float height){
		curWidths = new float[lanes];
		NUM_LANES = lanes;
		shellCount = 0;
		initializeHealth();
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		randomnotes = true;
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
		currentLane = 0;
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
		lpl = linesPerLane;
	}

	public void updateWidth(){
		for(int i = 0; i < NUM_LANES; ++i){
			if(curP == GameplayController.play_phase.NOTES){
				//If we are in a NOTES phase, all widths are small except for the active lane, which is large
				curWidths[i] = currentLane == i ? largewidth : smallwidth;
				curHeight = TOPBOUND - BOTTOMBOUND;
			}
			else{
				//Otherwise we must be transitioning.
				if(i == currentLane){
					//If this is the current active lane, make sure it shrinks, and decrease the height
					curWidths[i] = largewidth + (float)(t_progress)*(smallwidth - largewidth)/(float)(T_SwitchPhases);
					curHeight = (TOPBOUND - BOTTOMBOUND) * (float)(T_SwitchPhases-t_progress)/(float)(T_SwitchPhases);
				}
				else if(i == goal){
					//If this is the goal lane we are trying to transition to, make sure it grows
					curWidths[i] = smallwidth + (float)(t_progress)*(largewidth - smallwidth)/(float)(T_SwitchPhases);
				}
				else{
					//Otherwise this lane should stay a small width
					curWidths[i] = smallwidth;
				}
			}
		}
	}

	private void initializeHealth() {
		health = new int[NUM_LANES];
		for (int i = 0; i < NUM_LANES; i++) {
			health[i] = MAX_HEALTH;
		}
	}

	public boolean checkHealth(boolean dec) {
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
		beetleTexture = directory.getEntry("beetle", Texture.class);
		bulletTexture = directory.getEntry("bullet", Texture.class);
		starTexture = directory.getEntry("star", Texture.class);
		redTexture  = directory.getEntry("red", Texture.class);
		greenTexture = directory.getEntry("green", Texture.class);
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
	 * Starts a new game.
	 *
	 * This method creates a single player, but does nothing else.
	 *
	 * @param x Starting x-position for the player
	 * @param y Starting y-position for the player
	 */
	public void start(float x, float y, int width, int height, boolean r) {
		// Create the player's ship
//		player = new Ship();
//		player.setTexture(beetleTexture);
//		player.getPosition().set(x,y);
//
//		// Player must be in object list.
//		objects.add(player);
	//	setCoords(width, height);
		randomnotes = r;


	}

	/**
	 * Resets the game, deleting all objects.
	 */
	public void reset() {
		//player = null;
		shellCount = 0;
		objects.clear();
		initializeHealth();
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
	/**
	 * Adds a new shell to the game.
	 *
	 * A shell is generated at the top with a random horizontal position. Notice that
	 * this allocates memory to the heap.  If we were REALLY worried about performance,
	 * we would use a memory pool here.
	 *
	 * @param height Current game height
	 */
	public void addShellRandom(float height, int frame) {
		randomnotes = true;
		if(randomnotes){
			int lane = RandomController.rollInt(0,lpl-1);
			int dur = RandomController.rollInt(1, 3);
			if(frame % 250 == 0&& curP == play_phase.NOTES && !heldPresent[lane]){

				Note h = new Note(lane, Note.NType.HELD);
				h.lane = currentLane;
				heldPresent[lane] = true;
				h.setX(LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/(2*lpl) + lane*(largewidth/lpl));
				h.bx = LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/(2*lpl) + lane*(largewidth/lpl);
				h.startFrame = frame;
				h.holdFrame = 60 + (15 * dur);
				h.setY(height);
				h.by = height;
				h.tail_thickness = 15f;
				h.setTexture(greenTexture);
				h.setTailTexture(redTexture);
				objects.add(h);
				++shellCount;
			}
			if(frame%45 == 0 && curP == play_phase.NOTES){
				int det = RandomController.rollInt(0,lpl);
				if(det < 4 && !heldPresent[det]){
					Note s = new Note(det, Note.NType.BEAT);
					s.lane = currentLane;
					s.setX(LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/(2*lpl) + det*(largewidth/lpl));
					s.setTexture(redTexture);
					s.setY(height);
					s.setVX(0);
					objects.add(s);
					++shellCount;
				}
			}

			if(frame%450 == 0 && curP == play_phase.NOTES){
				int det = RandomController.rollInt(0,NUM_LANES - 1);
				if(det != currentLane){
					Note s = new Note(det, Note.NType.SWITCH);
					s.lane = det;
					s.setX(LEFTBOUND + (det * (smallwidth + inBetweenWidth) + (det > currentLane ? largewidth - smallwidth : 0)) + smallwidth/2f);
					s.setTexture(greenTexture);
					s.setY(height);
					s.setVX(0);
					objects.add(s);
					++shellCount;
				}
			}
		}
	}

	public void addShell(int lane,int line){

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
			if (o.isDestroyed()) {
				destroy(o);
			} else {
				backing.add(o);
			}
		}

		// Swap the backing store and the objects.
		// This is essentially stop-and-copy garbage collection
		Array<GameObject> tmp = backing;
		backing = objects;
		objects = tmp;
		backing.clear();
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
		switch(o.getType()) {
			case SHIP:
				//player = null;
				break;
			case NOTE:
				// Create some stars if hit on beat - more stars if more accurate
				if(((Note)o).nt == Note.NType.HELD){
					System.out.println("HELD NOTE DESTROYED");
					heldPresent[((Note)o).line] = false;
				}
				spawnStars(((Note)o).hitStatus, o.getX(), o.getY(), o.getVX(), o.getVY());
				int hpUpdate = ((Note) o).nt == Note.NType.SWITCH ? goal : currentLane;
				health[hpUpdate] += ((Note) o).hitStatus*recovery;
				health[hpUpdate] = Math.min(MAX_HEALTH, health[hpUpdate]);
				health[hpUpdate] = Math.max(0, health[hpUpdate]);
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
				backing.add(s);
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
	int goal;
	/**
	 * Whether or not a trigger for a certain line was pressed
	 */
	boolean[] triggers;


	/**
	 * The current transition progress
	 */
	int t_progress;
	/**
	 * Whether or not we have indicated we want to switch to a certain lane
	 */
	boolean[] switches;
	/**
	 * Resolves state changes into and out of the TRANSITION phase
	 */
	public void resolvePhase(InputController input, float delta){
		//Currently, this method will destroy all notes on the screen
		if(curP == play_phase.NOTES){
			//If we are currently in a note phase, detect for switch presses
			switches = input.switches();

			for(int i = 0; i < switches.length; ++i){
				//For each active switch, check if it is not the current active band member lane.
				//If it is, do nothing
				if(switches[i] && i != currentLane){
					//If it is not, initiate change to TRANSITION phase
					//First set the goal band member to the detected switch
					goal = i;
					//Change the phase to TRANSITION
					curP = play_phase.TRANSITION;
					//Start transition progress at 0
					t_progress = 0;

					for(GameObject o : objects){
						if(o.getType() == ObjectType.NOTE){
							//For all NOTE objects, check for switch notes
							//This variable is to make sure we do not attempt to destroy a switch note twice
							boolean switchTog = false;
							if(((Note)o).nt == Note.NType.SWITCH){
								if(switches[((Note)o).line]){
									//If there is a switch note on this line and within bounds, destroy it and reward HP points
									//Also set the switchTog variable to true
									if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
										//System.out.println("Good switch");
										((Note) o).hitStatus = 4;
										switchTog = true;
										o.setDestroyed(true);

									} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
										//System.out.println("switch");
										((Note) o).hitStatus = 2;
										switchTog = true;
										o.setDestroyed(true);
									}
									else {
										//System.out.println("missed switch");
										((Note) o).hitStatus = 0;
									}
								}
							}
							if(!switchTog){
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
				currentLane = goal;
				//Change phase to NOTES phase
				curP = play_phase.NOTES;
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
		if(curP == play_phase.NOTES){
			//If we are in the NOTES phase, get trigger input
			triggers = input.didTrigger();
			// Process the objects.
			for (GameObject o : objects) {
				//Objects may have been destroyed but not despawned by the resolvePhases method, which gets called before
				if(o.destroyed){
					//If so, ignore these objects
					continue;
				}
				if(o.getType() == ObjectType.NOTE){
					//If the object is a note, first update the note
					((Note)o).update(delta, frame);
					//If the note is a BEAT, detect whether we have a trigger pressed on its line
					if(((Note)o).nt == Note.NType.BEAT){
						if(triggers[((Note)o).getLine()]){
							//If the trigger is pressed while the BEAT is in appropriate bounds, destroy the note
							//and award HP points.
							//We need to return after each one so that we don't register 1 trigger click for two notes
							//that spawned close together
							//System.out.println(hitbarY + " " + o.getY() + " " + o.getRadius());
							if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								//System.out.println("Good hit");
								((Note) o).hitStatus = 2;

								o.setDestroyed(true);
								return;

							} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								//System.out.println("hit");
								((Note) o).hitStatus = 1;

								o.setDestroyed(true);
								return;
							}
							else {
								//Otherwise set  its hit value to 0
								//System.out.println("miss");
								((Note) o).hitStatus = 0;
							}
						}
					}
					else if(((Note)o).nt == Note.NType.HELD){
						//If the note is a held note, detect if we have a trigger on its  line
						if(triggers[((Note)o).line]){
							//If the bottom of the note is within bounds, spawn some stars
							//We need to return again for similar reasons to the beat notes
							if(((Note)o).by <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								System.out.println("Good hold start");
								spawnStars(3, ((Note)o).bx, ((Note)o).by, 0, Note.descentSpeed);
								return;

							} else if (((Note)o).by <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								System.out.println("hold start");
								spawnStars(1, ((Note)o).bx, ((Note)o).by, 0, Note.descentSpeed);
								return;
							}
							else{
								System.out.println("hold missed");
								((Note)o).hitStatus = 0;
							}
						}
						//Now detect if a trigger was lifted
						if(input.triggerLifted[((Note)o).line]){
							System.out.println("let go");
							//If the trigger was lifted when the tail of the held note is near the hitbarY value,
							//destroy the note and reward some HP
							if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								System.out.println("Good hold end");
								((Note)o).hitStatus = 4;
								o.setDestroyed(true);
								return;
							} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								System.out.println("hold end");

								((Note)o).hitStatus = 2;
								o.setDestroyed(true);

								return;
							}
							else{
								System.out.println("hold end missed");

							}
						}
					}
				}
				else{
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