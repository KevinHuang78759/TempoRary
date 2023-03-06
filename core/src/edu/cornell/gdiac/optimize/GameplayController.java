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

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.optimize.entity.*;
import edu.cornell.gdiac.optimize.GameObject.ObjectType;

import java.util.Random;

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

	/** THe amount of health lost when missing a note */
	private static final int MISS_HIT_HEALTH = 3;

	/** Reference to player (need to change to allow multiple players) */
	//private Ship player;
	/** Shell count for the display in window corner */
	private int shellCount;

	/** health of lines */
	private int[] health;

	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<GameObject> objects;
	/** The backing set for garbage collection */
	private Array<GameObject> backing;

	HashMap<Integer, Shell> noteCoords = new HashMap<>();

	private void setCoords(float width, float height) {
		// note appears every two seconds if we have a 30 second loop
		final int NUM_NOTES = 60;

		// 1800
		for (int i = 0; i < NUM_NOTES; i++) {
			Shell b = new Shell (i%4);
			b.setX(width/8 + (i % 4) * width/4);
			b.setTexture(redTexture);
			b.setY(height);
			b.setVX(0);
			b.setVY(-5f);
			noteCoords.put(i * 30, b);
		}
	}


	private int highscore;

	private boolean hsflag;

	public int lane;
	/**
	 * Creates a new GameplayController with no active elements.
	 */
	public GameplayController() {
		//player = null;
		shellCount = 0;
		health = new int[4];
		health[0] = 100;
		health[1] = 100;
		health[2] = 100;
		health[3] = 100;
		objects = new Array<GameObject>();
		backing = new Array<GameObject>();
		highscore = 0;
		hsflag = false;
		lane = 0;
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

	public boolean newhsreached(){
		return hsflag;
	}
	public int gethighscore(){
		return highscore;
	}
	/**
	 * Starts a new game.
	 *
	 * This method creates a single player, but does nothing else.
	 *
	 * @param x Starting x-position for the player
	 * @param y Starting y-position for the player
	 */
	public void start(float x, float y, int width, int height) {
		// Create the player's ship
//		player = new Ship();
//		player.setTexture(beetleTexture);
//		player.getPosition().set(x,y);
//
//		// Player must be in object list.
//		objects.add(player);
		setCoords(width,height);
		System.out.println(noteCoords);
	}

	/**
	 * Resets the game, deleting all objects.
	 */
	public void reset() {
		//player = null;
		shellCount = 0;
		objects.clear();
		hsflag = false;
	}

	/**
	 * Adds a new shell to the game.
	 *
	 * A shell is generated at the top with a random horizontal position. Notice that
	 * this allocates memory to the heap.  If we were REALLY worried about performance,
	 * we would use a memory pool here.
	 *
	 * @param width  Current game width
	 * @param height Current game height
	 */
	public void addShell(float width, float height, int frame) {

//		if(shellCount > 4){
//			return;
//		}
//		int lane = RandomController.rollInt(0,3);

		Shell b;

		b = noteCoords.get(frame);

//		b = new Shell(lane);
//		b.setX(width/8 + lane * width/4);
//		// Add a new shell
//		if (RandomController.rollInt(0,1) == 0) {
//			b.setTexture(redTexture);
//		} else {
//			b.setTexture(greenTexture);
//		}

		// Position the shella
//		b.setY(height);
//		b.setVX(0);
//		b.setVY(-5f);
		if (b != null) {
			objects.add(b);
			shellCount++;
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
			case SHELL:
				// Create some stars if hit on beat - more stars if more accurate

				if(((Shell) o).getHitVal() == 1){
					health[((Shell) o).getLine()] += WEAK_HIT_HEALTH;
					for (int j = 0; j < 3; j++) {
						Star s = new Star();
						s.setTexture(starTexture);
						s.getPosition().set(o.getPosition());
						float vx = o.getVX() * RandomController.rollFloat(MIN_STAR_FACTOR, MAX_STAR_FACTOR)
								+ RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
						float vy = o.getVY() * RandomController.rollFloat(MIN_STAR_FACTOR, MAX_STAR_FACTOR)
								+ RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
						s.getVelocity().set(vx,vy);
						backing.add(s);
					}
				}
				else if(((Shell) o).getHitVal() == 2) {
					health[((Shell) o).getLine()] += STRONG_HIT_HEALTH;
					for (int j = 0; j < 9; j++) {
						Star s = new Star();
						s.setTexture(starTexture);
						s.getPosition().set(o.getPosition());
						float vx = o.getVX() * RandomController.rollFloat(MIN_STAR_FACTOR, MAX_STAR_FACTOR)
								+ RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
						float vy = o.getVY() * RandomController.rollFloat(MIN_STAR_FACTOR, MAX_STAR_FACTOR)
								+ RandomController.rollFloat(MIN_STAR_OFFSET, MAX_STAR_OFFSET);
						s.getVelocity().set(vx, vy);
						backing.add(s);
					}
				}
				else if(((Shell) o).getHitVal() == 0) {
					health[((Shell) o).getLine()] -= MISS_HIT_HEALTH;
				}

				shellCount--;
				break;
			default:
				break;
		}
	}

	/**
	 * Resolve the actions of all game objects (player and shells)
	 *
	 * You will probably want to modify this heavily in Part 2.
	 *
	 * @param input  Reference to the input controller
	 * @param delta  Number of seconds since last animation frame
	 */
	boolean trigger;
	public void resolveActions(InputController input, float delta, float width, float height) {
		// Process the player
//		if (player != null) {
//			resolvePlayer(input,delta);
//			if(player.getPoints() > highscore){
//				hsflag = true;
//				highscore = player.getPoints();
//			}
//		}
		boolean[] switches = input.switches();

		lane = Math.max(Math.min(3, lane + (switches[2] ? 1 : 0) - (switches[1] ? 1 : 0)), 0);

		trigger = input.isTrigger();

		// Process the other (non-ship) objects.
		for (GameObject o : objects) {
			o.update(delta);
			if(o.getType() == ObjectType.SHELL){
				if(trigger && lane == ((Shell)o).getLine()){
					System.out.println(height/3f + " " + o.getY() + " " + o.getRadius());

					if(o.getY() <= (height/3f - o.getRadius()/4f) && o.getY() >= (height/3f - 3*o.getRadius()/4f)){
						System.out.println("Good hit");
						((Shell) o).hitStatus = 2;
						o.setDestroyed(true);

					} else if (o.getY() <= (height/3f + o.getRadius()/3f) && o.getY() >= (height/3f - 7*o.getRadius()/3f)) {
						System.out.println("hit");
						((Shell) o).hitStatus = 1;
						o.setDestroyed(true);

					}
					else {
						System.out.println("miss");
						((Shell) o).hitStatus = 0;
					}
				}
			}

		}
	}

	/**
	 * Process the player's actions.
	 *
	 * Notice that firing bullets allocates memory to the heap.  If we were REALLY
	 * worried about performance, we would use a memory pool here.
	 *
	 * @param input  Reference to the input controller
	 * @param delta  Number of seconds since last animation frame
	 */
//	public void resolvePlayer(InputController input, float delta) {
//		player.setMovement(input.getMovement());
//		player.setFiring(input.didFire());
//		player.update(delta);
//		if (!player.isFiring()) {
//			return;
//		}
//
//		// Create a new bullet
//		Bullet b = new Bullet(player);
//		b.setTexture(bulletTexture);
//		b.setX(player.getX());
//		b.setY(player.getY()+player.getRadius()+BULLET_OFFSET);
//		b.setVY(BULLET_SPEED);
//		backing.add(b); // Bullet added NEXT frame.
//
//		// Prevent player from firing immediately afterwards.
//		player.resetCooldown();
//	}
}