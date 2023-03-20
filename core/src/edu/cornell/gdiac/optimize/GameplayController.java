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

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Music;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.optimize.entity.*;
import edu.cornell.gdiac.optimize.GameObject.ObjectType;

import java.util.ArrayList;

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

	/** Maximum amount of health */
	final int MAX_HEALTH = 30;

	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<GameObject> objects;
	/** The backing set for garbage collection */
	private Array<GameObject> backing;
	/** Active lane currently */
	int currentLane;

	final int recovery = 3;

	private boolean hsflag; // TODO : ??????????

	/**
	 * Creates a new GameplayController with no active elements.
	 */
	public boolean randomnotes;

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
		hsflag = false;
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();

		// TODO: WIDTHS???? DEFINE THESE PLS
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

		activeNotes = new ArrayList<Fish>();
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
	public void populate(AssetDirectory directory)
	{
		// gather assets
		beetleTexture = directory.getEntry("beetle", Texture.class);
		bulletTexture = directory.getEntry("bullet", Texture.class);
		starTexture = directory.getEntry("star", Texture.class);
		redTexture  = directory.getEntry("red", Texture.class);
		greenTexture = directory.getEntry("green", Texture.class);
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
		level = new Level(levelData);
		bandMembers = level.getBandMembers();
		musicController = new MusicController(music, level, bandMembers);

		//notes = new ArrayList<ArrayList<Fish>>();

		/*for(BandMember bandMember : bandMembers){
			notes.add(bandMember.getNotes());
			for(Fish o : bandMember.getNotes()){
				objects.add(o);
			}
		}*/

		// TODO: THIS IS NOT EFFICIENT AT ALL LMAO
		
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

	/** get bandMembers */
	public BandMember[] getBandMembers() {
		return bandMembers;
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

		// play music
		//music.play();
	}

	/** Update competency of all band members.
	 *
	 * @return true if one of them died, false if not
	 *
	 * */
	public boolean checkAllCompetencies(boolean decreasing){
		for(BandMember bandMember : bandMembers){
			if(bandMember.updateHealth(decreasing)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Resets the game, deleting all objects and setting their parameters to their default values.
	 * todo: RESET FUNCTIONS IN LEVEL, FISH, BAND MEMBER
	 *
	 */
	public void reset() {
		//player = null;
		//shellCount = 0;
		objects.clear();
		//initializeHealth();
	}
	boolean[] heldPresent;

	/** Add new notes to the game using MusicController
	 *
	 * @param height current game height
	 * @param frame frame / tick?? idk actually lol*/
	public void updateNotes(float delta, float height, int frame){
		// check with music controller
		ArrayList<Fish> newNotes = musicController.update(currentLane, delta);

		if(newNotes.size() > 0){
			for(Fish note : newNotes){
				activeNotes.add(note);
				note.setPosition(height, currentLane, redTexture, smallwidth, largewidth, inBetweenWidth, LEFTBOUND);
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
	public void addShellRandom(float height, int frame) {
		randomnotes = true;
		if(randomnotes){
			/*if(frame % 250 == 0&& curP == play_phase.NOTES){
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
			}*/
			/*if(frame%45 == 0 && curP == play_phase.NOTES){
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
			}*/
			/*if(frame%450 == 0 && curP == play_phase.NOTES){
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
			}*/


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
			case FISH:
				// Create some stars if hit on beat - more stars if more accurate
				/*if(((Fish)o).getNoteType() == Fish.NoteType.HELD){
					System.out.println("HELD NOTE DESTROYED");
					heldPresent[((Fish)o).line] = false;
				}*/

				spawnStars(((Fish)o).hitStatus, o.getX(), o.getY(), o.getVX(), o.getVY());

				int hpUpdate = ((Fish) o).getNoteType() == Fish.NoteType.SWITCH ? goalLaine : currentLane;
				if(hpUpdate == 1) {bandMembers[currentLane].addHealth();}

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

	public enum playPhase {
		PLAYING,
		TRANSITION
	}
	playPhase currentPhase = playPhase.PLAYING;
	int T_SwitchPhases = 20;
	int goalLaine;
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

	/** Resolve what playPhase we are in. PLAYING or TRANSITIONING ?
	 * TODO: is this description right, Kevin?
	 * */
	public void resolvePhase(InputController input, float delta){
		if(currentPhase == playPhase.PLAYING){
			switches = input.switches();
			// If switch was made, check if there was a switch note present. this will increase health by a bit TODO
			for(int i = 0; i < switches.length; ++i) {
				if(switches[i] && i != currentLane){
					goalLaine = i;
					currentPhase = playPhase.TRANSITION;
					t_progress = 0;

					// Check switch notes amongst objects TODO is this the most efficient way to do this?
					for(GameObject o : objects){
						if(o.getType() == ObjectType.FISH){
							boolean switchToggle = false;
							// If a switch note type, determine accuracy of switch + update switchToggle
							if(((Fish)o).getNoteType() == Fish.NoteType.SWITCH){
								if(switches[((Fish)o).line]){
									if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
										//System.out.println("Good switch");
										((Fish) o).hitStatus = 4;
										switchToggle = true;
										o.setDestroyed(true);

									} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
										//System.out.println("switch");
										((Fish) o).hitStatus = 2;
										switchToggle = true;
										o.setDestroyed(true);
									}
									else {
										//System.out.println("missed switch");
										((Fish) o).hitStatus = 0;
									}
								}
							}
							// if didn't switch, set note to be destroyed.
							if(!switchToggle){
								o.setDestroyed(true);
							}
						}
					}
					break;
				}
			}
		}
		else {
			if (t_progress == T_SwitchPhases){
				currentLane = goalLaine;
				currentPhase = playPhase.PLAYING;
			}
		}
	}


	public void resolveActions(InputController input, float delta, int frame) {
		if(currentPhase == playPhase.PLAYING){
			triggers = input.didTrigger();
			// Process the objects.
			for (GameObject o : objects) {
				if(o.destroyed){
					continue;
				}
				if(o.getType() == ObjectType.FISH){
					((Fish)o).update(delta, frame);

					if(((Fish)o).getNoteType() == Fish.NoteType.SINGLE){
						if(triggers[((Fish)o).getLane()]){
							//System.out.println(hitbarY + " " + o.getY() + " " + o.getRadius());
							if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								//System.out.println("Good hit");
								((Fish) o).hitStatus = 2;

								o.setDestroyed(true);
								return;

							} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								//System.out.println("hit");
								((Fish) o).hitStatus = 1;

								o.setDestroyed(true);
								return;
							}
							else {
								//System.out.println("miss");
								((Fish) o).hitStatus = 0;
							}
						}
					}
					else if(((Fish)o).getNoteType() == Fish.NoteType.HOLD){
						/*if(triggers[((Fish)o).line]){
							if(((Fish)o).by <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								System.out.println("Good hold start");
								spawnStars(3, ((Fish)o).bx, ((Fish)o).by, 0, Fish.descentSpeed);
								return;

							} else if (((Fish)o).by <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								System.out.println("hold start");
								spawnStars(1, ((Fish)o).bx, ((Fish)o).by, 0, Fish.descentSpeed);
								return;
							}
							else{
								System.out.println("hold missed");
								((Fish)o).hitStatus = 0;
							}
						}

						if(input.triggerLifted[((Fish)o).line]){
							System.out.println("let go");
							if(o.getY() <= (hitbarY + o.getRadius()/4f) && o.getY() >= (hitbarY - o.getRadius()/4f)){
								System.out.println("Good hold end");
								((Fish)o).hitStatus = 4;
								o.setDestroyed(true);
								return;
							} else if (o.getY() <= (hitbarY + o.getRadius()) && o.getY() >= (hitbarY - o.getRadius())) {
								System.out.println("hold end");

								((Fish)o).hitStatus = 2;
								o.setDestroyed(true);

								return;
							}
							else{
								System.out.println("hold end missed");

							}
						}*/
					}

				}
				else {
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