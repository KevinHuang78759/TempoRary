/*
 * Note.java
 *
 *
 * This separation is very important for this class because it has a lot 
 * of interactions with other classes.  When a shell dies, it emits stars.
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
public class Note{
	public static final float descentSpeed = -4.5f;
	/** Rescale the size of a shell */
	private static final float SHELL_SIZE_MULTIPLE = 4.0f;
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int   NUM_ANIM_FRAMES = 4;
	/** Current animation frame for this shell */
	private float animeframe;

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
	public float w;
	public float h;
	public float x;
	public float y;
	public float vy;
	public float by;
	public boolean destroyed;
	FilmStrip animator;
	Vector2 origin;
	
	/**
	 * Initialize shell with trivial starting position.
	 */
	public Note(int line, NType n, int frame) {
		// Set minimum Y velocity for this shell
		this.line = line;
		hitStatus = 0;
		animeframe = 0.0f;
		nt = n;
		vy = n == NType.HELD? 0f : descentSpeed;
		startFrame = frame;
	}

	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
		h = animator.getRegionHeight();
		w = animator.getRegionWidth();
	}


	public void update(int frame) {

		if(nt == NType.HELD){
			by += descentSpeed;
			if(frame == (startFrame + holdFrame)){
				vy = descentSpeed;
			}

		}
		y += vy;
		// Increase animation frame
		animeframe += ANIMATION_SPEED;
		if (animeframe >= NUM_ANIM_FRAMES) {
			animeframe -= NUM_ANIM_FRAMES;
		}
	}

	public float tail_thickness = 5f;
	/**
	 * Draws this shell to the canvas
	 *
	 * There is only one drawing pass in this application, so you can draw the objects 
	 * in any order.
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas, float widthConfine, float heightconFine) {
		if(nt == NType.HELD){
			canvas.drawRect(x - tail_thickness/2, by, x + tail_thickness/2, y, Color.BLUE, true);

			animator.setFrame(0);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, by,
					0.0f, widthConfine/w, heightconFine/h);
		}
		else{
			animator.setFrame((int)animeframe);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, y,
					0.0f, widthConfine/w, heightconFine/h);
		}

	}

}