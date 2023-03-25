package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.temporary.GameObject;
import edu.cornell.gdiac.temporary.MusicController;
import edu.cornell.gdiac.util.FilmStrip;

/*
* let's pretend a note is a fish
 */
public class Fish {




    public enum NoteType {
        SINGLE, HOLD, SWITCH, DEAD
    }

    // NOTE DETAILS
    /** Note type. Single / Hold / Switch / Dead */
    private NoteType noteType;

    /** beat number in the song */
    private int beat;
    /** height of the note texture */
    public int height;
    /** width of the note texture */
    private int width;

    /** Spawn position */
    private Vector2 spawnPosition;

    /** Exit position / point to be destroyed at*/
    private Vector2 exitPosition;

    /** beat number in the song held note starts */
    private int startBeat; // for held notes only
    /** beat number in the song held note ends */
    private int endBeat; // for held notes only
    /** lane 0/1/2/3 for instrument */
    private int lane;
    /** visible? */

    /** object position (centered on the texture middle) */
    protected Vector2 position;
    /** Reference to texture origin */
    protected Vector2 origin;
    /** Radius of the object (used for collisions) */
    protected float radius;
    /** Whether or not the object should be removed at next timestep. */
    protected boolean destroyed;

    /** Rescale the size of a note */
    private static final float NOTE_MULTIPLIER = 4.0f;

    /** CURRENT image for this object. May change over time. */
    protected FilmStrip animator;

    /** Current animation frame for this shell */
    private float animFrame;
    /** How fast we change frames (one frame per 4 calls to update) */
    private static final float ANIMATION_SPEED = 0.25f;
    /** The number of animation frames in our filmstrip */
    private static final int NUM_ANIM_FRAMES = 4;

    /** The note texture */
    private Texture catTexture;
    /** are we hit? */
    public int hitStatus;

    /** line the note is on */
    public int line;
    /** ??? */
    public int startFrame;
    /** ??? */
    public int holdFrame;

    /** Allocate space for a note. */
    public Fish(){
        this.noteType = NoteType.DEAD;
        this.beat = -1;
        this.lane = -1;
    }

    /**
     * Initialize a note given JSON data
     * */
    public Fish(JsonValue data, Texture cat){
        //System.out.println("NOTE MADE");
        String noteTypeData = data.getString("note");

        this.beat = data.getInt("beat");

        if(noteTypeData == "Single"){
            this.noteType = NoteType.SINGLE;
            this.beat = data.getInt("beat");
            this.lane = data.getInt("lane");
        }
        else if(noteTypeData == "Hold"){
            //TODO: HELD NOTES
        }
        else if(noteTypeData == "Switch"){
            //TODO: SWITCH NOTES
            this.noteType = NoteType.SINGLE;
            this.beat = data.getInt("beat");
            this.lane = 0;
        }

        this.setTexture(cat);
        this.position = new Vector2();
    }

    /** Set a note to be destroyed. */
    public void setDestroyed(boolean value) { destroyed = true; }
    public NoteType getNoteType(){return noteType;}
    public int getLane(){return lane;}
    public int getBeat(){return beat;}

    /** Set texture of note depending on note type.
     *
     * @param texture */
    public void setTexture(Texture texture){
        animator = new FilmStrip(texture, 1, NUM_ANIM_FRAMES, NUM_ANIM_FRAMES);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
        radius = animator.getRegionHeight() / 2.0f;
        height = animator.getRegionHeight();
        width = animator.getRegionWidth();
    }

    /** Update the animation and position of this note based on the current beat of the song and delta.
     *
     * @param songBeat
     * @param delta */
    public void update(float songBeat, float delta) {
        // CHANGE POSITION. ie) position.add(velocity)
        // transform.position = Vector2.Lerp(transform.position, destination, Time.deltaTime);
        // (BeatsShownInAdvance - (beatOfThisNote - songPosInBeats)) / BeatsShownInAdvance
        System.out.println(songBeat);
        System.out.println(beat);
        float timeLeft = this.beat - songBeat;
        float progress = 1 - ((timeLeft) / MusicController.beatsShownInAdvance);

        //System.out.println(timeLeft);

        // distance to move icon from current position
        float distance = Vector2.dst(position.x, position.y, exitPosition.x, exitPosition.y);
        float distanceToMove = distance * (delta / timeLeft);

        System.out.println(distance);
        System.out.println("distance to move " + distanceToMove);

        // update position: goal.position - current.position
        Vector2 direction = new Vector2();
        direction.set(position.x, exitPosition.y-position.y);

        System.out.println("curr pos " + position);
        System.out.println("direction " + direction);

        position.y = (direction.nor().y * distanceToMove);
        System.out.println("new pos " + position);

        position.y = spawnPosition.y - ((spawnPosition.y - exitPosition.y)*progress);


        if(position == exitPosition){
            this.setDestroyed(true);
        }

        System.out.println("note updated");

        // TODO: HELD NOTE

    }


    /** Initialize note position in the lane and height */
    public void setPosition(float height, int bandMemberOrder, Texture texture, float smallwidth, float largewidth, float inBetweenWidth, float LEFTBOUND){
        float x = LEFTBOUND + bandMemberOrder * (inBetweenWidth + smallwidth) + largewidth/8f + lane * (largewidth/4f);
        float y = height;

        float BOTTOMBOUND = height/5f;

        //this.setTexture(texture);
        this.spawnPosition = new Vector2(x, y);
        position.set(spawnPosition);
        this.exitPosition = new Vector2(x, BOTTOMBOUND);
        System.out.println(position);
        System.out.println(exitPosition);
        System.out.println("new");
    }


    /** Draw the note to the canvas given a width and height confinement */
    public void draw(GameCanvas canvas, float widthConfine, float heightConfine){
        System.out.println("NOTE DRAWN");
        animator.setFrame(1);
        canvas.draw(animator, position.x, position.y);

        //canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y, 0, 0, 0.0f, widthConfine/width, heightConfine/height);
    }

    public boolean isDestroyed() {
        return destroyed;
    }
    public float getY(){return position.y;}
    public float getX(){return position.x;}

}
