package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.temporary.GameObject;
import edu.cornell.gdiac.util.FilmStrip;

/*
* let's pretend a note is a fish
 */
public class Fish extends GameObject {
    public enum NoteType {
        SINGLE, HOLD, SWITCH, DEAD
    }

    // NOTE DETAILS
    /** NOTE MOVEMENT DOWN IS CONSTANT. TODO: CHANGE??? */
    private static final float NOTE_DRAW = 10f;
    /** Note type. Single / Hold / Switch / Dead */
    private NoteType noteType;
    /** number of notes we show in advance */
    private int beatsAhead = 5;
    /** beat number in the song */
    private int beat;
    /***/
    public int height;
    private int width;

    /** Spawn position */
    private Vector2 spawnPosition;

    /** Leave position */
    private Vector2 exitPosition;

    /** beat number in the song held note starts */
    private int startBeat; // for held notes only
    /** beat number in the song held note ends */
    private int endBeat; // for held notes only
    /** lane 0/1/2/3 for instrument */
    private int lane;
    /** visible? */
    // ANIMATION INFORMATION /////////////////////////////////////////////////////////////////////////////////

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

    /** To measure if we are damaged */
    private boolean damaged;
    /** The backup texture to use if we are damaged */
    private Texture dmgTexture;
    private Texture catTexture;
    /** are we hit? */
    public int hitStatus;

    /** line the note is one */
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
        System.out.println("NOTE MADE");
        String noteTypeData = data.getString("note");
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
            this.lane = -1;
        }
        this.catTexture = cat;
        this.setTexture(cat);
    }

    /** Set a note to be destroyed. */
    public void setDestroyed(boolean value) { damaged = true; }

    @Override
    public ObjectType getType() {
        return ObjectType.FISH;
    }
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

    /** Update the animation and position of this note. */
    public void update(int songBeat, float delta) {
        // CHANGE POSITION. ie) position.add(velocity)
        //transform.position = Vector2.Lerp(transform.position, destination, Time.deltaTime);
        // (BeatsShownInAdvance - (beatOfThisNote - songPosInBeats)) / BeatsShownInAdvance

        //float timeMove = (beatsAhead - (this.beat - this.));

        float timeLeft = this.beat - songBeat;

        // distance to move icon from current position
        float distance = Vector2.dst(getX(), exitPosition.y, getX(), getY());
        float distanceToMove = distance * (delta / timeLeft);

        // update position: goal.position - current.position
        Vector2 direction = new Vector2();
        direction.set(getX(), exitPosition.y-getY());

        setX(direction.nor().x * distanceToMove);

        if(position == exitPosition){
            this.setDestroyed(true);
        }

        // TODO: HELD NOTE

        // increase animation frame
        animFrame += ANIMATION_SPEED;
        if(animFrame >= NUM_ANIM_FRAMES){
            animFrame -= NUM_ANIM_FRAMES;
        }
    }

    /** Update note position */
    public void updatePosition(){

    }

    /** Initialize note position in the lane and height */
    public void setPosition(float height, int bandMemberOrder, Texture texture, float smallwidth, float largewidth, float inBetweenWidth, float LEFTBOUND){
        float x = LEFTBOUND + bandMemberOrder * (inBetweenWidth + smallwidth) + largewidth/8f + lane * (largewidth/4f);
        float y = height;

        float BOTTOMBOUND = height/5f;

        this.setX(x);
        this.setTexture(texture);
        this.setY(y);

        this.spawnPosition = new Vector2(x, y);
        this.position = spawnPosition;
        position.set(spawnPosition);
        this.exitPosition = new Vector2(x, BOTTOMBOUND);
        System.out.println("set position");
    }

    /** Draw the note to the canvas.
     *
     * There is only one drawing pass in this application, so you can draw the objects in any order.
     *
     * @param canvas the drawing context*/
    public void draw(GameCanvas canvas){

        // TODO: HELD NODE if note is HELD do something else

        animator.setFrame((int) animFrame);
        canvas.draw(animator, Color.WHITE, origin.x, origin.y, position.x, position.y,
                0.0f, NOTE_MULTIPLIER, NOTE_MULTIPLIER);
    }

    /** Draw the note to the canvas given a width and height confinement */
    public void draw(GameCanvas canvas, float widthConfine, float heightConfine){
        canvas.draw(catTexture, getX(), getY());
        //canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y, 0, 0, 0.0f, widthConfine/width, heightConfine/height);
    }

}
