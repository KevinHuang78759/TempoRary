package edu.cornell.gdiac.temporary.editor;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.audio.*;

import java.util.HashMap;
import java.util.LinkedList;

public class EditorMode implements Screen {

    /** number of beats per minute */
    private final int BPM = 100;

    /** Location and size of the UI buttons for changing place type and precision*/
    private Vector2 quarterPrecision1ButtonLocation;
    private Vector2 quarterPrecision2ButtonLocation;
    private Vector2 quarterPrecision3ButtonLocation;
    private Vector2 thirdPrecision1ButtonLocation;
    private Vector2 thirdPrecision2ButtonLocation;
    private Vector2 thirdPrecision3ButtonLocation;
    private Vector2 freeButtonLocation;
    private float buttonSize;

    /**Location and size of the UI buttons for changing note type */
    private Vector2 beatButtonLocation;
    private Vector2 heldButtonLocation;
    private Vector2 switchButtonLocation;
    private float noteButtonSize;

    /** Location and dimensions of the UI buttons for playing and tracking the song */
    private Vector2 playButtonLocation;
    private Vector2 trackButtonLocation;
    private Vector2 playButtonDimensions;
    private Vector2 trackButtonDimensions;

    /**Location and size of the UI buttons for undoing and redoing */
    private Vector2 undoButtonLocation;
    private Vector2 redoButtonLocation;
    private float undoButtonSize;

    /** The song*/
    private MusicQueue music;

    /** number of song samples per second*/
    private int sampleRate;

    /** number of frames per second */
    private int frameRate;

    /** background texture*/
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

    /** Texture for the cat notes*/
    private Texture catNoteTexture;

    /** animator to draw the cat notes*/
    private FilmStrip animator;

    /** center location for the cat note texture*/
    private Vector2 origin;

    /** Location in the song (in samples) that the editor screen is currently centered on */
    private int songPosition;

    /** zoom-in factor */
    private float zoom;

    /** scroll speed of the editor*/
    private float speed;

    /** Placement type for notes.
     * Quarter: Place notes on halfs, quarters, sixteenths of beat.
     * Third: Place notes on third, sixths, twelths of beat
     * Free: Place notes freely
     * Auto: Used when placing or removing notes with undo/redo
     * */
    private enum PlaceType{
        FREE,
        QUARTER,
        THIRD,
        AUTO
    }

    /** Current place type of notes.*/
    private PlaceType currentPlaceType;

    /** Precision of note placement (i.e PlaceType = Quarter with precision = 1 is half notes.
     * Precision = 2 is quarter notes. Precision = 3 is sixteenth notes.)*/
    private int precision;

    /** Type of note selected to be placed*/
    private EditorNote.NoteType selectedNoteType;

    /** Selected duration of held notes to be placed*/
    private int selectedDuration;

    private int duration = 0;

    /** True if the song is playing and the song bar is progressing through the level*/
    private boolean playing;

    /** Location in the song (in samples) the song bar is at */
    private int playPosition;

    /** True if the user selected to follow the song bar*/
    private boolean trackSong;

    /** Player actions which can be undone.
     * None: no action
     * Place: Placed a note in the level
     * Erase: Erased a note from the level
     * */
    private enum Action {
        NONE,
        PLACE,
        ERASE
    }

    /** List of the last actions performed (needed for undoing)*/
    private LinkedList<Action> lastActions;

    /** Notes corresponding to the last actions */
    private LinkedList<EditorNote> lastNotes;

    /** List of the last actions undone (needed for redoing)*/
    private LinkedList<Action> undoActions;

    /** Notes corresponding to the undone actions */
    private LinkedList<EditorNote> undoNotes;

    /** Spacing (in samples) between beat lines in the song*/
    private int beat;

    /** Amount of samples per frame */
    private int samplesPerFrame;

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

    private void defineSongCharacteristics() {
        beat = (int) (((float) sampleRate)/(((float) BPM)/60f));
        samplesPerFrame = sampleRate/frameRate;
    }
    /**
     * Initializes the dimensions of the lane rectangles on the screen
     *
     * @param laneNumber number of band member lanes to be displayed
     */
    private void defineRectangles(int laneNumber){
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

    private void defineButtonLocations(){
        float width = canvas.getWidth();
        float height = canvas.getHeight();

        //Place Type Buttons
        quarterPrecision1ButtonLocation.set(width*0.25f, height*0.95f);
        quarterPrecision2ButtonLocation.set(width*0.25f, height*0.95f - width*0.03f);
        quarterPrecision3ButtonLocation.set(width*0.25f, height*0.95f - width*0.06f);
        thirdPrecision1ButtonLocation.set(width*0.30f, height*0.95f);
        thirdPrecision2ButtonLocation.set(width*0.30f, height*0.95f - width*0.03f);
        thirdPrecision3ButtonLocation.set(width*0.30f, height*0.95f - width*0.06f);
        freeButtonLocation.set(width*0.35f, height*0.95f);
        buttonSize = width*0.025f;

        //Note Type Buttons
        beatButtonLocation.set(width*0.05f, height*0.7f);
        heldButtonLocation.set(width*0.05f, height*0.7f - width*0.15f);
        switchButtonLocation.set(width*0.05f, height*0.7f - width*0.3f);
        noteButtonSize = (1f/15f)*width*0.05f;

        //Play and Track Buttons
        playButtonLocation.set(width*0.45f, height * 0.90f);
        trackButtonLocation.set(width*0.55f, height*0.90f);
        playButtonDimensions.set(width*0.08f, height*0.06f);
        trackButtonDimensions.set(width*0.075f, height*0.05f);

        //Undo and Redo Buttons
        undoButtonLocation.set(width*0.85f, height*0.90f);
        redoButtonLocation.set(width*0.90f, height*0.90f);
        undoButtonSize = width*0.035f;
    }


    /**
     * Creates a new level editor with the given drawing context.
     *
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     */
    public EditorMode(GameCanvas canvas) {
        this.canvas = canvas;
        zoom = 2;
        speed = 2;
        currentPlaceType = PlaceType.QUARTER;
        precision = 2;
        selectedNoteType = EditorNote.NoteType.BEAT;
        duration = 0;

        laneNumber = 4;
        lineNumber = 4;
        ticks = 0;
        playing = false;
        playPosition = 0;
        trackSong = true;

        defineRectangles(laneNumber);
        quarterPrecision1ButtonLocation = new Vector2();
        quarterPrecision2ButtonLocation = new Vector2();
        quarterPrecision3ButtonLocation = new Vector2();
        thirdPrecision1ButtonLocation = new Vector2();
        thirdPrecision2ButtonLocation = new Vector2();
        thirdPrecision3ButtonLocation = new Vector2();
        freeButtonLocation = new Vector2();
        beatButtonLocation = new Vector2();
        heldButtonLocation = new Vector2();
        switchButtonLocation = new Vector2();
        playButtonLocation = new Vector2();
        trackButtonLocation = new Vector2();
        playButtonDimensions = new Vector2();
        trackButtonDimensions = new Vector2();
        undoButtonLocation = new Vector2();
        redoButtonLocation = new Vector2();
        defineButtonLocations();

        inputController = new InputController(4, 4);

        //initialize lists
        Notes = new LinkedList();
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

        //initialize actions list
        lastActions = new LinkedList<Action>();
        lastNotes = new LinkedList<EditorNote>();
        undoActions = new LinkedList<Action>();
        undoNotes = new LinkedList<EditorNote>();
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
        animator = new FilmStrip(catNoteTexture, 1, 4,4);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
        displayFont = directory.getEntry("lucida", BitmapFont.class);
        inputController.setEditorProcessor();
        music = directory.getEntry("noedell", MusicQueue.class);
        sampleRate = music.getSampleRate();
        frameRate = 60;
        defineSongCharacteristics();
        selectedDuration = beat;
        songPosition = (int) ((4/zoom)*beat);
    }

    /**
     * Converts a freely selected song position (in samples) to the nearest position allowed
     * by the placement type.
     * @param songPos song position (in samples) selected
     * @param placeType placement type
     * @return nearest song position (in samples) allowed by placement type
     */
    private int PlacePosition(int songPos, PlaceType placeType) {
        if (placeType == PlaceType.FREE || placeType == PlaceType.AUTO) {
            return songPos;
        } else {
            int modulus;
            if (placeType == PlaceType.QUARTER) {
                modulus = beat / (2 * (int) Math.pow((double) 2, (double) precision-1));
            } else {
                modulus = beat/ (3 * (int) Math.pow((double) 2, (double) precision-1));
            }
            return modulus * (Math.round(((float) songPos) / ((float) modulus)));
        }

    }

    /**
     * Adds a note into the level. Updates level data lists.
     *
     * @param type type selected of the note to be placed (Beat, Held, or Switch)
     * @param lane band member lane selected for the note to be placed
     * @param line note line selected for the note to be placed
     * @param songPos song position (in samples) selected for the note to be placed
     * @param duration duration selected for held notes to last
     * @return note which was placed
     *
     */
    public EditorNote addNote(EditorNote.NoteType type, int lane, int line, int songPos, int duration){
        int PlacePos = PlacePosition(songPos, currentPlaceType);
        boolean noteConflict = false;
        for (EditorNote note : Notes){
            if (note.getLane() == lane && note.getLine() == line) {
                if (currentPlaceType == PlaceType.FREE && note.getPos() < PlacePos + (int) ((1 / (4 * zoom) * beat)) && note.getPos() > PlacePos - (int) ((1 / (4 * zoom) * beat))) {
                    noteConflict = true;
                    System.out.println("Note Conflict");
                }
                if (currentPlaceType != PlaceType.FREE && note.getPos() == PlacePos){
                    noteConflict = true;
                    System.out.println("Note Conflict");
                }
            }
        }
        if (!noteConflict) {
            System.out.println("No Note Conflict");
            EditorNote n = new EditorNote(type, lane, line, PlacePos, duration);
            n.setTexture(catNoteTexture);
            Notes.add(n);
            if (n.getType() == EditorNote.NoteType.BEAT) {
                beatNotes[n.getLane()][n.getLine()].add(n.getPos());
            }
            if (n.getType() == EditorNote.NoteType.HELD) {
                heldNotes[n.getLane()][n.getLine()].add(n.getPos());
            }
            if (n.getType() == EditorNote.NoteType.SWITCH) {
                switchNotes[n.getLane()].add(n.getPos());
            }
            return n;
        }
        return null;
    }

    /**
     * Deletes a note from the level. Updates the level data lists.
     *
     * @param lane band member lane selected for note to be removed
     * @param line note line selected for note to be removed
     * @param songPos song position (in samples) selected for note to be removed
     * @return note which was removed
     */
    public EditorNote deleteNote(int lane, int line, int songPos){
        int minDistance = Integer.MAX_VALUE;
        EditorNote minNote = null;
        for (EditorNote note : Notes){
            if (note.getLane() == lane && note.getLine() == line) {
                if (note.getPos() < songPos + (int) ((1 / (4 * zoom) * beat)) && note.getPos() > songPos - (int) ((1 / (4 * zoom) * beat))) {
                    if (Math.abs(note.getPos() - songPos) < minDistance){
                        minDistance = Math.abs(note.getPos());
                        minNote = note;
                    }
                }
            }
        }
        if (minNote != null){
            Notes.removeFirstOccurrence(minNote);
            if (minNote.getType() == EditorNote.NoteType.BEAT){
                beatNotes[minNote.getLane()][minNote.getLine()].removeFirstOccurrence(minNote.getPos());
            }
            if (minNote.getType() == EditorNote.NoteType.HELD){
                heldNotes[minNote.getLane()][minNote.getLine()].removeFirstOccurrence(minNote.getPos());
            }
            if (minNote.getType() == EditorNote.NoteType.SWITCH){
                switchNotes[minNote.getLane()].removeFirstOccurrence(minNote.getPos());
            }
        }
        return minNote;
    }

    private void undo(){
        if (lastActions.size() != 0) {
            PlaceType lastPlaceType = currentPlaceType;
            currentPlaceType = PlaceType.AUTO;
            Action lastAction = lastActions.pollLast();
            EditorNote lastNote = lastNotes.pollLast();
            if (lastAction == Action.PLACE){
                deleteNote(lastNote.getLane(), lastNote.getLine(), lastNote.getPos());
            }
            if (lastAction == Action.ERASE){
                addNote(lastNote.getType(), lastNote.getLane(), lastNote.getLine(), lastNote.getPos(), lastNote.getDuration());
            }
            undoActions.add(lastAction);
            undoNotes.add(lastNote);
            currentPlaceType = lastPlaceType;
        }
    }

    private void redo(){
        if (undoActions.size() != 0) {
            PlaceType lastPlaceType = currentPlaceType;
            currentPlaceType = PlaceType.AUTO;
            Action undidAction = undoActions.pollLast();
            EditorNote undidNote = undoNotes.pollLast();
            if (undidAction == Action.PLACE){
                addNote(undidNote.getType(), undidNote.getLane(), undidNote.getLine(), undidNote.getPos(), undidNote.getDuration());
            }
            if (undidAction == Action.ERASE){
                deleteNote(undidNote.getLane(), undidNote.getLine(), undidNote.getPos());
            }
            lastActions.add(undidAction);
            lastNotes.add(undidNote);
            currentPlaceType = lastPlaceType;
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

        if (playing){
            playPosition += samplesPerFrame;
            if (trackSong) {
                songPosition = playPosition;
            }
        }

        if (songPosition < (int) ((4/zoom)*beat)){
            songPosition = (int) ((4/zoom)*beat);
        }

        //update note positions
        for (EditorNote note : Notes){
            note.setX(lineToScreenX(note.getLane(), note.getLine()));
            note.setY(songPosToScreenY(note.getPos()));
            note.setOnScreen(onScreen(note.getPos()));
        }

        if (inputController.didExit()) {
            listener.exitScreen(this, 0);
        }
    }

    /**
     * Lane and Line corresponding to a horizontal screen position (returns {-1, -1} if not on a line)
     * @param x the horizontal screen position
     * @return the corresponding lane and line
     */
    private int[] screenXtoline(float x){
        for (int lane = 0; lane < laneNumber; lane++){
            float laneLeft = leftBound + ((float) lane)*(laneWidth + laneSpacing);
            float laneRight = laneLeft + laneWidth;
            if (x > laneLeft && x < laneRight){
                for (int line = 0; line < lineNumber; line++){
                    float lineLeft = laneLeft + ((float) line)*(laneWidth/((float) lineNumber));
                    float lineRight = lineLeft + (laneWidth/((float) lineNumber));
                    if (x > lineLeft && x < lineRight){
                        int[] a = {lane, line};
                        return a;
                    }
                }
            }
        }
        int[] a = {-1, -1};
        return a;
    }

    /**
     * The song position (in samples) corresponding t0 a vertical screen position (returns -1 if not on a lane)
     * @param y the vertical screen position
     * @return the corresponding song position (in samples)
     */
    private int screenYtoSongPos(float y){
        if (y > bottomBound && y < bottomBound + laneHeight){
            float centerY = y - (bottomBound + (laneHeight/2));
            return songPosition - (int) (((int) ((4/zoom)*beat))*(2*centerY/laneHeight));
        }
        return -1;
    }

    public void togglePlay(){
        if (playing){
            playing = false;
            music.pause();
        } else {
            playing = true;
            music.play();
        }
    }

    /**
     * Checks if the given screen location is on a UI button and if so activates the button effect.
     * @param x the horizontal screen location
     * @param y the vertical screen location
     */
    private void buttonClick(float x, float y){
        if (x >= quarterPrecision1ButtonLocation.x && x <= quarterPrecision1ButtonLocation.x + buttonSize){
            if (y >= quarterPrecision1ButtonLocation.y && y <= quarterPrecision1ButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.QUARTER;
                precision = 1;
            }
        }
        if (x >= quarterPrecision2ButtonLocation.x && x <= quarterPrecision2ButtonLocation.x + buttonSize){
            if (y >= quarterPrecision2ButtonLocation.y && y <= quarterPrecision2ButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.QUARTER;
                precision = 2;
            }
        }
        if (x >= quarterPrecision3ButtonLocation.x && x <= quarterPrecision3ButtonLocation.x + buttonSize){
            if (y >= quarterPrecision3ButtonLocation.y && y <= quarterPrecision3ButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.QUARTER;
                precision = 3;
            }
        }
        if (x >= thirdPrecision1ButtonLocation.x && x <= thirdPrecision1ButtonLocation.x + buttonSize){
            if (y >= thirdPrecision1ButtonLocation.y && y <= thirdPrecision1ButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.THIRD;
                precision = 1;
            }
        }
        if (x >= thirdPrecision2ButtonLocation.x && x <= thirdPrecision2ButtonLocation.x + buttonSize){
            if (y >= thirdPrecision2ButtonLocation.y && y <= thirdPrecision2ButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.THIRD;
                precision = 2;
            }
        }
        if (x >= thirdPrecision3ButtonLocation.x && x <= thirdPrecision3ButtonLocation.x + buttonSize){
            if (y >= thirdPrecision3ButtonLocation.y && y <= thirdPrecision3ButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.THIRD;
                precision = 3;
            }
        }
        if (x >= freeButtonLocation.x && x <= freeButtonLocation.x + buttonSize){
            if (y >= freeButtonLocation.y && y <= freeButtonLocation.y + buttonSize){
                currentPlaceType = PlaceType.FREE;
                precision = 1;
            }
        }
        if (x >=  beatButtonLocation.x - 7.5f*noteButtonSize && x <= beatButtonLocation.x + 7.5f*noteButtonSize){
            if (y >= beatButtonLocation.y - 7.5f*noteButtonSize && y <= beatButtonLocation.y + 7.5f*noteButtonSize){
                selectedNoteType = EditorNote.NoteType.BEAT;
            }
        }
        if (x >=  heldButtonLocation.x - 7.5f*noteButtonSize && x <= heldButtonLocation.x + 7.5f*noteButtonSize){
            if (y >= heldButtonLocation.y - 7.5f*noteButtonSize && y <= heldButtonLocation.y + 7.5f*noteButtonSize){
                selectedNoteType = EditorNote.NoteType.HELD;
            }
        }
        if (x >=  switchButtonLocation.x - 7.5f*noteButtonSize && x <= switchButtonLocation.x + 7.5f*noteButtonSize){
            if (y >= switchButtonLocation.y - 7.5f*noteButtonSize && y <= switchButtonLocation.y + 7.5f*noteButtonSize){
                selectedNoteType = EditorNote.NoteType.SWITCH;
            }
        }

        if (x >= playButtonLocation.x && x <= playButtonLocation.x + playButtonDimensions.x){
            if (y >= playButtonLocation.y && y <= playButtonLocation.y + playButtonDimensions.y){
                togglePlay();
            }
        }
        if (x >= trackButtonLocation.x && x <= trackButtonLocation.x + trackButtonDimensions.x){
            if (y >= trackButtonLocation.y && y <= trackButtonLocation.y + trackButtonDimensions.y){
                trackSong = !trackSong;
            }
        }
        if (x >= undoButtonLocation.x && x <= undoButtonLocation.x + undoButtonSize){
            if (y >= undoButtonLocation.y && y <= undoButtonLocation.y + undoButtonSize){
                undo();
            }
        }
        if (x >= redoButtonLocation.x && x <= redoButtonLocation.x + undoButtonSize){
            if (y >= redoButtonLocation.y && y <= redoButtonLocation.y + undoButtonSize){
                redo();
            }
        }
    }

    private void resolveAction(){
        //Get Movement Input
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

        //Get Place Note Input
        if (inputController.didClick()){
            float mouseX = inputController.getMouseX();
            float mouseY = canvas.getHeight() - inputController.getMouseY();
            int clickedLane = screenXtoline(mouseX)[0];
            int clickedLine = screenXtoline(mouseX)[1];
            int clickedSongPosition = screenYtoSongPos(mouseY);
            if (clickedLane != -1 && clickedSongPosition != -1) {
                if (selectedNoteType == EditorNote.NoteType.SWITCH){
                    clickedLine = -1;
                }
                EditorNote note = addNote(selectedNoteType, clickedLane, clickedLine, clickedSongPosition, duration);
                if (note != null){
                    lastActions.add(Action.PLACE);
                    lastNotes.add(note);
                    undoActions.clear();
                    undoNotes.clear();
                }
            }
            buttonClick(mouseX, mouseY);
        }

        //Get Erase Note Input
        if (inputController.didErase()){
            float mouseX = inputController.getMouseX();
            float mouseY = canvas.getHeight() - inputController.getMouseY();
            int clickedLane = screenXtoline(mouseX)[0];
            int clickedLine = screenXtoline(mouseX)[1];
            int clickedSongPosition = screenYtoSongPos(mouseY);
            if (clickedLane != -1 && clickedSongPosition != -1) {
                if (selectedNoteType == EditorNote.NoteType.SWITCH){
                    clickedLine = -1;
                }
                EditorNote note = deleteNote(clickedLane, clickedLine, clickedSongPosition);
                if (note != null){
                    lastActions.add(Action.ERASE);
                    lastNotes.add(note);
                    undoActions.clear();
                    undoNotes.clear();
                }
            }
        }

        //Get Placement Type Input
        if (inputController.didSetFree()){
            currentPlaceType = PlaceType.FREE;
        }
        if (inputController.didSetThird()){
            currentPlaceType = PlaceType.THIRD;
        }
        if (inputController.didSetQuarter()){
            currentPlaceType = PlaceType.QUARTER;
        }

        //Get Placement Note Type Input
        if (inputController.setSwitchNotes()){
            selectedNoteType = EditorNote.NoteType.SWITCH;
            duration = 0;
        }
        if (inputController.setBeatNotes()){
            selectedNoteType = EditorNote.NoteType.BEAT;
            duration = 0;
        }
        if (inputController.setHeldNotes()){
            selectedNoteType = EditorNote.NoteType.HELD;
            duration = selectedDuration;
        }

        //Get Placement Precision Input
        if (inputController.didPrecision1()){
            precision = 1;
        }
        if (inputController.didPrecision2()){
            precision = 2;
        }
        if (inputController.didPrecision3()){
            precision = 3;
        }

        //Get Undo/Redo Input
        if (inputController.didUndo()){
            undo();
        }
        if (inputController.didRedo()){
            redo();
        }

        //Get Play input
        if (inputController.didPressPlay()){
            togglePlay();
        }
        if (inputController.didPressTrack()){
            trackSong = !trackSong;
        }
        if (inputController.didResetSong()){
            playing = false;
            playPosition = 0;
            if (trackSong){
                songPosition = 0;
            }
        }

        //Get Speed Input
        if (inputController.didSpeedUp()){
            speed = 6;
        } else {
            speed = 2;
        }
    }

    /**
     * Returns the horizontal screen position of the left edge of the specified lane
     * @param lane band member lane specified
     * @return horizontal screen position of the left edge
     */
    public float getLaneEdge (int lane){
        return leftBound + ((float) lane)*(laneWidth + laneSpacing);
    }

    /**
     * Returns the horizontal screen width of a lane
     * @return horizontal screen width of a lane
     */
    public float getLaneWidth() {
        return laneWidth;
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
        float l;
        for (int lane = 0; lane < laneNumber; lane++){
            canvas.drawRect(x, y, x + laneWidth, y + laneHeight, laneBackColor, true);
            canvas.drawRect(x, y, x + laneWidth, y + laneHeight, laneBorderColor, false);

            //draw beat lines
            int beatLine = songPosition - (int) ((5/zoom)*beat); //line starts at the top of the screen space
            beatLine = beat*Math.round((float) (beatLine/((float )beat))); //set line to the nearest beat
            while (beatLine < songPosition + (int) ((5/zoom)*beat)){
                if (onScreen(beatLine)){
                    l = songPosToScreenY(beatLine);
                    canvas.drawLine(x, l, x+laneWidth, l, 3, Color.CYAN);
                }
                if (currentPlaceType == PlaceType.QUARTER || currentPlaceType == PlaceType.FREE){
                    for (int i = 0; i < 3; i++){
                        beatLine += beat / 4;
                        if (onScreen(beatLine)) {
                            l = songPosToScreenY(beatLine);
                            canvas.drawLine(x, l, x + laneWidth, l,2, Color.GRAY);
                        }
                    }
                    beatLine += beat / 4;
                }
                if (currentPlaceType == PlaceType.THIRD){
                    for (int i = 0; i < 2; i++){
                        beatLine += beat / 3;
                        if (onScreen(beatLine)) {
                            l = songPosToScreenY(beatLine);
                            canvas.drawLine(x, l, x + laneWidth, l, 2, Color.GRAY);
                        }
                    }
                    beatLine += beat / 3;
                }
            }

            //draw note lines
            float x2 = x;
            for (int line = 0; line < lineNumber-1; line++){
                x2 += laneWidth/lineNumber;
                canvas.drawLine(x2, y, x2, y + laneHeight, 1, Color.BLACK);
            }

            //draw song bar
            if (onScreen(playPosition)){
                l = songPosToScreenY(playPosition);
                if (playing) {
                    canvas.drawLine(x, l, x + laneWidth, l, 8, Color.RED);
                } else {
                    canvas.drawLine(x, l, x + laneWidth, l, 5, Color.MAROON);
                }
            }

            x += laneWidth + laneSpacing;

        }

        //draw notes
        for (EditorNote note: Notes){
            if (note.OnScreen()){
                note.draw(canvas, zoom, getLaneEdge(note.getLane()), getLaneWidth());
            }
        }

        //draw place type buttons
        canvas.drawRect(quarterPrecision1ButtonLocation, buttonSize, buttonSize, Color.RED, true);
        canvas.drawRect(quarterPrecision1ButtonLocation, buttonSize, buttonSize, Color.BLACK, false);
        canvas.drawRect(quarterPrecision2ButtonLocation, buttonSize, buttonSize, Color.RED, true);
        canvas.drawRect(quarterPrecision2ButtonLocation, buttonSize, buttonSize, Color.BLACK, false);
        canvas.drawRect(quarterPrecision3ButtonLocation, buttonSize, buttonSize, Color.RED, true);
        canvas.drawRect(quarterPrecision3ButtonLocation, buttonSize, buttonSize, Color.BLACK, false);

        canvas.drawRect(thirdPrecision1ButtonLocation, buttonSize, buttonSize, Color.ORANGE, true);
        canvas.drawRect(thirdPrecision1ButtonLocation, buttonSize, buttonSize, Color.BLACK, false);
        canvas.drawRect(thirdPrecision2ButtonLocation, buttonSize, buttonSize, Color.ORANGE, true);
        canvas.drawRect(thirdPrecision2ButtonLocation, buttonSize, buttonSize, Color.BLACK, false);
        canvas.drawRect(thirdPrecision3ButtonLocation, buttonSize, buttonSize, Color.ORANGE, true);
        canvas.drawRect(thirdPrecision3ButtonLocation, buttonSize, buttonSize, Color.BLACK, false);

        canvas.drawRect(freeButtonLocation, buttonSize, buttonSize, Color.YELLOW, true);
        canvas.drawRect(freeButtonLocation, buttonSize, buttonSize, Color.BLACK, false);

        float selectBoxX;
        float selectBoxY;
        if (precision == 1) {
            selectBoxY = quarterPrecision1ButtonLocation.y;
        } else if (precision == 2){
            selectBoxY = quarterPrecision2ButtonLocation.y;
        } else {
            selectBoxY = quarterPrecision3ButtonLocation.y;
        }
        if (currentPlaceType == PlaceType.QUARTER){
            selectBoxX = quarterPrecision1ButtonLocation.x;
        } else if (currentPlaceType == PlaceType.THIRD){
            selectBoxX = thirdPrecision1ButtonLocation.x;
        } else {
            selectBoxX = freeButtonLocation.x;
            selectBoxY = quarterPrecision1ButtonLocation.y;
        }

        canvas.drawRect(selectBoxX + buttonSize/4f, selectBoxY + buttonSize/4f, selectBoxX + buttonSize*(1f-1f/4f), selectBoxY + buttonSize*(1f- 1f/4f), Color.CYAN, true);

        //draw note type buttons
        animator.setFrame(0);
        selectBoxX = beatButtonLocation.x;
        if (selectedNoteType == EditorNote.NoteType.BEAT){
            selectBoxY = beatButtonLocation.y;
        }
        if (selectedNoteType == EditorNote.NoteType.HELD){
            selectBoxY = heldButtonLocation.y;
        }
        if (selectedNoteType == EditorNote.NoteType.SWITCH){
            selectBoxY = switchButtonLocation.y;
        }
        canvas.draw(animator, Color.CYAN, origin.x, origin.y, selectBoxX, selectBoxY, 0.0f, noteButtonSize*1.4f, noteButtonSize*1.4f);

        canvas.draw(animator, Color.WHITE, origin.x, origin.y, beatButtonLocation.x, beatButtonLocation.y, 0.0f, noteButtonSize, noteButtonSize);
        canvas.draw(animator, Color.SALMON, origin.x, origin.y, heldButtonLocation.x, heldButtonLocation.y, 0.0f, noteButtonSize, noteButtonSize);
        canvas.draw(animator, Color.GREEN, origin.x, origin.y, switchButtonLocation.x, switchButtonLocation.y, 0.0f, noteButtonSize, noteButtonSize);

        //draw play/track buttons
        canvas.drawRect(playButtonLocation, playButtonDimensions.x, playButtonDimensions.y, Color.GREEN, true);
        canvas.drawRect(playButtonLocation, playButtonDimensions.x, playButtonDimensions.y, Color.BLACK, false);

        canvas.drawRect(trackButtonLocation, trackButtonDimensions.x, trackButtonDimensions.y, Color.FOREST, true);
        canvas.drawRect(trackButtonLocation, trackButtonDimensions.x, trackButtonDimensions.y, Color.BLACK, false);

        if (playing) {
            selectBoxX = playButtonLocation.x;
            selectBoxY = playButtonLocation.y;
            canvas.drawRect(selectBoxX + playButtonDimensions.x / 4f, selectBoxY + playButtonDimensions.y / 4f, selectBoxX + playButtonDimensions.x * (1f - 1f / 4f), selectBoxY + playButtonDimensions.y * (1f - 1f / 4f), Color.RED, true);
        }
        if (trackSong) {
            selectBoxX = trackButtonLocation.x;
            selectBoxY = trackButtonLocation.y;
            canvas.drawRect(selectBoxX + trackButtonDimensions.x / 4f, selectBoxY + trackButtonDimensions.y / 4f, selectBoxX + trackButtonDimensions.x * (1f - 1f / 4f), selectBoxY + trackButtonDimensions.y * (1f - 1f / 4f), Color.MAROON, true);
        }

        //draw undo/redo buttons
        canvas.drawRect(undoButtonLocation, undoButtonSize, undoButtonSize, Color.GRAY, true);
        canvas.drawRect(undoButtonLocation, undoButtonSize, undoButtonSize, Color.BLACK, false);
        canvas.drawRect(redoButtonLocation, undoButtonSize, undoButtonSize, Color.GRAY, true);
        canvas.drawRect(redoButtonLocation, undoButtonSize, undoButtonSize, Color.BLACK, false);


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
        defineButtonLocations();
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
