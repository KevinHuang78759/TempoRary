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

	/** Maximum amount of health */
	private static final int NUM_LANES = 4;

	/** Number of notes present in repeating rhythm */
	private static final int NUM_NOTES = 60;

	/** Reference to player (need to change to allow multiple players) */
	//private Ship player;
	/** Note count for the display in window corner */
	private int shellCount;

	/** health of lines */
	private int[] health;

	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<GameObject> objects;
	/** The backing set for garbage collection */
	private Array<GameObject> backing;

	HashMap<Integer, Note> noteCoords = new HashMap<>();

	int currentLane;

	final int recovery = 3;

//	private void setCoords(float width, float height) {
//		// note appears every two seconds if we have a 30 second loop
//
//		// 1800
//		for (int i = 0; i < NUM_NOTES; i++) {
//			Note s = new Note(i%4, Note.NType.BEAT);
//			s.setX(width/8 + (i % 4) * width/4);
//			s.setTexture(redTexture);
//			s.setY(height);
//			s.setVX(0);
//			s.setVY(-5f);
//			noteCoords.put(i * 30, s);
//		}
//	}
//

	private boolean hsflag;

	/**
	 * Creates a new GameplayController with no active elements.
	 */
	public boolean randomnotes;
	public GameplayController(boolean rn) {
		shellCount = 0;
		initializeHealth();
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		hsflag = false;
		currentLane = 0;
		randomnotes = rn;
	}

	 float LEFTBOUND;
	 float RIGHTBOUND;
	 float TOPBOUND;
	 float BOTTOMBOUND;

	 float smallwidth;
	 float largewidth;
	 float inBetweenWidth;

	float hitbarY;

	float hpwidth;
	float hpbet;
	public GameplayController(boolean rn, float width, float height){
		shellCount = 0;
		initializeHealth();
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		hsflag = false;
		randomnotes = true;
		LEFTBOUND = width/10f;
		RIGHTBOUND = 9*width/10f;
		TOPBOUND = 19f*height/20f;
		BOTTOMBOUND = height/5f;
		smallwidth = width/15;
		inBetweenWidth = smallwidth/4f;
		largewidth = 8f*width/10f - 3*(smallwidth + inBetweenWidth);
		currentLane = 0;
		hitbarY = BOTTOMBOUND + 3*height/20f;
		hpwidth = (RIGHTBOUND - LEFTBOUND)*4/19;
		hpbet = (RIGHTBOUND - LEFTBOUND - 4*hpwidth)/3f;
		heldPresent = new boolean[4];
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
	 * Returns a reference to the currently active player.
	 *
	 * This property needs to be modified if you want multiple players.
	 *
	 * @return a reference to the currently active player.
	 */
//	public Ship getPlayer() {
//		return player;
//	}

	/**
	 * Returns true if the currently active player is alive.
	 *
	 * This property needs to be modified if you want multiple players.
	 *
	 * @return true if the currently active player is alive.
	 */
	public boolean isAlive() {
		return true;
	}

	/**
	 * Returns the number of shells currently active on the screen.
	 *
	 * @return the number of shells currently active on the screen.
	 */
	public int getShellCount() {
		return shellCount;
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

	public boolean newhsreached(){
		return hsflag;
	}

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
	boolean[] heldPresent;
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
			if(frame % 250 == 0&& curP == play_phase.NOTES){
				int lane = RandomController.rollInt(0,3);
				int dur = RandomController.rollInt(1, 3);
				Note h = new Note(lane, Note.NType.HELD);
				heldPresent[lane] = true;
				h.setX(LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/8f + lane*(largewidth/4f));
				h.bx = LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/8f + lane*(largewidth/4f);
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
				int det = RandomController.rollInt(0,4);
				if(det < 4 && !heldPresent[det]){
					Note s = new Note(det, Note.NType.BEAT);
					s.setX(LEFTBOUND + currentLane*(inBetweenWidth + smallwidth) + largewidth/8f + det*(largewidth/4f));
					s.setTexture(redTexture);
					s.setY(height);
					s.setVX(0);
					objects.add(s);
					++shellCount;
				}
			}

			if(frame%450 == 0 && curP == play_phase.NOTES){
				int det = RandomController.rollInt(0,3);
				if(det != currentLane){
					Note s = new Note(det, Note.NType.SWITCH);
					s.setX(LEFTBOUND + (det * (smallwidth + inBetweenWidth) + (det > currentLane ? largewidth - smallwidth : 0)) + smallwidth/2f);
					s.setTexture(greenTexture);
					s.setY(height);
					s.setVX(0);
					objects.add(s);
					++shellCount;
				}
			}


		}
//		if(!randomnotes){
//			//add notes in fixed pattern
//			Note s = noteCoords.get(frame);
//			if (s != null) {
////			s.setDestroyed(false);
//				objects.add(s);
//				shellCount++;
//
//				if (shellCount == NUM_NOTES) {
//					setCoords(width, height);
//					shellCount = 0;
//				}
//			}
//		}else{
//			//add notes randomly - to a random lane with fixed probability
//			if(frame%25 == 0){
//				int det = RandomController.rollInt(0,4);
//				if(det < 4){
//					Note s = new Note(det);
//					s.setX(width/8 + det * width/4);
//					s.setTexture(redTexture);
//					s.setY(height);
//					s.setVX(0);
//					s.setVY(-5f);
//					objects.add(s);
//					++shellCount;
//				}
//			}
//		}

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

	public enum play_phase{
		NOTES,
		TRANSITION
	}
	play_phase curP = play_phase.NOTES;
	int T_SwitchPhases = 20;
	int goal;
	/**
	 * Resolve the actions of all game objects (player and shells)
	 *
	 * You will probably want to modify this heavily in Part 2.
	 *
	 * @param input  Reference to the input controller
	 * @param delta  Number of seconds since last animation frame
	 */
	boolean[] triggers = new boolean[4];

	int t_progress;
	boolean[] switches;
	public void resolvePhase(InputController input, float delta){
		if(curP == play_phase.NOTES){
			switches = input.switches();
			for(int i = 0; i < switches.length; ++i){
				if(switches[i] && i != currentLane){
					goal = i;
					curP = play_phase.TRANSITION;
					t_progress = 0;
					for(GameObject o : objects){
						if(o.getType() == ObjectType.NOTE){
							boolean switchTog = false;
							if(((Note)o).nt == Note.NType.SWITCH){
								if(switches[((Note)o).line]){

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
								o.setDestroyed(true);
							}
						}
					}
					break;
				}
			}
		}
		else{
			if (t_progress == T_SwitchPhases){
				currentLane = goal;
				curP = play_phase.NOTES;
			}
		}
	}

	public void resolveActions(InputController input, float delta, int frame) {
		if(curP == play_phase.NOTES){
			triggers = input.didTrigger();
			// Process the objects.
			for (GameObject o : objects) {
				if(o.destroyed){
					continue;
				}
				if(o.getType() == ObjectType.NOTE){
					((Note)o).update(delta, frame);
					if(((Note)o).nt == Note.NType.BEAT){
						if(triggers[((Note)o).getLine()]){
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
								//System.out.println("miss");
								((Note) o).hitStatus = 0;
							}
						}
					}
					else if(((Note)o).nt == Note.NType.HELD){
						if(triggers[((Note)o).line]){
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

						if(input.triggerLifted[((Note)o).line]){
							System.out.println("let go");
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