package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

public class CalibNote {

    /** How fast we change frames (one frame per 4 calls to update) */
    private static final float ANIMATION_SPEED = 0.25f;
    /** The number of animation frames in our filmstrip */
    private static final int   NUM_ANIM_FRAMES = 4;
    /** Current animation frame for this shell */
    private float animeframe;
    // Attributes for all game objects
    /** Object position (centered on the texture middle) */
    protected Vector2 position;
    /** Object velocity vector */
    protected Vector2 velocity;
    /** Reference to texture origin */
    protected Vector2 origin;
    /** Radius of the object (used for collisions) */
    protected float radius;
    /** CURRENT image for this object. May change over time. */
    protected FilmStrip animator;
    private static final float SHELL_SIZE_MULTIPLE = 4.0f;
    /** How fast we change frames (one frame per 4 calls to update) */




    public CalibNote() {
        velocity=new Vector2(20,20);
        position=new Vector2(0,0);
    }

    public void setTexture(Texture texture) {
        System.out.println(texture);
        animator = new FilmStrip(texture,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
        radius = animator.getRegionHeight() /  2.0f;
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
     * Returns the radius of this object.
     *
     * All of our objects are circles, to make collision detection easy.
     *
     * @return the radius of this object.
     */
    public float getRadius() {
        return radius * SHELL_SIZE_MULTIPLE;
    }

    /**
     * Updates the state of this object.
     *
     * This method only is only intended to update values that change local state in
     * well-defined ways, like position or a cooldown value.  It does not handle
     * collisions (which are determined by the CollisionController).  It is
     * not intended to interact with other objects in any way at all.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void update(float delta) {
        position.add(velocity);
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
        animator.setFrame((int)animeframe);
        canvas.draw(animator, Color.WHITE, origin.x, origin.y, position.x, position.y,
                0.0f, SHELL_SIZE_MULTIPLE, SHELL_SIZE_MULTIPLE);
    }

}
