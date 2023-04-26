package edu.cornell.gdiac.temporary.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Model class for editors notes.
 * Does not extend GameObject because this is an editor object not present in the game.
 */
public class EditorNote implements Comparable<EditorNote> {
    private float sizeMultiple;

    private FilmStrip animator;

    private Vector2 origin;

    /** true if the cat should meow :)*/
    private boolean meow;

    /** lane the note corresponds to */
    private int lane;

    /** line the note corresponds to (Switch notes MUST have line = -1) */
    private int line;

    /** position in the song the note appears */
    private int songPos;

    /** duration which held notes are held (only important for held notes)*/
    private int duration;

    /** horizontal location of note on screen */
    private float x;

    /** vertical location of note on screen */
    private float y;

    /** True if the note should be visible on screen*/
    private boolean onScreen;

    @Override
    public int compareTo(EditorNote n) {
        if (this.getPos() == n.getPos()) {
            return 0;
        } else if (this.getPos() < n.getPos()) {
            return -1;
        } else {
            return 1;
        }
    }

    public enum NoteType{
        SWITCH,
        HELD,
        BEAT
    }

    /** Type of the note (either Beat, Held, or Switch)*/
    private NoteType type;

    public EditorNote(NoteType type, int lane, int line, int songPos, int duration){
        meow = true;
        this.lane = lane;
        this.line = line;
        if (type == NoteType.SWITCH){
            this.line = -1;
        }
        this.songPos = songPos;
        this.type = type;
        this.duration = duration;
        sizeMultiple = 1;
    }

    public boolean shouldMeow(){return meow;}
    public void setMeow(boolean meow){this.meow = meow;}

    public void setLane(int lane){
        this.lane = lane;
    }

    public int getLane(){return lane;}

    public void setLine(int line){
        this.line = line;
    }

    public int getLine(){return line;}

    public void setPos(int songPos){
        this.songPos = songPos;
    }

    public int getPos(){
        return songPos;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public int getDuration(){
        return duration;
    }

    public void setType(NoteType type){
        this.type = type;
    }

    public NoteType getType(){
        return type;
    }

    /**
     * sets the horizotnal screen location of the note
     * @param x horizontal screen location
     */
    public void setX(float x){
        this.x = x;
    }

    /**
     * sets the vertical screen location of the note
     * @param y vertical screen location
     */
    public void setY(float y){
        this.y = y;
    }

    /**
     * returns the horizontal screen location of the note
     * @return the horizontal screen location
     */
    public float getX(){
        return x;
    }

    /**
     * returns the vertical screen location of the note
     * @return the vertical screen location
     */
    public float getY(){
        return y;
    }

    public void setOnScreen(boolean onScreen){
        this.onScreen = onScreen;
    }

    public boolean OnScreen(){
        return onScreen;
    }

    public void setTexture(Texture texture){
        animator = new FilmStrip(texture, 1, 4,4);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }
    public void draw(GameCanvas canvas, float zoom, float laneEdge, float laneWidth){
        sizeMultiple = 3;
        if (zoom < 1 / 2) {
            sizeMultiple = 3 * (zoom + 1 / 2);
        }
        if (type == NoteType.BEAT) {
            animator.setFrame(0);
            canvas.draw(animator, Color.WHITE, origin.x, origin.y, x, y, 0.0f, sizeMultiple, sizeMultiple);
        }
        if (type == NoteType.HELD) {
            animator.setFrame(0);
            canvas.draw(animator, Color.SALMON, origin.x, origin.y, x, y, 0.0f, sizeMultiple, sizeMultiple);
        }
        if (type == NoteType.SWITCH) {
           canvas.drawLine(laneEdge, y, laneEdge + laneWidth, y, 4*((int) sizeMultiple), Color.LIME);
        }
    }

}
