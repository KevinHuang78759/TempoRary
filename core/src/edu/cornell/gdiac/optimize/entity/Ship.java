/*
 * Ship.java
 *
 * This is a passive model, and this model does very little by itself.  
 * All of its work is done by the CollisionController or the 
 * GameplayController. 
 * 
 * This separation is very important for this class because it has a lot 
 * of interactions with other classes.  When a ship fires, it creates  
 * bullets. If did not move that behavior to the GameplayController,
 * then we would have to have a reference to the GameEngine in this
 * class. Tight coupling with the GameEngine is a very bad idea, so
 * we have separated this out.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.optimize.entity;

import edu.cornell.gdiac.optimize.*;
import edu.cornell.gdiac.util.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Model class for the player ship.
 */
public class Ship extends GameObject {
	/// CONSTANTS
	/** Horizontal speed **/
	private static final float BEETLE_SPEED = 4.0f;
	/** How long between shots */
	private static final int COOLDOWN_TIME  = 8;
	/** Cooldown bonus if we don't fire */
	private static final int COOLDOWN_BONUS = 3;
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int   NUM_ANIM_FRAMES = 2;
	//#region REMOVE ME
	/** Number of kills for a power up */
	private static final int POWER_KILL = 20;
	/** Length of time of power up */
	private static final int POWER_TIME = 600; // 5 seconds
	//#endregion
	
	/// ATTRIBUTES
	/** The left/right movement of the player this turn */
	private float movement = 0.0f;
	/** Whether this ship is currently firing */
	private boolean firing = false;
	/** How long before ship can fire again */
	private int cooldown;
	/** Current animation frame for this ship */
	private float animeframe;
	//#region REMOVE ME
	// Code to allow power-ups
	/** Whether we are currently powered up */
	private boolean powered = false;
	/** Number of kills so far (to acquire power-up) */
	private int killcount = 0;
	/** The current amount of time with the power-up */
	private int powertime;
	/** The number of points this ship has*/
	int points;

	/** Is the ship is invincible*/
	boolean invincible;
	//#endregion

	
	/**
	 * Returns the type of this object.
	 *
	 * We use this instead of runtime-typing for performance reasons.
	 *
	 * @return the type of this object.
	 */
	public ObjectType getType() {
		return ObjectType.SHIP;
	}

	/**
	 * Initialize a ship with trivial starting position.
	 */
	public Ship() {
		cooldown   = 0;
		animeframe = 0.0f;
		invincible = false;
		points = 0;
	}

	public boolean isInvincible(){
		return invincible;
	}

	public void setInvincible(boolean t){
		invincible = t;
	}
	public void setTexture(Texture texture) {

		animator = new FilmStrip(texture,1,2,2);
		radius = animator.getRegionHeight() / 2.0f;
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
	}
	
	/**
	 * Updates the animation frame and position of this ship.
	 *
	 * Notice how little this method does.  It does not actively fire the weapon.  It 
	 * only manages the cooldown and indicates whether the weapon is currently firing. 
	 * The result of weapon fire is managed by the GameplayController.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float delta) {
		// Call superclass's update
		super.update(delta);

		// Increase animation frame, but only if trying to move
		if (movement != 0.0f) {
			animeframe += ANIMATION_SPEED;
			if (animeframe >= NUM_ANIM_FRAMES) {
				animeframe -= NUM_ANIM_FRAMES;
			}
			position.x += movement * BEETLE_SPEED;
		}

		// Decrease time until ship can fire again
		if (cooldown > 0) {
			cooldown--;
		}

		if (!firing) {
			// Cool down faster when not holding space
			cooldown -= COOLDOWN_BONUS;
		}

	}

	/**
	 * Draws this shell to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects 
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas) {
		float x = animator.getRegionWidth()/2.0f;
		float y = animator.getRegionHeight()/2.0f;
		animator.setFrame((int)animeframe);
		canvas.draw(animator, Color.WHITE, x, y, position.x, position.y, 0.0f, 1.0f, 1.f);
	}
	
}
