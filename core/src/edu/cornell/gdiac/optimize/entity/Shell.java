/*
 * Shell.java
 *
 * This is a passive model, and this model does very little by itself.  
 * All of its work is done by the CollisionController or the 
 * GameplayController.  
 *
 * This separation is very important for this class because it has a lot 
 * of interactions with other classes.  When a shell dies, it emits stars.  
 * If did not move that behavior to the CollisionController,
 * then we would have to have a reference to the GameEngine in this
 * class.  Tight coupling with the GameEngine is a very bad idea, so
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
 * Model class for enemy shells.
 */
public class Shell extends GameObject {
	// Physics constants
	/** Maximum friction multiplier for collisions */
	private static final float MAX_FRICTION_MULT = 0.95f;
	/** Minimum friction multiplier for collisions */
	private static final float MIN_FRICTION_MULT = 0.85f;
	/** Maximum y-velocity on the bounce */
	private static final float MAX_MIN_Y_VELOC   = 20.0f;
	/** Minimum y-velocity on the bounce */
	private static final float MIN_MIN_Y_VELOC   = 10.0f;
	/** The effects of gravity on this shell */
	private static final float GRAVITY = 0.5f;
	/** Rescale the size of a shell */
	private static final float SHELL_SIZE_MULTIPLE = 5.0f;
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int   NUM_ANIM_FRAMES = 4;

	/** Friction multiplier for this shell */
	//private float friction;
	/** Minimum Y velocity for this sheall */
	private float minvelocy;
	/** Current animation frame for this shell */
	private float animeframe;
	
	/** To measure if we are damaged */
	private boolean damaged;
	/** The backup texture to use if we are damaged */
	private Texture dmgTexture;

	public int hitStatus;

	/** line the note is one */
	public int line;
	/**
	 * Returns the type of this object.
	 *
	 * We use this instead of runtime-typing for performance reasons.
	 *
	 * @return the type of this object.
	 */
	public ObjectType getType() {
		return ObjectType.SHELL;
	}

	/**
	 * Returns the minimum y-velocity on a bounce
	 *
	 * @return the minimum y-velocity on a bounce
	 */
	public float getMinVY() {
		return minvelocy;
	}

	public float getRadius(){
		return super.getRadius() * SHELL_SIZE_MULTIPLE;
	}
	
	/**
	 * Sets whether this shell is destroyed.
	 *
	 * Shells have to be shot twice to be destroyed.  This getter checks whether this 
	 * shell should be destroyed or it should just change colors.
	 *
	 * @param value whether this shell is destroyed
	 */
	public void setDestroyed(boolean value) {
		destroyed = true;
	}
	
	/**
	 * Initialize shell with trivial starting position.
	 */
	public Shell(int line) {
		// Set minimum Y velocity for this shell
		this.line = line;
		minvelocy = -2f;
		hitStatus = 0;
		animeframe = 0.0f;
	}

	public int getHitVal(){
		return hitStatus;
	}

	public int getLine() { return line;}

	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
		radius = animator.getRegionHeight() / 2.0f;
	}
	
	public void setDamagedTexture(Texture texture) {
		dmgTexture = texture;
	}
	
	public Texture getDamagedTexture() {
		return dmgTexture;
	}
	
	/**
	 * Updates the animation frame and velocity of this shell.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float delta) {
		// Call superclass's run
		super.update(delta);

		// Increase animation frame
		animeframe += ANIMATION_SPEED;
		if (animeframe >= NUM_ANIM_FRAMES) {
			animeframe -= NUM_ANIM_FRAMES;
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
		animator.setFrame((int)animeframe);
		canvas.draw(animator, Color.WHITE, origin.x, origin.y, position.x, position.y, 
					0.0f, SHELL_SIZE_MULTIPLE, SHELL_SIZE_MULTIPLE);
	}

}