package edu.cornell.gdiac.temporary.editor;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.temporary.InputController;
import edu.cornell.gdiac.temporary.entity.*;
import edu.cornell.gdiac.temporary.GameObject.ObjectType;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.audio.*;

import java.util.HashMap;
import java.util.LinkedList;

public class EditorMode implements Screen {

    private MusicQueue music;

    private Texture background;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Amount of game ticks which have gone by */
    private int ticks;

    /** List containing input information */
    private boolean[] moves;

    private Texture catNoteTexture;

    /** Location in the song (in samples) that the editor screen is currently centered on */
    private int songPosition;

    /** zoom-in factor */
    private float zoom;

    /** scroll speed of the editor*/
    private float speed;

    /** Spacing between beat lines in the song*/
    private int beat;

    /** Number of band member lanes in the level */
    private int laneNumber;

    /** Number of lines a band member has in the level */
    private int lineNumber;

    /** left edge of the first lane rectangle on the screen */
    private float leftBound;

    /** right edge of the first lane rectangle on the screen */
    private float rightBound;

    /** bottom edge of the lane rectangles on the screen */
    private float bottomBound;

    /** Spacing between lane rectangles*/
    private float laneSpacing;

    /** Width of the lane rectangles*/
    private float laneWidth;

    /** Height of the lane rectangles*/
    private float laneHeight;

    /** Color of the lane rectangle borders*/
    private Color laneBorderColor;

    /** Color of the lane rectangle background*/
    private Color laneBackColor;

    /**
     * 2D array of int lists representing the level's beat note information.
     * The first index is the lane.
     * The second index is the line
     * The int list is the song location (in samples) of the beat notes.
     * List is always sorted chronologically.
     *
     * */
    private LinkedList<Integer>[][] beatNotes;

    /**
     * 2D array of int lists representing the level's held note information.
     * The first index is the lane.
     * The second index is the line
     * The int list is the song location (in samples) of the held notes.
     * List is always sorted chronologically.
     *
     * */
    private LinkedList<Integer>[][] heldNotes;

    /**
     * array of int lists representing the level's switch note information.
     * The first index is the lane.
     * The int list is the song location (in samples) of the held notes.
     * List is always sorted chronologically.
     *
     * */
    private LinkedList<Integer>[] switchNotes;

    /**
     * Array of all notes in the level
     */
    private LinkedList<EditorNote> Notes;

    /**
     * Initializes the dimensions of the lane rectangles on the screen
     *
     * @param laneNumber number of band member lanes to be displayed
     */
    public void defineRectangles(int laneNumber){
        float num = (float) laneNumber;
        leftBound = ((float) canvas.getWidth())/8f;
        rightBound = ((float) canvas.getWidth())*(1f - 1f/32f);
        float width = rightBound - leftBound;
        float height = (float) canvas.getHeight();
        bottomBound = height/24f;
        laneSpacing = width/(16f*num);
        laneWidth = 15f*width/(16f*num);
        laneHeight = height/1.25f;
        laneBorderColor = new Color(Color.BLACK);
        laneBackColor = new Color(Color.PINK);
    }


    /**
     * Creates a new level editor with the given drawing context.
     *
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     */
    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        zoom = 1;
        speed = 2;
        beat = 1000;
        songPosition = (int) ((4/zoom)*beat);
        laneNumber = 4;
        lineNumber = 4;
        ticks = 0;
        defineRectangles(laneNumber);

        inputController = new InputController(4, 4);

        Notes = new LinkedList();
        //initialize lists
        beatNotes = new LinkedList[laneNumber][lineNumber];
        heldNotes = new LinkedList[laneNumber][lineNumber];
        switchNotes = new LinkedList[laneNumber];
        for (int lane = 0; lane < laneNumber; lane++){
            switchNotes[lane] = new LinkedList<Integer>();
            for (int line = 0; line < lineNumber; line++){
                beatNotes[lane][line] = new LinkedList<Integer>();
                heldNotes[lane][line] = new LinkedList<Integer>();
            }
        }
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        inputController = null;
        canvas = null;
    }

    /**
     * Populates this mode from the given the directory.
     *
     * The asset directory is a dictionary that maps string keys to assets.
     * Assets can include images, sounds, and fonts (and more). This
     * method delegates to the gameplay controller
     *
     * @param directory 	Reference to the asset directory.
     */
    public void populate(AssetDirectory directory) {
        background  = directory.getEntry("background",Texture.class);
        displayFont = directory.getEntry("times",BitmapFont.class);
        catNoteTexture = directory.getEntry("catnote", Texture.class);
        addNote(EditorNote.NoteType.BEAT, 1, 0, 4*beat, 0);
        addNote(EditorNote.NoteType.BEAT, 1, 1, 5*beat, 0);
    }

    public void addNote(EditorNote.NoteType type, int lane, int line, int songPos, int duration){
        EditorNote n = new EditorNote(type, lane, line, songPos, duration);
        n.setTexture(catNoteTexture);
        Notes.add(n);
        if (n.getType() == EditorNote.NoteType.BEAT){
            beatNotes[n.getLane()][n.getLine()].add(n.getPos());
        }
        if (n.getType() == EditorNote.NoteType.HELD){
            heldNotes[n.getLane()][n.getLine()].add(n.getPos());
        }
        if (n.getType() == EditorNote.NoteType.SWITCH){
            switchNotes[n.getLane()].add(n.getPos());
        }
    }
    /**
     * Converts a location in the song (in samples) to the vertical location on the screen
     *
     * @param position location in the song (in samples)
     * @return vertical location on the screen
     */
    private float songPosToScreenY(int position){
        float center = bottomBound + laneHeight/2f;
        int offset = songPosition - position;
        int top = (int) ((4/zoom)*beat);
        return center + (((float) offset)/((float) top))*(laneHeight/2f);
    }

    /**
     * Converts a lane and line value to a horizontal location on the screen.
     *
     * @param lane band member lane
     * @param line note line
     * @return horizontal location on the screen
     */
    private float lineToScreenX(int lane, int line){
        float x = leftBound;
        x += ((float) lane)*(laneWidth + laneSpacing);
        return x + (((float) line) + 0.5f)*(laneWidth/((float) lineNumber));
    }

    /**
     * Returns true if the vertical screen location corresponding to a song location is visible on screen
     * (in the lane rectangle).
     *
     * @param position location in the song (in samples)
     * @return true if vertical location is on screen
     */
    private boolean onScreen(int position){
        int offset = songPosition - position;
        int top = (int) ((4/zoom)*beat);
        if (Math.abs(offset) > top){
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * This method processes a single step in the game loop.
     */
    public void update(float delta){
        inputController.readInput();
        resolveAction();
        if (songPosition < (int) ((4/zoom)*beat)){
            songPosition = (int) ((4/zoom)*beat);
        }

        //update note positions
        for (EditorNote note : Notes){
            note.setX(lineToScreenX(note.getLane(), note.getLine()));
            note.setY(songPosToScreenY(note.getPos()));
            note.setOnScreen(onScreen(note.getPos()));
        }
    }


    private void resolveAction(){
        moves = inputController.getMoves();
        if (moves[0]){
            songPosition -= (int) ((speed/20f)*beat*(1/zoom));
        }
        if (moves[1]){
            songPosition += (int) ((speed/20f)*beat*(1/zoom));
        }
        if (moves[2]){
            zoom *= 1.01f;
        }
        if (moves[3]){
            zoom *= 0.99f;
        }

    }
    /**
    * Draw the status of this player mode.
    */
    public void draw(float delta){
        canvas.begin();
        canvas.drawBackground(background, 0, 0);

        //draw lane rectangles and lines
        float x = leftBound;
        float y = bottomBound;
        for (int lane = 0; lane < laneNumber; lane++){
            canvas.drawRect(x, y, x + laneWidth, y + laneHeight, laneBackColor, true);
            canvas.drawRect(x, y, x + laneWidth, y + laneHeight, laneBorderColor, false);

            //draw beat lines
            int beatLine = songPosition - (int) ((5/zoom)*beat); //line starts at the top of the screen space
            beatLine = beat*Math.round((float) (beatLine/((float )beat))); //set line to the nearest beat
            while (beatLine < songPosition + (int) ((5/zoom)*beat)){
                if (onScreen(beatLine)){
                    float l = songPosToScreenY(beatLine);
                    canvas.drawLine(x, l, x+laneWidth, l, 2, Color.GRAY);
                }
                beatLine += beat;
            }

            //draw note lines
            for (int line = 0; line < lineNumber-1; line++){
                x += laneWidth/lineNumber;
                canvas.drawLine(x, y, x, y + laneHeight, 1, Color.BLACK);
            }

            x += laneWidth/(lineNumber) + laneSpacing;

        }

        //draw notes
        for (EditorNote note: Notes){
            if (note.OnScreen()){
                note.draw(canvas, zoom);
            }
        }

        canvas.end();
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
            if (inputController.didExit() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        defineRectangles(laneNumber);
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

}
