/*
 * Note.java
 *
 *
 * This separation is very important for this class because it has a lot
 * of interactions with other classes.  When a note is hit, it emits stars.
 */
package edu.cornell.gdiac.temporary.entity;

import edu.cornell.gdiac.temporary.*;
import edu.cornell.gdiac.util.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Model class for Notes.
 */
public class Note{
	/** Rescale the size of a shell */
	private static final float SHELL_SIZE_MULTIPLE = 4.0f;
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;

	public int getNUM_ANIM_FRAMES() {
		return NUM_ANIM_FRAMES;
	}

	public void setNUM_ANIM_FRAMES(int NUM_ANIM_FRAMES) {
		this.NUM_ANIM_FRAMES = NUM_ANIM_FRAMES;
	}

	/** The number of animation frames in our filmstrip */
	private int   NUM_ANIM_FRAMES;
	/** Current animation frame for this shell */
	private float animeframe;

	private int hitStatus;

	public int getHitStatus(){
		return hitStatus;
	}
	public void setHitStatus(int t){
		hitStatus = t;
	}

	/** line the note is one */
	private int line;
	public int getLine(){
		return line;
	}
	public void setLine(int t){
		line = t;
	}
	private long startSample;
	public long getStartSample(){
		return startSample;
	}
	public void setStartSample(long t){
		startSample = t;
	}

	private long hitSample;
	public long getHitSample(){
		return hitSample;
	}
	public void setHitSample(long t){
		hitSample = t;
	}

	/**
	 * How many samples do we intend to hold the held note for?
	 */
	private long holdSamples;

	public long getHoldSamples(){
		return holdSamples;
	}
	public void setHoldSamples(long t){
		holdSamples = t;
	}

	public enum NoteType {
		SWITCH,
		HELD,
		BEAT
	}

	private NoteType nt;

	public NoteType getNoteType(){
		return nt;
	}
	public void setNoteType(NoteType t){
		nt = t;
	}
	private float w;

	public float getWidth(){
		return w;
	}
	public void setWidth(float t){
		w = t;
	}
	private float h;

	public float getHeight(){
		return h;
	}
	public void setHeight(float t){
		h = t;
	}
	private float x;

	public float getX(){
		return x;
	}
	public void setX(float t){
		x = t;
	}
	private float y;
	public float getY(){
		return y;
	}
	public void setY(float t){
		y = t;
	}
	private float by;
	public float getBottomY(){
		return by;
	}
	public void setBottomY(float y){
		by = y;
	}
	private boolean destroyed;
	public boolean isDestroyed(){
		return destroyed;
	}
	public void setDestroyed(boolean d){
		destroyed = d;
	}
	FilmStrip animator;
	Vector2 origin;

	/**
	 * Initialize shell with trivial starting position.
	 */
	public Note(int line, NoteType n, long startSample, Texture t) {
		// Set minimum Y velocity for this shell
		this.line = line;
		hitStatus = 0;
		animeframe = 0.0f;
		curEndFrame = 0.0f;
		curTrailFrame = 0.0f;
		nt = n;
		this.startSample = startSample;
		switch(nt) {
			case BEAT:
				NUM_ANIM_FRAMES = 1;
				break;
			case HELD:
				NUM_ANIM_FRAMES = 1;
				break;
			case SWITCH:
				NUM_ANIM_FRAMES = 1;
				break;
		}
		animator = new FilmStrip(t,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
		h = animator.getRegionHeight();
		w = animator.getRegionWidth();
	}

	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
		h = animator.getRegionHeight();
		w = animator.getRegionWidth();
	}

	private FilmStrip trailAnimator;
	private int trailFrames;
	/**
	 * Height of the trail texture
	 */
	private float trailHeight;
	/**
	 * Width of the trail texture
	 */
	private float trailWidth;
	/**
	 * Origin of trail texture
	 */
	private Vector2 trailOrigin;


	private FilmStrip endAnimator;
	private int endFrames;
	/**
	 * Height of end texture
	 */
	private float endHeight;
	/**
	 * Width of end texture
	 */
	private float endWidth;
	/**
	 * Origin of end texture
	 */
	private Vector2 endOrigin;
	private float curTrailFrame;
	private float curEndFrame;

	public void setHoldTextures(Texture trail, int trailFrames, Texture end, int endFrames){
		trailAnimator = new FilmStrip(trail, 1, trailFrames, trailFrames);
		this.trailFrames = trailFrames;
		trailHeight = trailAnimator.getRegionHeight();
		trailWidth = trailAnimator.getRegionWidth();
		trailOrigin = new Vector2(trailWidth/2f, trailHeight/2f);

		endAnimator = new FilmStrip(end, 1, endFrames, endFrames);
		this.endFrames = endFrames;
		endHeight = endAnimator.getRegionHeight();
		endWidth = endAnimator.getRegionWidth();
		endOrigin = new Vector2(endWidth/2f, endHeight/2f);
	}



	/**
	 * Update animations
	 */
	public void update() {
		// Increase animation frame
		animeframe += ANIMATION_SPEED;
		if (animeframe >= NUM_ANIM_FRAMES) {
			animeframe -= NUM_ANIM_FRAMES;
		}
		if(nt == NoteType.HELD){
			curTrailFrame += ANIMATION_SPEED;
			if(curTrailFrame >= trailFrames){
				curTrailFrame -= trailFrames;
			}
			curEndFrame += ANIMATION_SPEED;
			if(curEndFrame >= endFrames){
				curEndFrame -= endFrames;
			}
		}
	}

	private float tail_thickness = 5f;

	public float getTail_thickness(){
		return tail_thickness;
	}
	public void setTail_thickness(float t){
		tail_thickness = t;
	}
	/**
	 * Draws this note to the canvas under a width and height restriction
	 * This will draw the image in the original scale, and will scale the image down by the smallest possible factor
	 * to meet the confinements
	 *
	 * @param canvas The drawing context
	 */
	public void draw(GameCanvas canvas, float widthConfine, float heightConfine) {

		float scale = Math.max(widthConfine/w, heightConfine/h);
		if(nt == NoteType.HELD){
			tail_thickness = widthConfine/2f;
			trailAnimator.setFrame((int)curTrailFrame);
			endAnimator.setFrame((int)curEndFrame);
			for (float cury = by; cury <= y; cury += trailHeight*tail_thickness/trailWidth){
				canvas.draw(trailAnimator, Color.WHITE, trailOrigin.x, 0, x, cury,
						0.0f, tail_thickness/trailWidth, tail_thickness/trailWidth);
			}



			canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, by,
					0.0f, scale, scale);
			canvas.draw(endAnimator, Color.WHITE, endOrigin.x, endOrigin.y, x, y,
					0.0f, scale, scale);
		}
		else{

			animator.setFrame((int)animeframe);
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, y,
					0.0f, scale, scale);
		}

	}

}