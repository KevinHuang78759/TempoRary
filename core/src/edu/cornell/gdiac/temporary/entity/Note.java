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
	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	private FilmStrip backSplash;
	private FilmStrip frontSplash;

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

	/** line the note is on (if switch note, this is -1) */
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
	 * Note constructor
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
		//Set the number of animation frames
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

	/**
	 * Sets the note texture
	 * @param texture
	 */
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

	/**
	 * Set the textures for held notes. This is outside the constructor because not every note is a held note
	 * @param trail
	 * @param trailFrames
	 * @param end
	 * @param endFrames
	 */
	public void setHoldTextures(Texture trail, int trailFrames, Texture end, int endFrames, FilmStrip backSplash, FilmStrip frontSplash){
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

		// hold animations
		this.backSplash = backSplash.copy();
		this.frontSplash = frontSplash.copy();
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
		if(nt == NoteType.HELD) {
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
	public void draw(GameCanvas canvas, float widthConfine, float heightConfine, float topbound, float lowbound) {
		//Calculate a scale such that the entire sprite fits within both confines, but does not get distorted
		float scale = Math.min(widthConfine/w, heightConfine/h);

		if(nt == NoteType.HELD){
			canvas.draw(backSplash, Color.WHITE, backSplash.getRegionWidth() / 2, backSplash.getRegionHeight() / 2,
					x, by + animator.getRegionHeight() / 2 * scale, 0.0f, scale * 6, scale * 6);
			//The tail should be about half the width of the actual note assets
			tail_thickness = widthConfine/2f;
			//Set the animation frame properly
			trailAnimator.setFrame((int)curTrailFrame);
			endAnimator.setFrame((int)curEndFrame);
			float trailScale = tail_thickness/trailWidth;
			//Start at the bottom location, then draw until we reach the top
			float starty = by + trailHeight*trailScale*0.5f;
			float trueHeight = trailHeight*trailScale;
			float numSegments = ((y - by)/(trailHeight*trailScale));
			for(int i = 0; i < (int)numSegments; ++i){
				//only draw if in bounds
				float drawy = starty + i*trailHeight*trailScale;
				if(drawy - trueHeight/2 < topbound && drawy + trueHeight/2 > topbound){
					//we need to draw the full sprite somewhere
					canvas.draw(trailAnimator, Color.WHITE, trailOrigin.x, trailOrigin.y, 4000, 4000, 0f, trailScale, trailScale);

					float fracToDraw = (topbound - (drawy - trueHeight/2))/trueHeight;
					canvas.drawSubsection(trailAnimator, x, drawy, trailScale, 0f, 1f, 0f, fracToDraw);

				}
				else if(drawy + trueHeight/2 <= topbound && drawy - trueHeight/2 >=lowbound){
					canvas.draw(trailAnimator, Color.WHITE, trailOrigin.x, trailOrigin.y, x, drawy, 0f, trailScale, trailScale);

				}
				else if(drawy + trueHeight/2 > lowbound && drawy - trueHeight < lowbound){
					//we need to draw the full sprite somewhere
					canvas.draw(trailAnimator, Color.WHITE, trailOrigin.x, trailOrigin.y, 4000, 4000, 0f, trailScale, trailScale);

					float fracToDraw = (drawy + trueHeight/2 - lowbound)/trueHeight;
					canvas.drawSubsection(trailAnimator, x, drawy, trailScale, 0f, 1f, 1f - fracToDraw, 1.0f);

				}
			}
			//We do not want to draw the tail in a way such that it will poke out of the bottom sprite
			//Therefore, we need to draw only a vertical fraction of our trail asset for the final
			//segment
			float lastDrawY = starty + ((int)numSegments)*trailHeight*trailScale;
			float lastSegmentHeight = trueHeight * (numSegments - (int)numSegments);
			float lastSegmentYCenter = lastDrawY - trueHeight/2 + lastSegmentHeight/2;
			if(lastSegmentYCenter + lastSegmentHeight/2 > topbound && lastSegmentYCenter - lastSegmentHeight/2 < topbound){
				float lastSegFracToDraw = (topbound - (lastDrawY - trueHeight/2))/trueHeight;
				canvas.drawSubsection(trailAnimator, x, lastDrawY, trailScale, 0f, 1f, 0f, lastSegFracToDraw);

			}
			else if(lastSegmentYCenter + lastSegmentHeight/2 <= topbound && lastSegmentYCenter - lastSegmentHeight/2 >= lowbound){
				canvas.drawSubsection(trailAnimator, x, lastDrawY, trailScale, 0f, 1f, 0f, numSegments - (int)numSegments);

			}
			else if(lastSegmentYCenter + lastSegmentHeight/2 > lowbound && lastSegmentYCenter - lastSegmentHeight/2 < lowbound){
				float lastSegFracStart = (lowbound - (lastDrawY - trueHeight/2))/trueHeight;
				canvas.drawSubsection(trailAnimator, x, lastDrawY, trailScale, 0f, 1f, lastSegFracStart, numSegments - (int)numSegments);

			}
//			canvas.drawSubsection(trailAnimator, x, lastDrawY, trailScale, 0f, 1f, 0f, numSegments - (int)numSegments);



			//The head and tail are drawn after the trails to cover up the jagged ends
			float headHeight = animator.getRegionHeight()*scale;
			if(by + headHeight/2 >topbound && by - headHeight/2 < topbound){
				canvas.draw(animator, Color.WHITE, origin.x, origin.y, 4000, 4000,
						0.0f, scale, scale);
				float drawFrac = (topbound - (by - headHeight/2))/headHeight;
				canvas.drawSubsection(animator, x, by,scale, 0f, 1f, 0f, drawFrac);
			}
			else if(by + headHeight/2 <= topbound && by - headHeight/2 >= lowbound){
				canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, by,
						0.0f, scale, scale);
			}
			else if(by + headHeight/2 >lowbound && by - headHeight/2 < lowbound){
				canvas.draw(animator, Color.WHITE, origin.x, origin.y, 4000, 4000,
						0.0f, scale, scale);
				float startFrac = (lowbound - (by - headHeight/2))/headHeight;
				canvas.drawSubsection(animator, x, by, scale, 0f, 1f, startFrac, 1f);
			}

			float endH = endHeight*scale;
			if(y + endH/2 > topbound && y - endH/2 < topbound){
				canvas.draw(endAnimator, Color.WHITE, origin.x, origin.y, 4000, 4000,
						0.0f, scale, scale);
				float drawFrac = (topbound - (y - endH/2))/endH;
				canvas.drawSubsection(endAnimator, x, y, scale, 0f, 1f, 0f, drawFrac);
			}
			else if(y + endH/2 <= topbound && y - endH/2 >= lowbound){
				canvas.draw(endAnimator, Color.WHITE, origin.x, origin.y, x, y,
						0.0f, scale, scale);
			}
			else if(y + endH/2 >lowbound && y - endH/2 < lowbound){
				canvas.draw(endAnimator, Color.WHITE, origin.x, origin.y, 4000, 4000,
						0.0f, scale, scale);
				float startFrac = (lowbound - (y - endH/2))/endH;
				canvas.drawSubsection(endAnimator, x, y, scale, 0f, 1f, startFrac, 1f);
			}

			canvas.draw(frontSplash, Color.WHITE, frontSplash.getRegionWidth() / 2,frontSplash.getRegionHeight() / 2,
					x, by +  animator.getRegionHeight() / 2 * scale, 0.0f, scale * 6, scale * 6);
		}
		else{
			animator.setFrame((int)animeframe);
			float headHeight = animator.getRegionHeight()*scale;
			if(y + headHeight/2 >topbound && y - headHeight/2 < topbound){
				canvas.draw(animator, Color.WHITE, origin.x, origin.y, 4000, 4000,
						0.0f, scale, scale);
				float drawFrac = (topbound - (y - headHeight/2))/headHeight;
				canvas.drawSubsection(animator, x, y,scale, 0f, 1f, 0f, drawFrac);
			}
			else if(y + headHeight/2 <= topbound && y - headHeight/2 >= lowbound){
				canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, y,
						0.0f, scale, scale);
			}
			else if(y + headHeight/2 >lowbound && y - headHeight/2 < lowbound){
				canvas.draw(animator, Color.WHITE, origin.x, origin.y, 4000, 4000,
						0.0f, scale, scale);
				float startFrac = (lowbound - (y - headHeight/2))/headHeight;
				canvas.drawSubsection(animator, x, y, scale, 0f, 1f, startFrac, 1f);
			}
		}
	}
}