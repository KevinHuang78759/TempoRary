/*
 * Particle.java
 *
 * This is a passive model, and this model does very little by itself.  
 * The CollisionController does most of the hard work.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.temporary.*;
import com.badlogic.gdx.graphics.*;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Model class for particles caused by note hits.
 */
public class Particle {
	/** Mean life-expectancy of a particle */
	private static final int PARTICLE_AGE = 30;
	/** Variance of particle ages */
	private static final int AGE_RANGE = 10;

	/** Current age of particle.  Deleted when reach 0. */
	private int age;
	/** Current angle of particle, as they can rotate */
	private float angle;

	// Attributes for all game objects
	/** Object position (centered on the texture middle) */
	protected Vector2 position;
	/** Object velocity vector */
	protected Vector2 velocity;
	/** Reference to texture origin */
	protected Vector2 origin;
	/** Radius of the object (used for collisions) */
	protected float radius;
	/** Whether or not the object should be removed at next timestep. */
	protected boolean destroyed;
	/** CURRENT image for this object. May change over time. */
	protected FilmStrip animator;

	protected FilmStrip tail;

	/**
	 * Initialize particle with trivial starting position.
	 */
	public Particle() {
		// Particles die over time
		position = new Vector2(0.0f, 0.0f);
		velocity = new Vector2(0.0f, 0.0f);
		radius = 0.0f;
		destroyed = false;
		age = RandomController.rollInt(PARTICLE_AGE - AGE_RANGE, PARTICLE_AGE + AGE_RANGE);
	}

	public void setAge(int dieAge){
		age = dieAge;
	}

	// ACCESSORS
	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,1,1);
		radius = animator.getRegionHeight() / 2.0f;
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
	}

	public Texture getTexture() {
		return animator == null ? null : animator.getTexture();
	}

	/**
	 * Returns the position of this object (e.g. location of the center pixel)
	 *
	 * The value returned is a reference to the position vector, which may be
	 * modified freely.
	 *
	 * @return the position of this object
	 */
	public Vector2 getPosition() {
		return position;
	}

	/**
	 * Returns the x-coordinate of the object position (center).
	 *
	 * @return the x-coordinate of the object position
	 */
	public float getX() {
		return position.x;
	}

	/**
	 * Sets the x-coordinate of the object position (center).
	 *
	 * @param value the x-coordinate of the object position
	 */
	public void setX(float value) {
		position.x = value;
	}

	/**
	 * Returns the y-coordinate of the object position (center).
	 *
	 * @return the y-coordinate of the object position
	 */
	public float getY() {
		return position.y;
	}

	/**
	 * Sets the y-coordinate of the object position (center).
	 *
	 * @param value the y-coordinate of the object position
	 */
	public void setY(float value) {
		position.y = value;
	}

	/**
	 * Returns the velocity of this object in pixels per animation frame.
	 *
	 * The value returned is a reference to the velocity vector, which may be
	 * modified freely.
	 *
	 * @return the velocity of this object
	 */
	public Vector2 getVelocity() {
		return velocity;
	}

	/**
	 * Returns the x-coordinate of the object velocity.
	 *
	 * @return the x-coordinate of the object velocity.
	 */
	public float getVX() {
		return velocity.x;
	}

	/**
	 * Sets the x-coordinate of the object velocity.
	 *
	 * @param value the x-coordinate of the object velocity.
	 */
	public void setVX(float value) {
		velocity.x = value;
	}

	/**
	 * Sets the y-coordinate of the object velocity.
	 *
	 * //@param value the y-coordinate of the object velocity.
	 */
	public float getVY() {
		return velocity.y;
	}

	/**
	 * Sets the y-coordinate of the object velocity.
	 *
	 * @param value the y-coordinate of the object velocity.
	 */
	public void setVY(float value) {
		velocity.y = value;
	}


	/**
	 * Returns true if this object is destroyed.
	 *
	 * Objects are not removed immediately when destroyed.  They are garbage collected
	 * at the end of the frame.  This tells us whether the object should be garbage
	 * collected at the frame end.
	 *
	 * @return true if this object is destroyed
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	/**
	 * Sets whether this object is destroyed.
	 *
	 * Objects are not removed immediately when destroyed.  They are garbage collected
	 * at the end of the frame.  This tells us whether the object should be garbage
	 * collected at the frame end.
	 *
	 * @param value whether this object is destroyed
	 */
	public void setDestroyed(boolean value) {
		destroyed = value;
	}

	/**
	 * Returns the radius of this object.
	 *
	 * All of our objects are circles, to make collision detection easy.
	 *
	 * @return the radius of this object.
	 */
	public float getRadius() {
		return radius;
	}
	
	/**
	 * Returns the current angle of this particle
	 *
	 * @return the current angle of this particle
	 */
	public float getAngle() {
		return angle;
	}

	/**
	 * Returns the current age of this particle
	 *
	 * @return the current age of this particle
	 */
	public int getAge(){return age;}

	/**
	 * Updates the age and angle of this particle.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float delta) {
		position.add(velocity);
//		System.out.println("age"+age);
//
//		System.out.println("is destroyed"+destroyed);
		
		// Decrease time until death; die if it's time
		if (--age <= 0) {
			destroyed = true;
		}
		
		// Compute a new angle of rotation.
		angle = (float)(delta*1000 % (8 * Math.PI)); // MAGIC NUMBERS
	}


	/**
	 * Particle must fit in a square of this side length
	 */
	private float sizeConfine;
	public void setSizeConfine(float s){
		sizeConfine = s;
	}
	/**
	 * Draws this object to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects 
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas) {
		float scale = Math.min(sizeConfine/animator.getRegionWidth(), sizeConfine/animator.getRegionHeight());
		if (age>0){
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, position.x, position.y, angle, scale, scale);

		}

	}
	
}
