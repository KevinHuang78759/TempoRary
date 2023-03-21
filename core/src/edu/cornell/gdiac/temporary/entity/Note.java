/*
 * Note.java
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
package edu.cornell.gdiac.temporary.entity;

import edu.cornell.gdiac.temporary.*;
import edu.cornell.gdiac.util.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Model class for enemy shells.
 */
public class Note extends GameObject {
	public static final float descentSpeed = -3.5f;
	/** Rescale the size of a shell */
	private static final float SHELL_SIZE_MULTIPLE = 4.0f;
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int   NUM_ANIM_FRAMES = 4;

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

	public int startFrame;
	public int holdFrame;

	public enum NType{
		SWITCH,
		HELD,
		BEAT
	}

	public NType nt;

	/**
	 * Returns the type of this object.
	 *
	 * We use this instead of runtime-typing for performance reasons.
	 *
	 * @return the type of this object.
	 */
	public ObjectType getType() {
		return ObjectType.NOTE;
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
	public Note(int line, NType n) {
		// Set minimum Y velocity for this shell
		this.line = line;
		minvelocy = 0f;
		hitStatus = 0;
		animeframe = 0.0f;
		nt = n;
		setVY(n == NType.HELD? 0f : descentSpeed);
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

	public float bx;
	public float by;
	public Texture getDamagedTexture() {
		return dmgTexture;
	}

	/**
	 * Updates the animation frame and velocity of this shell.
	 *
	 * @param delta Number of seconds since last animation frame
	 */


	public void update(float delta, int frame) {
		// Call superclass's run
		super.update(delta);

		if(nt == NType.HELD){
			by += descentSpeed;
			if(frame%1800 == (startFrame + holdFrame)%1800){
				setVY(descentSpeed);
			}

		}

		// Increase animation frame
		animeframe += ANIMATION_SPEED;
		if (animeframe >= NUM_ANIM_FRAMES) {
			animeframe -= NUM_ANIM_FRAMES;
		}
	}

	public float tail_thickness = 0f;
	/**
	 * Draws this shell to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects 
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas) {
		if(nt == NType.HELD){

			tail.setFrame(0);
			//System.out.println(bx + " " + by + " " + position.x + " " + position.y + " " + tail_thickness);
			canvas.textureRect(tail, bx - tail_thickness/2, by, position.x + tail_thickness/2, position.y);
			canvas.drawRect(bx - tail_thickness/2, by, position.x + tail_thickness/2, position.y, Color.BLUE, true);

			animator.setFrame(0);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, bx, by,
					0.0f, SHELL_SIZE_MULTIPLE, SHELL_SIZE_MULTIPLE);
		}
		else{
			animator.setFrame((int)animeframe);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, position.x, position.y,
					0.0f, SHELL_SIZE_MULTIPLE, SHELL_SIZE_MULTIPLE);
		}

	}

	@Override
	public String toString() {
		return "Note{" +
				"position=" + position +
				", velocity=" + velocity +
				", origin=" + origin +
				", radius=" + radius +
				", destroyed=" + destroyed +
				", animator=" + animator +
				", minvelocy=" + minvelocy +
				", animeframe=" + animeframe +
				", damaged=" + damaged +
				", dmgTexture=" + dmgTexture +
				", hitStatus=" + hitStatus +
				", line=" + line +
				'}';
	}
}