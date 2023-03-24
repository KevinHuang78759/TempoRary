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

import com.badlogic.gdx.graphics.Color;
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
	int curframe;
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

	/** Number of band member lanes */
	int NUM_LANES;

	/** Reference to player (need to change to allow multiple players) */
	//private Ship player;
	/** Note count for the display in window corner */
	private int shellCount;


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
	 * The base hp increment. Hp will incrememnt on a destroyed note by the product of this value and its hit status
	 */
	final int recovery = 3;

	BandMember[] bms;

	/**
	 * Indicates whether or not we want to use randomly generated notes
	 */
	public boolean randomnotes;

	float noteSpawnY;
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
	public GameplayController(int lanes, int linesPerLane, float width, float height){
		NUM_LANES = lanes;
		shellCount = 0;
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
		activeBM = 0;
		//Have the y value be a bit above the bottom of the play area, but not too close
		hitbarY = BOTTOMBOUND + 3*height/20f;
		//There ar e NUM_LANES hp bars, and the width between each one shall be 1/4 their length
		//The width will then be 1/(5NUMLANES/4 - 1/4) of the total available width
		hpwidth = (RIGHTBOUND - LEFTBOUND)/(5f*NUM_LANES/4f - 0.25f);
		//Width between each HP bar shall be 1/4 of the width of an HP bar
		hpbet = hpwidth/4f;
		//instantiate other variables
		heldPresent = new boolean[linesPerLane];
		triggers = new boolean[linesPerLane];
		lpl = linesPerLane;
		noteSpawnY = TOPBOUND + smallwidth/2;
		noteDieY = BOTTOMBOUND - smallwidth/2;
		bms = new BandMember[NUM_LANES];
		triggers = new boolean[linesPerLane];
		switches = new boolean[lanes];
	}

	public void setupBandMembers(Color[] c){

		float XCoor = LEFTBOUND;
		for(int i = 0; i < NUM_LANES; ++i){
			bms[i] = new BandMember();
			bms[i].borderColor = c[i];
			bms[i].BL.x = XCoor;
			bms[i].BL.y = BOTTOMBOUND;
			bms[i].width = i == 0 ? largewidth : smallwidth;
			bms[i].lineHeight = i == 0 ? TOPBOUND - BOTTOMBOUND : 0;
			bms[i].height = TOPBOUND - BOTTOMBOUND;
			bms[i].numLines = lpl;
			bms[i].maxComp = MAX_HEALTH;
			bms[i].curComp = MAX_HEALTH;
			XCoor += bms[i].width + inBetweenWidth;
		}
	}

	public void checkDeadNotes(){
		for(int i = 0; i < bms.length; ++i){
			for(Note n : bms[i].hitNotes){
				if(n.y < noteDieY && n.hitStatus == 0){
					n.hitStatus = -2;
					n.destroyed = true;
				}
				if(n.destroyed){
					if(i == activeBM || i == goalBM){
						bms[i].compUpdate(n.hitStatus);
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
	 * Starts level
	 */
	public void start() {
		setupBandMembers(new Color[]{Color.BLUE, Color.GOLDENROD, Color.CORAL,Color.MAROON});
		activeBM = 0;
		curframe = 0;
		addNoteRandom();
	}
	public void update(){
		checkDeadNotes();
		for(BandMember bm : bms){
			bm.updateNotes(curframe);
			bm.spawnNotes(curframe);
			if(curframe%120 == 1){
				bm.compUpdate(-1);
			}
		}
		for(GameObject o : objects){
			o.update(0f);
		}

		++curframe;
	}



	public void updateBMCoords(){
		if(curP == play_phase.NOTES){
			//If we are in the notes phase, we set the width of the active lane to goal, and everything else to small
			//We also set the line height of everything to 0 except for the active lane
			float XCoord = LEFTBOUND;
			for(int i = 0; i < bms.length; ++i){
				bms[i].BL.x = XCoord;
				if(i == activeBM){
					bms[i].width = largewidth;
					bms[i].lineHeight = TOPBOUND - BOTTOMBOUND;
				}
				else{
					bms[i].width = smallwidth;
					bms[i].lineHeight = 0f;
				}
				XCoord += bms[i].width + inBetweenWidth;
			}
		}
		else{
			//Otherwise we must be in transition
			float progressFrac = t_progress/(float)T_SwitchPhases;
			float XCoord = LEFTBOUND;
			for(int i = 0; i < bms.length; ++i){
				bms[i].BL.x = XCoord;
				if(i == activeBM){
					bms[i].width = (largewidth - smallwidth)*(1-progressFrac) + smallwidth;
					bms[i].lineHeight = (TOPBOUND - BOTTOMBOUND)*(1-progressFrac);
				}
				else if(i == goalBM){
					bms[i].width = (largewidth - smallwidth)*(progressFrac) + smallwidth;
					bms[i].lineHeight = (TOPBOUND - BOTTOMBOUND)*(progressFrac);
				}
				else{
					bms[i].width = smallwidth;
					bms[i].lineHeight = 0f;
				}
				XCoord += bms[i].width + inBetweenWidth;
			}
		}
	}

	/**
	 * Resets the game, deleting all objects.
	 */
	public void reset() {
		//player = null;
		curframe = 0;
		objects.clear();
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
	 * Adds a new shell to the game
	 */
	public void addNoteRandom() {
		//We need to decide which band member gets what type of note at what point in time
		//For now, lets just do normal and switch notes
		for(int frame = 50; frame < 5000; frame += 25){
			for(int i = 0; i < bms.length; ++i){
				float det = RandomController.rollFloat(0f,1f);
				if(det < 0.25){
					//add a hit note
					int l = RandomController.rollInt(0,lpl - 1);
					Note n = new Note(l, Note.NType.BEAT,frame);
					n.y = noteSpawnY;
					n.setTexture(redTexture);
					bms[i].allNotes.addLast(n);
				}
			}
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
		for (BandMember bm : bms) {
			bm.garbageCollect();
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
	public boolean[] switches;
	public boolean[] triggers;

	public void handleActions(InputController input){
		switches = input.switches();
		triggers = input.didTrigger();
		boolean[] lifted = input.triggerLifted;
		//First handle the switches
		if(curP == play_phase.NOTES){
			for(int i = 0; i < switches.length; ++i){
				if(switches[i] && i != activeBM){
					for(Note n : bms[i].switchNotes){
						float dist = Math.abs(hitbarY - n.y)/n.h;
						if(dist < 1.5){
							n.hitStatus = dist < 0.75 ? 4 : 2;
							spawnStars(n.hitStatus, n.x, n.y, 0, n.vy);
							n.destroyed = true;
						}
					}
					goalBM = i;
					curP = play_phase.TRANSITION;
					t_progress = 0;
					return;
				}
			}
		}
		else{
			//Otherwise we must be in transition
			++t_progress;

			if(t_progress == T_SwitchPhases){
				curP = play_phase.NOTES;
				activeBM = goalBM;
			}
			updateBMCoords();
		}
		//Now check for hit and held notes
		//This array tells us if a hit has already been registered in this frame for the ith bm.
		//We do not want one hit to count for two notes that are close together.
		boolean[] hitReg = new boolean[triggers.length];
		int checkBM = curP == play_phase.NOTES ? activeBM : goalBM;
		for(Note n : bms[checkBM].hitNotes){
			if(n.nt == Note.NType.BEAT){
				if(triggers[n.line] && !hitReg[n.line]){
					float dist = Math.abs(hitbarY - n.y)/n.h;
					if(dist < 1.5){
						n.hitStatus = dist < 0.75 ? 2 : 1;
						spawnStars(n.hitStatus, n.x, n.y, 0, n.vy);
						n.destroyed = true;
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
						n.destroyed = true;
					}
				}
			}
		}
	}
}