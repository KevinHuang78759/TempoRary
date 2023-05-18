package edu.cornell.gdiac.temporary.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.ExitCode;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.temporary.InputController;
import edu.cornell.gdiac.temporary.SoundController;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.audio.*;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

public class EditorMode implements Screen {

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

    /**Location and size of the UI buttons for placing competency drain flags */
    private Vector2 flagButtonLocation;
    private float flagButtonSize;
    private Vector2 flagSettingsButtonLocation;
    private float flagSettingsButtonSize;

    private Vector2 flagSettingsScreenLocation;
    private Vector2 flagSettingsScreenDimensions;
    private Vector2 flagSettingsTitleLocation;
    private Vector2 closeFlagSettingsButtonLocation;
    private float closeFlagSettingsButtonSize;
    private Vector2 rateButtonLocation;
    private Vector2 gainButtonLocation;
    private Vector2 rateTextLocation;
    private Vector2 gainTextLocation;
    private float flagRateButtonSize;

    /**Location and size of the UI buttons for placing random hits */
    private Vector2 hitButtonLocation;
    private float hitButtonSize;
    private Vector2 hitSettingsButtonLocation;
    private float hitSettingsButtonSize;

    private Vector2 hitSettingsScreenLocation;
    private Vector2 hitSettingsScreenDimensions;
    private Vector2 hitSettingsTitleLocation;
    private Vector2 closeHitSettingsButtonLocation;
    private float closeHitSettingsButtonSize;
    private Vector2[] probabilityButtonLocations;

    private Vector2[] probabilityTextLocations;
    private float probabilityButtonSize;


    /**Location and size of the UI buttons for changing duration of held notes */
    private Vector2 upDurationButtonLocation;
    private Vector2 downDurationButtonLocation;
    private float durationButtonSize;

    /** Location and dimensions of the UI buttons for playing and tracking the song */
    private Vector2 playButtonLocation;
    private Vector2 trackButtonLocation;

    private Vector2 resetButtonLocation;
    private Vector2 playButtonDimensions;
    private Vector2 trackButtonDimensions;
    private float resetButtonSize;

    /**Location and size of the UI buttons for undoing and redoing */
    private Vector2 undoButtonLocation;
    private Vector2 redoButtonLocation;
    private float undoButtonSize;

    /** Location and size of the UI button for opening level settings menu */

    private Vector2 settingsButtonLocation;
    private float settingsButtonSize;

    /** Location and dimensions of the settings screen */
    private Vector2 settingsScreenLocation;
    private Vector2 settingsScreenDimensions;

    /** Location and size of the settings UI buttons */
    private Vector2 changeBPMButtonLocation;
    private Vector2 changeLaneButtonLocation;
    private Vector2 changeLineButtonLocation;
    private Vector2 changeNameButtonLocation;
    private Vector2 changeSongNameButtonLocation;
    private Vector2 changeFallSpeedButtonLocation;
    private Vector2 changeMaxCompButtonLocation;
    private Vector2 changeInstrumentsButtonLocation;
    private Vector2 changeThresholdsButtonLocation;

    private Vector2 closeSettingsButtonLocation;
    private float settingsButtonsSize;

    /** Location of the settings text */

    private Vector2 settingsTitleTextLocation;
    private Vector2 nameTextLocation;
    private Vector2 songNameTextLocation;
    private Vector2 BPMTextLocation;
    private Vector2 maxCompTextLocation;
    private Vector2 fallSpeedTextLocation;
    private Vector2 instrumentsTextLocation;
    private Vector2 thresholdsTextLocation;
    private Vector2 laneTextLocation;
    private Vector2 lineTextLocation;

    /** Location and dimensions of Instruments Settings */
    private Vector2 instrumentsSettingsScreenLocation;
    private Vector2 instrumentsSettingsScreenDimensions;
    private Vector2 instrumentsSettingsCloseButtonLocation;
    private Vector2[] instrumentsButtonLocations;
    private Vector2[] instrumentsTextLocations;
    private float instrumentSettingsButtonSize;

    /** Location and dimensions of the text prompt bar */
    private Vector2 textPromptBarLocation;
    private Vector2 textPromptBarDimensions;

    private Vector2 promptTextLocation;

    /** File Writer for writing level to JSON*/
    private FileWriter file;

    /** The default JSON level to load from */
    private JsonValue defaultLevel;

    /** The song*/
    private MusicQueue music;

    /** The song name the asset loader needs to load the song*/
    private String songName;

    /** The name of the level*/
    private String levelName;

    /** number of song samples per second*/
    private int sampleRate;

    /** number of frames per second */
    private int frameRate;

    /** number of beats per minute */
    private int BPM;

    /** background texture*/
    private Texture background;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;

    /** Plays Sound Effects */
    private SoundController<String> soundController;

    /** Whether this player mode is still active */
    private boolean active;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** List containing input information */
    private boolean[] moves;

    /** Texture for the cat notes*/
    private Texture catNoteTexture;

    /** Texture for the hit star icon */
    private Texture hitStarTexture;

    /** animator to draw the cat notes*/
    private FilmStrip catNoteAnimator;

    /** center location for the cat note texture*/
    private Vector2 catNoteOrigin;

    /** animator to draw the hits and flags*/
    private FilmStrip hitAnimator;

    /** center location for the hit and flag texture*/
    private Vector2 hitOrigin;

    /** animator to draw the hits and flags*/
    private FilmStrip flagAnimator;

    /** center location for the hit and flag texture*/
    private Vector2 flagOrigin;

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

    /** which lane the notes are heard when playing */
    private int meowLane;

    /** Type of note selected to be placed*/
    private EditorNote.NoteType selectedNoteType;

    /** Selected duration of held notes to be placed*/
    private int selectedDuration;

    /** Selected probability distribution of random hits to be placed */
    private int[] selectedProbabilities;

    /** Selected loss rate of competency flags to be placed */
    private int selectedLossRate;

    /** Selected note reward of competency flags to be placed */
    private int selectedNoteGain;

    /** True if the user is placing the start location*/
    private boolean placing_start;

    /** True if the user is placing notes */
    private boolean placing_notes;

    /** True if the user is placing competency drain flags*/
    private boolean placing_flags;

    /** True if the user is placing random hits*/
    private boolean placing_hits;

    /** True if the song is playing and the song bar is progressing through the level*/
    private boolean playing;

    /** True if the user is in the level settings */
    private boolean setting;

    /** True if the user is in the random hit settings */
    private boolean hitSetting;

    /** True if the user is in the competency flag settings */
    private boolean flagSetting;

    /** True if the user is in the instruments setting */
    private boolean instrumentSetting;

    /** True if the user is in the thresholds setting */
    private boolean thresholdSetting;

    /** True if the user is typing in any text prompt */
    private boolean typing;

    /** True if the user is typing in the BPM text prompt */
    private boolean typingBPM;

    /** True if the user is typing in the lane number text prompt */
    private boolean typingLaneNum;

    /** True if the user is typing in the line number text prompt */
    private boolean typingLineNum;

    /** True if the user is typing in the level name text prompt */
    private boolean typingName;

    /** True if the user is typing in the song name text prompt */
    private boolean typingSongName;

    /** True if the user is typing in the max competency text prompt */
    private boolean typingMaxComp;

    /** True if the user is typing in the note fall speed text prompt */
    private boolean typingFallSpeed;

    /** True if the user is typing in the competency loss rate text prompt */
    private boolean typingLossRate;

    /** True if the user is typing in the note competency reward text prompt */
    private boolean typingNoteGain;

    /** True if the user is typing in the A Score Threshold text prompt */
    private boolean typingThresholdA;

    /** True if the user is typing in the B Score Threshold text prompt */
    private boolean typingThresholdB;

    /** True if the user is typing in the C Score Threshold text prompt */
    private boolean typingThresholdC;

    /** True if the user is typing in the S Score Threshold text prompt */
    private boolean typingThresholdS;

    /** True if the user is typing in the hit probabilities text prompt */
    private boolean[] typingProbabilities;

    /** String the user is typing in a text prompt */
    private String typedString;

    /** Location in the song (in samples) the song bar is at */
    private int playPosition;

    /** The location in the song (in samples) where the song begins to play from*/
    private int startPosition;

    /** True if the user selected to follow the song bar*/
    private boolean trackSong;

    /** True if the user just moved the song to their position */
    private boolean relocated;

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

    /** The Maximum amount a band member's competency bar can be filled to */
    private int maxCompetency;

    /** The speed at which notes fall down when playing the level */
    private int fallSpeed;

    private enum Instrument {
        VIOLIN,
        PIANO,
        DRUM,
        VOICE,
    }

    /** Instruments played by the band members in the level */
    private Instrument[] instruments;

    /** Score Thresholds for letter grades */
    private int AThreshold;
    private int BThreshold;
    private int CThreshold;
    private int SThreshold;

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
     * The int array list is the song location and duration (in samples) of the held notes.
     * List is always sorted chronologically.
     *
     * */
    private LinkedList<Integer[]>[][] heldNotes;

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
     * Array of all competency loss rate flags in the level
     */
    private LinkedList<EditorFlag> Flags;

    /**
     * Array of all random hits in the level
     */
    private LinkedList<EditorHit> Hits;

    private void defineSongCharacteristics() {
        beat = (int) (((float) sampleRate)/(((float) BPM)/60f));
        samplesPerFrame = sampleRate/frameRate;
    }

    /** array of the horizontal locations of all the left edges of the lane rectangles */
    private float[] laneEdges;

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
        laneEdges = new float[laneNumber];
        float x = leftBound;
        for (int lane = 0; lane< laneNumber; lane++){
            laneEdges[lane] = x;
            x += laneWidth + laneSpacing;
        }
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
        heldButtonLocation.set(width*0.05f, height*0.7f - width*0.10f);
        switchButtonLocation.set(width*0.05f, height*0.7f - width*0.2f);
        noteButtonSize = (1f/15f)*width*0.05f;

        //Duration Type Buttons
        upDurationButtonLocation.set(width*0.09f,  height*0.7f - width*0.09f);
        downDurationButtonLocation.set(width*0.09f,  height*0.7f - width*0.13f);
        durationButtonSize = width*0.02f;

        //Flag Type Buttons
        flagButtonLocation.set(width*0.035f, height*0.7f - width*0.33f);
        flagButtonSize = (1f/20f)*width*0.05f;
        flagSettingsButtonLocation.set(width*0.075f, height*0.7f - width*0.35f);
        flagSettingsButtonSize = width*0.03f;
        flagSettingsScreenLocation.set(width*0.15f, height*0.35f);
        flagSettingsScreenDimensions.set(width*0.6f, height*0.3f);
        flagSettingsTitleLocation.set(width*0.21f, height*0.62f);
        closeFlagSettingsButtonLocation.set(width*0.16f, height*0.64f - width*0.04f);
        closeFlagSettingsButtonSize = width*0.04f;
        rateButtonLocation.set(width*0.3f, height*0.45f);
        gainButtonLocation.set(width*0.55f, height*0.45f);
        rateTextLocation.set(width*0.27f, height*0.40f);
        gainTextLocation.set(width*0.52f, height*0.40f);
        flagRateButtonSize = width*0.04f;

        //Hit Type Buttons
        hitButtonLocation.set(width*0.035f, height*0.7f - width*0.4f);
        hitButtonSize = (1f/20f)*width*0.05f;
        hitSettingsButtonLocation.set(width*0.075f, height*0.7f - width*0.42f);
        hitSettingsButtonSize = width*0.03f;
        hitSettingsScreenLocation.set(width*0.25f, height*0.35f);
        hitSettingsScreenDimensions.set(width*0.5f, height*0.3f);
        hitSettingsTitleLocation.set(width*0.35f, height*0.62f);
        closeHitSettingsButtonLocation.set(width*0.26f, height*0.64f - width*0.04f);
        closeHitSettingsButtonSize = width*0.04f;
        for (int i=0; i < laneNumber; i++){
            probabilityButtonLocations[i].set(width*0.3f - width*0.02f + (width*0.4f/((float) laneNumber))*(((float) i) + 1f/2f), height*0.45f);
            probabilityTextLocations[i].set(width*0.3f - width*0.02f + (width*0.4f/((float) laneNumber))*(((float) i) + 1f/2f), height*0.4f);
        }
        probabilityButtonSize = width*0.04f;

        //Play and Track Buttons
        playButtonLocation.set(width*0.50f, height * 0.90f);
        trackButtonLocation.set(width*0.60f, height*0.90f);
        resetButtonLocation.set(width*0.46f, height*0.90f);
        playButtonDimensions.set(width*0.08f, height*0.06f);
        trackButtonDimensions.set(width*0.075f, height*0.05f);
        resetButtonSize = height*0.035f;

        //Undo and Redo Buttons
        undoButtonLocation.set(width*0.85f, height*0.90f);
        redoButtonLocation.set(width*0.90f, height*0.90f);
        undoButtonSize = width*0.035f;

        //Open Settings Button
        settingsButtonLocation.set(width*0.05f, height*0.90f);
        settingsButtonSize = width*0.045f;

        //Settings
        settingsScreenLocation.set(width*0.2f, height*0.1f);
        settingsScreenDimensions.set(width*0.6f, height*0.8f);
        changeNameButtonLocation.set(width*0.25f, height*0.65f);
        changeSongNameButtonLocation.set(width*0.55f, height*0.65f);
        changeBPMButtonLocation.set(width*0.25f, height*0.55f);
        changeFallSpeedButtonLocation.set(width*0.55f, height*0.55f);
        changeLaneButtonLocation.set(width*0.25f, height*0.45f);
        changeLineButtonLocation.set(width*0.55f, height*0.45f);
        changeMaxCompButtonLocation.set(width*0.25f, height*0.35f);
        changeInstrumentsButtonLocation.set(width*0.25f, height*0.25f);
        changeThresholdsButtonLocation.set(width*0.25f, height*0.15f);
        closeSettingsButtonLocation.set(width*0.21f, height*0.825f);
        settingsButtonsSize = width*0.04f;

        //Settings Text
        settingsTitleTextLocation.set(width*0.45f, height*0.85f);
        nameTextLocation.set(width*0.3f, height*0.69f);
        songNameTextLocation.set(width*0.6f, height*0.69f);
        BPMTextLocation.set(width*0.3f, height*0.59f);
        fallSpeedTextLocation.set(width*0.6f, height*0.59f);
        laneTextLocation.set(width*0.3f, height*0.49f);
        lineTextLocation.set(width*0.6f, height*0.49f);
        maxCompTextLocation.set(width*0.3f, height*0.39f);
        instrumentsTextLocation.set(width*0.3f, height*0.29f);
        thresholdsTextLocation.set(width*0.3f, height*0.19f);

        //Instruments Settings
        instrumentsSettingsScreenLocation.set(width*0.25f, height*0.30f);
        instrumentsSettingsScreenDimensions.set(width*0.5f, height*0.35f);
        instrumentsSettingsCloseButtonLocation.set(width*0.26f, height*0.64f - width*0.04f);
        for (int i=0; i < laneNumber; i++){
            instrumentsButtonLocations[i].set(width*0.3f - width*0.02f + (width*0.4f/((float) laneNumber))*(((float) i) + 1f/2f), height*0.45f);
            instrumentsTextLocations[i].set(width*0.3f - width*0.02f + (width*0.4f/((float) laneNumber))*(((float) i) + 1f/2f), height*0.35f);
        }
        instrumentSettingsButtonSize = width*0.04f;

        //Text Prompt Bar
        textPromptBarLocation.set(width*0.3f, height*0.45f);
        textPromptBarDimensions.set(width*0.4f, height*0.06f);
        promptTextLocation.set(width*0.45f, height*0.495f);
    }


    /**
     * Creates a new level editor with the given drawing context.
     *
     * This constructor initializes the models and controllers for the game.  The
     * view has already been initialized by the root class.
     */
    public EditorMode(GameCanvas canvas) throws IOException {
        this.canvas = canvas;
        inputController = new InputController();

        //editor parameters
        zoom = 2;
        speed = 2;
        currentPlaceType = PlaceType.QUARTER;
        precision = 2;
        meowLane = 0;
        selectedNoteType = EditorNote.NoteType.BEAT;
        selectedLossRate = 3;
        selectedNoteGain = 3;
        placing_start = false;
        placing_notes = true;
        placing_flags = false;
        placing_hits = false;
        playing = false;
        setting = false;
        hitSetting = false;
        flagSetting = false;
        instrumentSetting = false;
        thresholdSetting = false;
        typing = false;
        typingBPM = false;
        typingLaneNum = false;
        typingLineNum = false;
        typingName = false;
        typingSongName = false;
        typingMaxComp = false;
        typingFallSpeed = false;
        typingLossRate = false;
        typingNoteGain = false;
        typedString = "";
        trackSong = true;
        levelName = "New Level";
        songName = "N/A";
        BPM = 144;
        maxCompetency = 100;
        fallSpeed = 5;

        //initialize UI button locations
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
        upDurationButtonLocation = new Vector2();
        downDurationButtonLocation = new Vector2();
        flagButtonLocation = new Vector2();
        flagSettingsButtonLocation = new Vector2();
        flagSettingsScreenLocation = new Vector2();
        flagSettingsScreenDimensions = new Vector2();
        flagSettingsTitleLocation = new Vector2();
        closeFlagSettingsButtonLocation = new Vector2();
        rateButtonLocation = new Vector2();
        gainButtonLocation = new Vector2();
        rateTextLocation = new Vector2();
        gainTextLocation = new Vector2();
        hitButtonLocation = new Vector2();
        hitSettingsButtonLocation = new Vector2();
        hitSettingsScreenLocation = new Vector2();
        hitSettingsScreenDimensions = new Vector2();
        hitSettingsTitleLocation = new Vector2();
        closeHitSettingsButtonLocation = new Vector2();
        playButtonLocation = new Vector2();
        trackButtonLocation = new Vector2();
        resetButtonLocation = new Vector2();
        playButtonDimensions = new Vector2();
        trackButtonDimensions = new Vector2();
        undoButtonLocation = new Vector2();
        redoButtonLocation = new Vector2();
        settingsButtonLocation = new Vector2();
        settingsScreenLocation = new Vector2();
        settingsScreenDimensions = new Vector2();
        changeNameButtonLocation = new Vector2();
        changeSongNameButtonLocation = new Vector2();
        changeBPMButtonLocation = new Vector2();
        changeMaxCompButtonLocation = new Vector2();
        changeFallSpeedButtonLocation = new Vector2();
        changeInstrumentsButtonLocation = new Vector2();
        changeThresholdsButtonLocation = new Vector2();
        changeLaneButtonLocation = new Vector2();
        changeLineButtonLocation = new Vector2();
        closeSettingsButtonLocation = new Vector2();
        settingsTitleTextLocation = new Vector2();
        nameTextLocation = new Vector2();
        songNameTextLocation = new Vector2();
        BPMTextLocation = new Vector2();
        maxCompTextLocation = new Vector2();
        fallSpeedTextLocation = new Vector2();
        instrumentsTextLocation = new Vector2();
        thresholdsTextLocation = new Vector2();
        instrumentsSettingsScreenLocation = new Vector2();
        instrumentsSettingsScreenDimensions = new Vector2();
        instrumentsSettingsCloseButtonLocation = new Vector2();
        laneTextLocation = new Vector2();
        lineTextLocation = new Vector2();
        textPromptBarLocation = new Vector2();
        textPromptBarDimensions = new Vector2();
        promptTextLocation = new Vector2();

        //initialize actions list
        lastActions = new LinkedList<Action>();
        lastNotes = new LinkedList<EditorNote>();
        undoActions = new LinkedList<Action>();
        undoNotes = new LinkedList<EditorNote>();
    }

    private void initializeLevel(int laneNumber, int lineNumber) {
        sampleRate = music.getSampleRate();
        frameRate = 60;
        defineSongCharacteristics();
        startPosition = 0;
        playPosition = startPosition;
        this.laneNumber = laneNumber;
        this.lineNumber = lineNumber;
        selectedProbabilities = new int[laneNumber];
        typingProbabilities = new boolean[laneNumber];
        CThreshold = 2000;
        BThreshold = 3000;
        AThreshold = 4000;
        SThreshold = 5000;
        probabilityButtonLocations = new Vector2[laneNumber];
        probabilityTextLocations = new Vector2[laneNumber];
        instruments = new Instrument[laneNumber];
        instrumentsButtonLocations = new Vector2[laneNumber];
        instrumentsTextLocations = new Vector2[laneNumber];
        for (int i = 0; i < laneNumber; i++){
            selectedProbabilities[i] = 10;
            typingProbabilities[i] = false;
            probabilityButtonLocations[i] = new Vector2();
            probabilityTextLocations[i] = new Vector2();
            instruments[i] = Instrument.VIOLIN;
            instrumentsButtonLocations[i] = new Vector2();
            instrumentsTextLocations[i] = new Vector2();
        }

        //initialize band member lanes
        defineRectangles(laneNumber);
        defineButtonLocations();

        //initialize level data lists
        Notes = new LinkedList();
        Flags = new LinkedList();
        Hits = new LinkedList();
        EditorHit.setLaneNumber(laneNumber);
        beatNotes = new LinkedList[laneNumber][lineNumber];
        heldNotes = new LinkedList[laneNumber][lineNumber];
        switchNotes = new LinkedList[laneNumber];
        for (int lane = 0; lane < laneNumber; lane++) {
            switchNotes[lane] = new LinkedList<Integer>();
            for (int line = 0; line < lineNumber; line++) {
                beatNotes[lane][line] = new LinkedList<Integer>();
                heldNotes[lane][line] = new LinkedList<Integer[]>();
            }
        }
    }

    /**
     * Loads all the notes and other level editor settings in from what is specified in the level JSON.
     * @param level the JSON value corresponding to the JSON file containing all the level information
     */
    private void loadLevel(JsonValue level) {
        boolean loadNew = true;

        levelName = level.getString("levelName");
        songName = level.getString("song");
        sampleRate = music.getSampleRate();
        frameRate = 60;
        BPM = level.getInt("bpm");
        defineSongCharacteristics();
        selectedDuration = beat;
        songPosition = (int) ((4/zoom)*beat);
        startPosition = level.getInt("startPosition");
        playPosition = startPosition;
        laneNumber = level.get("bandMembers").size;
        lineNumber = level.getInt("linesPerMember");
        if (loadNew) {
            AThreshold = level.getInt("thresholdA");
            BThreshold = level.getInt("thresholdB");
            CThreshold = level.getInt("thresholdC");
            SThreshold = level.getInt("thresholdS");
            fallSpeed = level.getInt("fallSpeed");
        }
        selectedProbabilities = new int[laneNumber];
        typingProbabilities = new boolean[laneNumber];
        probabilityButtonLocations = new Vector2[laneNumber];
        probabilityTextLocations = new Vector2[laneNumber];
        instruments = new Instrument[laneNumber];
        instrumentsButtonLocations = new Vector2[laneNumber];
        instrumentsTextLocations = new Vector2[laneNumber];
        for (int i = 0; i < laneNumber; i++){
            selectedProbabilities[i] = 10;
            typingProbabilities[i] = false;
            probabilityButtonLocations[i] = new Vector2();
            probabilityTextLocations[i] = new Vector2();
            instrumentsButtonLocations[i] = new Vector2();
            instrumentsTextLocations[i] = new Vector2();
        }
        maxCompetency = level.getInt("maxCompetency");


        //initialize band member lanes
        defineRectangles(laneNumber);
        defineButtonLocations();

        //initialize level data lists
        Notes = new LinkedList();
        Flags = new LinkedList();
        Hits = new LinkedList();
        EditorHit.setLaneNumber(laneNumber);
        beatNotes = new LinkedList[laneNumber][lineNumber];
        heldNotes = new LinkedList[laneNumber][lineNumber];
        switchNotes = new LinkedList[laneNumber];
        for (int lane = 0; lane < laneNumber; lane++){
            switchNotes[lane] = new LinkedList<Integer>();
            for (int line = 0; line < lineNumber; line++){
                beatNotes[lane][line] = new LinkedList<Integer>();
                heldNotes[lane][line] = new LinkedList<Integer[]>();
            }
            if (level.get("bandMembers").get(lane).getString("instrument").equals("violin")){
                instruments[lane] = Instrument.VIOLIN;
            }
            if (level.get("bandMembers").get(lane).getString("instrument").equals("piano")){
                instruments[lane] = Instrument.PIANO;
            }
            if (level.get("bandMembers").get(lane).getString("instrument").equals("drum")){
                instruments[lane] = Instrument.DRUM;
            }
            if (level.get("bandMembers").get(lane).getString("instrument").equals("voice")){
                instruments[lane] = Instrument.VOICE;
            }
            JsonValue memberNotes = level.get("bandMembers").get(lane).get("notes");
            for (int i = 0; i < memberNotes.size; i++){
                EditorNote.NoteType type;
                int position = memberNotes.get(i).getInt("position") + startPosition;
                int line;
                int duration;
                if (memberNotes.get(i).getString("type").equals("beat")){
                    type = EditorNote.NoteType.BEAT;
                    line = memberNotes.get(i).getInt("line");
                    duration = 0;
                } else if (memberNotes.get(i).getString("type").equals("held")){
                    type = EditorNote.NoteType.HELD;
                    line = memberNotes.get(i).getInt("line");
                    duration = memberNotes.get(i).getInt("duration");
                } else {
                    type = EditorNote.NoteType.SWITCH;
                    line = -1;
                    duration = 0;
                }
                PlaceType tmpPlaceType = currentPlaceType;
                currentPlaceType = PlaceType.AUTO;
                addNote(type, lane, line, position, duration);
                currentPlaceType = tmpPlaceType;
            }
            if (loadNew) {
                JsonValue memberFlags = level.get("bandMembers").get(lane).get("compFlags");
                for (int i = 0; i < memberFlags.size; i++) {
                    int position = memberFlags.get(i).getInt("position") + startPosition;
                    int lossRate = memberFlags.get(i).getInt("rate");
                    int noteGain = memberFlags.get(i).getInt("gain");
                    PlaceType tmpPlaceType = currentPlaceType;
                    currentPlaceType = PlaceType.AUTO;
                    addFlag(lane, position, lossRate, noteGain);
                    currentPlaceType = tmpPlaceType;
                }
            }
        }
        if (loadNew) {
            JsonValue randomHits = level.get("randomHits");
            for (int i = 0; i < randomHits.size; i++) {
                int position = randomHits.get(i).getInt("position") + startPosition;
                JsonValue probabilities = randomHits.get(i).get("probabilities");
                int[] probability = new int[probabilities.size];
                for (int j = 0; j < probabilities.size; j++) {
                    probability[j] = probabilities.get(j).getInt("probability");
                }
                PlaceType tmpPlaceType = currentPlaceType;
                currentPlaceType = PlaceType.AUTO;
                addHit(position, probability);
                currentPlaceType = tmpPlaceType;
            }
        }
    }

    /**
     * Transforms all the notes and other settings of the current level state into a JSON format and saves
     * it as a file
     * @param levelName name of the level and the file created
     */
    private void saveLevel(String levelName){
        class Note {
            public String type;
            public int position;
            public int duration;
            public int line;
        }
        class Flag {
            public int rate;
            public int gain;
            public int position;
        }
        class Probability {
            public int probability;
        }
        class Hit {
            public ArrayList<Probability> probabilities;
            public int position;
        }
        class BandMember {
            public String instrument;
            public ArrayList<Note> notes;
            public ArrayList<Flag> compFlags;
        }
        class Level {
            public String levelName;
            public int levelNumber;
            public String levelImage;
            public String background;
            public String song;
            public int startPosition;
            public int bpm;
            public int fallSpeed;
            public int maxCompetency;
            public int linesPerMember;
            public int thresholdA;
            public int thresholdB;
            public int thresholdC;
            public int thresholdS;
            public ArrayList<BandMember> bandMembers;
            public ArrayList<Hit> randomHits;
        }

        // first sort all the notes, flags, and hits
        Collections.sort(Notes);
        Collections.sort(Flags);
        Collections.sort(Hits);

        Level l = new Level();
        l.levelName = this.levelName;
        l.levelNumber = 1;
        l.levelImage = "cafe";
        l.background = "cafe-background";
        l.song = songName;
        l.startPosition = this.startPosition;
        l.bpm = BPM;
        l.fallSpeed = fallSpeed;
        l.maxCompetency = this.maxCompetency;
        l.linesPerMember = lineNumber;
        l.thresholdA = AThreshold;
        l.thresholdB = BThreshold;
        l.thresholdC = CThreshold;
        l.thresholdS = SThreshold;
        l.bandMembers = new ArrayList<BandMember>();
        for (int lane = 0; lane < laneNumber; lane++){
            BandMember b = new BandMember();
            b.notes = new ArrayList<Note>();
            b.compFlags = new ArrayList<Flag>();
            if (instruments[lane] == Instrument.VIOLIN){
                b.instrument = "violin";
            }
            if (instruments[lane] == Instrument.PIANO){
                b.instrument = "piano";
            }
            if (instruments[lane] == Instrument.DRUM){
                b.instrument = "drum";
            }
            if (instruments[lane] == Instrument.VOICE){
                b.instrument = "voice";
            }
            l.bandMembers.add(b);
        }
        for (EditorNote note : Notes){
            Note n = new Note();
            n.line = note.getLine();
            n.position = note.getPos() - startPosition;
            if (note.getType() == EditorNote.NoteType.BEAT){
                n.type = "beat";
            }
            if (note.getType() == EditorNote.NoteType.HELD){
                n.type = "held";
            }
            if (note.getType() == EditorNote.NoteType.SWITCH){
                n.type = "switch";
            }
            n.duration = note.getDuration();
            l.bandMembers.get(note.getLane()).notes.add(n);
        }
        for (EditorFlag flag : Flags){
            Flag f = new Flag();
            f.rate = flag.getLossRate();
            f.gain = flag.getNoteGain();
            f.position = flag.getPos() - startPosition;
            l.bandMembers.get(flag.getLane()).compFlags.add(f);
        }
        l.randomHits = new ArrayList<Hit>();
        for (EditorHit hit : Hits){
            Hit h = new Hit();
            h.probabilities = new ArrayList<Probability>();
            for (int prob : hit.getProbabilities()) {
                Probability p = new Probability();
                p.probability = prob;
                h.probabilities.add(p);
            }
            h.position = hit.getPos() - startPosition;
            l.randomHits.add(h);
        }
        Json json = new Json();
        try {
            file = new FileWriter("test_easy_level.json");
            file.write(json.prettyPrint(l) );
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        soundController = new SoundController<String>();
        soundController.addSound("meow", "sound/meow.mp3");
        catNoteTexture = directory.getEntry("catnote", Texture.class);
        hitStarTexture = directory.getEntry("actual-star", Texture.class);
        catNoteAnimator = new FilmStrip(catNoteTexture, 1, 4,4);
        catNoteOrigin = new Vector2(catNoteAnimator.getRegionWidth()/2.0f, catNoteAnimator.getRegionHeight()/2.0f);
        hitAnimator = new FilmStrip(hitStarTexture, 1, 1,1);
        hitOrigin = new Vector2(hitAnimator.getRegionWidth()/2.0f, hitAnimator.getRegionHeight()/2.0f);
        flagAnimator = new FilmStrip(hitStarTexture, 1, 1,1);
        flagOrigin = new Vector2(flagAnimator.getRegionWidth()/2.0f, flagAnimator.getRegionHeight()/2.0f);
        displayFont = directory.getEntry("times", BitmapFont.class);
        inputController.setEditorProcessor();
        JsonReader jr = new JsonReader();
        defaultLevel = jr.parse(Gdx.files.internal("levels/tutorial-easy.json"));
        music = directory.getEntry("tutorial", MusicQueue.class);
        initializeLevel(4, 4);
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
                heldNotes[n.getLane()][n.getLine()].add(new Integer[] {n.getPos(),n.getDuration()});
            }
            if (n.getType() == EditorNote.NoteType.SWITCH) {
                switchNotes[n.getLane()].add(n.getPos());
            }
            return n;
        }
        return null;
    }

    /**
     * Adds a competency drain flag into the level. Updates level data lists.
     *
     * @param lane band member lane selected for the flag to be placed
     * @param songPos song position (in samples) selected for the flag to be placed
     * @param lossRate new rate of competency loss
     * @return flag which was added
     *
     */
    public EditorFlag addFlag(int lane, int songPos, int lossRate, int noteGain){
        int PlacePos = PlacePosition(songPos, currentPlaceType);
        boolean flagConflict = false;
        for (EditorFlag flag : Flags){
            if (flag.getLane() == lane) {
                if (currentPlaceType == PlaceType.FREE && flag.getPos() < PlacePos + (int) ((1 / (4 * zoom) * beat)) && flag.getPos() > PlacePos - (int) ((1 / (4 * zoom) * beat))) {
                    flagConflict = true;
                    System.out.println("Flag Conflict");
                }
                if (currentPlaceType != PlaceType.FREE && flag.getPos() == PlacePos){
                    flagConflict = true;
                    System.out.println("Flag Conflict");
                }
            }
        }
        if (!flagConflict) {
            System.out.println("No Flag Conflict");
            EditorFlag f = new EditorFlag(lane, PlacePos, lossRate, noteGain);
            Flags.add(f);
            return f;
        }
        return null;
    }

    /**
     * Adds a random hit into the level. Updates level data lists.
     *
     * @param songPos song position (in samples) selected for the hit to be placed
     * @param probabilities probability for each band member to be targeted notes
     * @return hit which was added
     *
     */
    public EditorHit addHit(int songPos, int[] probabilities){
        int PlacePos = PlacePosition(songPos, currentPlaceType);
        boolean hitConflict = false;
        for (EditorHit hit : Hits){
            if (currentPlaceType == PlaceType.FREE && hit.getPos() < PlacePos + (int) ((1 / (4 * zoom) * beat)) && hit.getPos() > PlacePos - (int) ((1 / (4 * zoom) * beat))) {
                hitConflict = true;
                System.out.println("Hit Conflict");
            }
            if (currentPlaceType != PlaceType.FREE && hit.getPos() == PlacePos){
                hitConflict = true;
                System.out.println("Hit Conflict");
            }

        }
        if (!hitConflict) {
            System.out.println("No Hit Conflict");
            EditorHit h = new EditorHit(PlacePos, probabilities);
            Hits.add(h);
            return h;
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

    /**
     * Deletes a competency drain flag from the level. Updates the level data lists.
     *
     * @param lane band member lane selected for flag to be removed
     * @param songPos song position (in samples) selected for flag to be removed
     * @return flag which was removed
     */
    public EditorFlag deleteFlag(int lane, int songPos){
        int minDistance = Integer.MAX_VALUE;
        EditorFlag minFlag = null;
        for (EditorFlag flag : Flags){
            if (flag.getLane() == lane) {
                if (flag.getPos() < songPos + (int) ((1 / (4 * zoom) * beat)) && flag.getPos() > songPos - (int) ((1 / (4 * zoom) * beat))) {
                    if (Math.abs(flag.getPos() - songPos) < minDistance){
                        minDistance = Math.abs(flag.getPos());
                        minFlag = flag;
                    }
                }
            }
        }
        if (minFlag != null){
            Flags.removeFirstOccurrence(minFlag);
        }
        return minFlag;
    }

    /**
     * Deletes a random hit from the level. Updates the level data lists.
     *
     * @param songPos song position (in samples) selected for hit to be removed
     * @return hit which was removed
     */
    public EditorHit deleteHit(int songPos){
        int minDistance = Integer.MAX_VALUE;
        EditorHit minHit = null;
        for (EditorHit hit : Hits){
            if (hit.getPos() < songPos + (int) ((1 / (4 * zoom) * beat)) && hit.getPos() > songPos - (int) ((1 / (4 * zoom) * beat))) {
                if (Math.abs(hit.getPos() - songPos) < minDistance){
                    minDistance = Math.abs(hit.getPos());
                    minHit = hit;
                }
            }
        }
        if (minHit != null){
            Hits.removeFirstOccurrence(minHit);
        }
        return minHit;
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
        inputController.readKeyboardLevelEditor();
        resolveAction();

        if (playing){
            playPosition = (int) (((float) sampleRate)*(music.getPosition())) + startPosition;
            if (trackSong) {
                if (!relocated || (playPosition>songPosition && playPosition - beat/8 < songPosition)) {
                    relocated = false;
                    songPosition = playPosition;
                }
            }
        }

        if (songPosition < (int) ((4/zoom)*beat) - 2*beat){
            songPosition = (int) ((4/zoom)*beat) - 2*beat;
        }

        typing = typingName || typingSongName || typingMaxComp || typingBPM || typingLaneNum || typingLineNum || typingFallSpeed || typingLossRate || typingNoteGain;
        for (int i =0; i<laneNumber;i++){
            if (typingProbabilities[i]){
                typing = true;
            }
        }

        //update note positions and play meows
        for (EditorNote note : Notes){
            note.setX(lineToScreenX(note.getLane(), note.getLine()));
            note.setY(songPosToScreenY(note.getPos()));
            note.setOnScreen(onScreen(note.getPos()));
            if (playing) {
                if (note.getType() != EditorNote.NoteType.SWITCH && note.getLane() == meowLane){
                    if (playPosition >= note.getPos() - beat / 4) {
                        if (note.shouldMeow()) {
                            soundController.playSound("meow", 0.75f);
                            note.setMeow(false);
                        }
                    } else {
                        note.setMeow(true);
                    }
                }
            }
        }
        //update flag positions
        for (EditorFlag flag : Flags){
            flag.setX(lineToScreenX(flag.getLane(), 0));
            flag.setY(songPosToScreenY(flag.getPos()));
            flag.setOnScreen(onScreen(flag.getPos()));
        }
        //update hit positions
        for (EditorHit hit : Hits){
            hit.setX(leftBound);
            hit.setY(songPosToScreenY(hit.getPos()));
            hit.setOnScreen(onScreen(hit.getPos()));
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
        return -3*beat;
    }

    /**
     * toggle between playing and pausing the music.
     * @param relocate true if the music should start playing from the player's song position when played.
     */
    public void togglePlay(boolean relocate){
        if (playing){
            playing = false;
            music.pause();
        } else {
            playing = true;
            music.play();
            if (relocate) {
                relocated = true;
                if (songPosition <= startPosition){
                    music.setPosition(0.01f);
                } else {
                    music.setPosition(((float) (songPosition - startPosition - beat/4))/ ((float) sampleRate));
                }
            }
        }
    }

    /** Resets the song and sets the song bar to the beginning position. If tracking the song, the user's
     * song position also moves to the beginning position.*/
    private void resetSong(){
        playing = false;
        playPosition = startPosition;
        music.reset();
        music.stop();
        if (trackSong){
            songPosition = 0;
        }
    }

    /**
     * Process user keyboard inputs into a typed string
     * @param digits True if the text prompt should only accept digits
     */
    private void typePrompt(boolean digits) {
        if (inputController.didType()){
            char ch = inputController.getCharTyped();
            if (((int) ch) == 8) {
                if (typedString.length() >= 1) {
                    typedString = typedString.substring(0, typedString.length() - 1);
                }
            } else if (!digits || Character.isDigit(ch)){
                typedString = typedString.concat(String.valueOf(ch));
            }
        }
    }

    /**
     * Resolves the typed string from the text prompt
     */
    private void resolveTypePrompt() {
        if (typingBPM) {
            typingBPM = false;
            if (typedString.equals("")) {
                BPM = 100;
            } else {
                BPM = Integer.parseInt(typedString);
            }
            typedString = "";
            defineSongCharacteristics();
        }
        if (typingFallSpeed) {
            typingFallSpeed = false;
            if (typedString.equals("")) {
                fallSpeed = 5;
            } else {
                fallSpeed = Integer.parseInt(typedString);
            }
            typedString = "";
        }
        if (typingMaxComp) {
            typingMaxComp = false;
            if (typedString.equals("")) {
                maxCompetency = 100;
            } else {
                maxCompetency = Integer.parseInt(typedString);
            }
            typedString = "";
        }
        if (typingLaneNum) {
            typingLaneNum = false;
            if (typedString.equals("")) {
                laneNumber = 4;
            } else {
                laneNumber = Integer.parseInt(typedString);
                if (laneNumber > 4) {
                    laneNumber = 4;
                    System.out.println("maximum lane number is 4");
                }
            }
            typedString = "";
            initializeLevel(laneNumber, lineNumber);
        }
        if (typingLineNum) {
            typingLineNum = false;
            if (typedString.equals("")) {
                lineNumber = 4;
            } else {
                lineNumber = Integer.parseInt(typedString);
                if (lineNumber > 4) {
                    lineNumber = 4;
                    System.out.println("maximum line number is 4");
                }
            }
            typedString = "";
            initializeLevel(laneNumber, lineNumber);
        }
        if (typingLossRate) {
            typingLossRate = false;
            if (typedString.equals("")){
                selectedLossRate = 3;
            } else {
                selectedLossRate = Integer.parseInt(typedString);
            }
            typedString = "";
        }
        if (typingNoteGain) {
            typingNoteGain = false;
            if (typedString.equals("")){
                selectedNoteGain = 3;
            } else {
                selectedNoteGain = Integer.parseInt(typedString);
            }
            typedString = "";
        }
        for(int i=0;i<laneNumber;i++){
            if (typingProbabilities[i]){
                typingProbabilities[i] = false;
                if (typedString.equals("")){
                    selectedProbabilities[i] = 10;
                } else {
                    selectedProbabilities[i] = Integer.parseInt(typedString);
                }
                typedString = "";
            }
        }
        if (typingName) {
            typingName = false;
            levelName = typedString;
            typedString = "";
        }
        if (typingSongName) {
            typingSongName = false;
            songName = typedString;
            typedString = "";
        }
    }


    /**
     * Checks if the given screen location is on a UI button and if so activates the button effect.
     * @param x the horizontal screen location
     * @param y the vertical screen location
     */
    private void buttonClick(float x, float y){
        if (!setting && !hitSetting && !flagSetting && !instrumentSetting && !thresholdSetting) {
            if (x >= quarterPrecision1ButtonLocation.x && x <= quarterPrecision1ButtonLocation.x + buttonSize) {
                if (y >= quarterPrecision1ButtonLocation.y && y <= quarterPrecision1ButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.QUARTER;
                    precision = 1;
                }
            }
            if (x >= quarterPrecision2ButtonLocation.x && x <= quarterPrecision2ButtonLocation.x + buttonSize) {
                if (y >= quarterPrecision2ButtonLocation.y && y <= quarterPrecision2ButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.QUARTER;
                    precision = 2;
                }
            }
            if (x >= quarterPrecision3ButtonLocation.x && x <= quarterPrecision3ButtonLocation.x + buttonSize) {
                if (y >= quarterPrecision3ButtonLocation.y && y <= quarterPrecision3ButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.QUARTER;
                    precision = 3;
                }
            }
            if (x >= thirdPrecision1ButtonLocation.x && x <= thirdPrecision1ButtonLocation.x + buttonSize) {
                if (y >= thirdPrecision1ButtonLocation.y && y <= thirdPrecision1ButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.THIRD;
                    precision = 1;
                }
            }
            if (x >= thirdPrecision2ButtonLocation.x && x <= thirdPrecision2ButtonLocation.x + buttonSize) {
                if (y >= thirdPrecision2ButtonLocation.y && y <= thirdPrecision2ButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.THIRD;
                    precision = 2;
                }
            }
            if (x >= thirdPrecision3ButtonLocation.x && x <= thirdPrecision3ButtonLocation.x + buttonSize) {
                if (y >= thirdPrecision3ButtonLocation.y && y <= thirdPrecision3ButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.THIRD;
                    precision = 3;
                }
            }
            if (x >= freeButtonLocation.x && x <= freeButtonLocation.x + buttonSize) {
                if (y >= freeButtonLocation.y && y <= freeButtonLocation.y + buttonSize) {
                    currentPlaceType = PlaceType.FREE;
                    precision = 1;
                }
            }
            if (x >= beatButtonLocation.x - 7.5f * noteButtonSize && x <= beatButtonLocation.x + 7.5f * noteButtonSize) {
                if (y >= beatButtonLocation.y - 7.5f * noteButtonSize && y <= beatButtonLocation.y + 7.5f * noteButtonSize) {
                    selectedNoteType = EditorNote.NoteType.BEAT;
                    setToPlaceNotes();
                }
            }
            if (x >= heldButtonLocation.x - 7.5f * noteButtonSize && x <= heldButtonLocation.x + 7.5f * noteButtonSize) {
                if (y >= heldButtonLocation.y - 7.5f * noteButtonSize && y <= heldButtonLocation.y + 7.5f * noteButtonSize) {
                    selectedNoteType = EditorNote.NoteType.HELD;
                    setToPlaceNotes();
                }
            }
            if (x >= switchButtonLocation.x - 7.5f * noteButtonSize && x <= switchButtonLocation.x + 7.5f * noteButtonSize) {
                if (y >= switchButtonLocation.y - 7.5f * noteButtonSize && y <= switchButtonLocation.y + 7.5f * noteButtonSize) {
                    selectedNoteType = EditorNote.NoteType.SWITCH;
                    setToPlaceNotes();
                }
            }

            if (x >= flagButtonLocation.x - 7.5f * flagButtonSize && x <= flagButtonLocation.x + 7.5f * flagButtonSize) {
                if (y >= flagButtonLocation.y - 7.5f * flagButtonSize && y <= flagButtonLocation.y + 7.5f * flagButtonSize) {
                    togglePlaceFlags();
                }
            }

            if (x >= hitButtonLocation.x - 7.5f * hitButtonSize && x <= hitButtonLocation.x + 7.5f * hitButtonSize) {
                if (y >= hitButtonLocation.y - 7.5f * hitButtonSize && y <= hitButtonLocation.y + 7.5f * hitButtonSize) {
                    togglePlaceHits();
                }
            }

            if (x >= flagSettingsButtonLocation.x && x <= flagSettingsButtonLocation.x + flagSettingsButtonSize) {
                if (y >= flagSettingsButtonLocation.y && y <= flagSettingsButtonLocation.y + flagSettingsButtonSize) {
                    flagSetting = true;
                }
            }
            if (x >= hitSettingsButtonLocation.x && x <= hitSettingsButtonLocation.x + hitSettingsButtonSize) {
                if (y >= hitSettingsButtonLocation.y && y <= hitSettingsButtonLocation.y + hitSettingsButtonSize) {
                    hitSetting = true;
                }
            }

            if (x >= upDurationButtonLocation.x && x <= upDurationButtonLocation.x + durationButtonSize) {
                if (y >= upDurationButtonLocation.y && y <= upDurationButtonLocation.y + durationButtonSize) {
                    incrementDuration(true);
                }
            }
            if (x >= downDurationButtonLocation.x && x <= downDurationButtonLocation.x + durationButtonSize) {
                if (y >= downDurationButtonLocation.y && y <= downDurationButtonLocation.y + durationButtonSize) {
                    incrementDuration(false);
                }
            }

            if (x >= playButtonLocation.x && x <= playButtonLocation.x + playButtonDimensions.x) {
                if (y >= playButtonLocation.y && y <= playButtonLocation.y + playButtonDimensions.y) {
                    togglePlay(false);
                }
            }
            if (x >= trackButtonLocation.x && x <= trackButtonLocation.x + trackButtonDimensions.x) {
                if (y >= trackButtonLocation.y && y <= trackButtonLocation.y + trackButtonDimensions.y) {
                    trackSong = !trackSong;
                    if (playing) {
                        songPosition = playPosition;
                    }
                }
            }
            if (x >= resetButtonLocation.x && x <= resetButtonLocation.x + resetButtonSize) {
                if (y >= resetButtonLocation.y && y <= resetButtonLocation.y + resetButtonSize) {
                    resetSong();
                }
            }
            if (x >= undoButtonLocation.x && x <= undoButtonLocation.x + undoButtonSize) {
                if (y >= undoButtonLocation.y && y <= undoButtonLocation.y + undoButtonSize) {
                    undo();
                }
            }
            if (x >= redoButtonLocation.x && x <= redoButtonLocation.x + undoButtonSize) {
                if (y >= redoButtonLocation.y && y <= redoButtonLocation.y + undoButtonSize) {
                    redo();
                }
            }
            if (x >= settingsButtonLocation.x && x <= settingsButtonLocation.x + settingsButtonSize) {
                if (y >= settingsButtonLocation.y && y <= settingsButtonLocation.y + settingsButtonSize) {
                    this.setting = true;
                }
            }
        } else if (setting && !instrumentSetting && !thresholdSetting){
            if (x >= closeSettingsButtonLocation.x && x <= closeSettingsButtonLocation.x + settingsButtonsSize) {
                if (y >= closeSettingsButtonLocation.y && y <= closeSettingsButtonLocation.y + settingsButtonsSize) {
                    setting = false;
                }
            }
            if (x >= changeNameButtonLocation.x && x <= changeNameButtonLocation.x + settingsButtonsSize) {
                if (y >= changeNameButtonLocation.y && y <= changeNameButtonLocation.y + settingsButtonsSize) {
                    typingName = true;
                }
            }
            if (x >= changeSongNameButtonLocation.x && x <= changeSongNameButtonLocation.x + settingsButtonsSize) {
                if (y >= changeSongNameButtonLocation.y && y <= changeSongNameButtonLocation.y + settingsButtonsSize) {
                    typingSongName = true;
                }
            }
            if (x >= changeBPMButtonLocation.x && x <= changeBPMButtonLocation.x + settingsButtonsSize) {
                if (y >= changeBPMButtonLocation.y && y <= changeBPMButtonLocation.y + settingsButtonsSize) {
                    typingBPM = true;
                }
            }
            if (x >= changeFallSpeedButtonLocation.x && x <= changeFallSpeedButtonLocation.x + settingsButtonsSize) {
                if (y >= changeFallSpeedButtonLocation.y && y <= changeFallSpeedButtonLocation.y + settingsButtonsSize) {
                    typingFallSpeed = true;
                }
            }
            if (x >= changeMaxCompButtonLocation.x && x <= changeMaxCompButtonLocation.x + settingsButtonsSize) {
                if (y >= changeMaxCompButtonLocation.y && y <= changeMaxCompButtonLocation.y + settingsButtonsSize) {
                    typingMaxComp = true;
                }
            }
            if (x >= changeInstrumentsButtonLocation.x && x <= changeInstrumentsButtonLocation.x + settingsButtonsSize) {
                if (y >= changeInstrumentsButtonLocation.y && y <= changeInstrumentsButtonLocation.y + settingsButtonsSize) {
                    instrumentSetting = true;
                }
            }
            if (x >= changeThresholdsButtonLocation.x && x <= changeThresholdsButtonLocation.x + settingsButtonsSize) {
                if (y >= changeThresholdsButtonLocation.y && y <= changeThresholdsButtonLocation.y + settingsButtonsSize) {
                    thresholdSetting = true;
                }
            }
            if (x >= changeLaneButtonLocation.x && x <= changeLaneButtonLocation.x + settingsButtonsSize) {
                if (y >= changeLaneButtonLocation.y && y <= changeLaneButtonLocation.y + settingsButtonsSize) {
                    typingLaneNum = true;
                }
            }
            if (x >= changeLineButtonLocation.x && x <= changeLineButtonLocation.x + settingsButtonsSize) {
                if (y >= changeLineButtonLocation.y && y <= changeLineButtonLocation.y + settingsButtonsSize) {
                    typingLineNum = true;
                }
            }
        } else if (hitSetting){
            if (x >= closeHitSettingsButtonLocation.x && x <= closeHitSettingsButtonLocation.x + closeHitSettingsButtonSize) {
                if (y >= closeHitSettingsButtonLocation.y && y <= closeHitSettingsButtonLocation.y + closeHitSettingsButtonSize) {
                    hitSetting = false;
                }
            }
            for (int i = 0; i < laneNumber; i++){
                if (x >= probabilityButtonLocations[i].x && x <= probabilityButtonLocations[i].x + probabilityButtonSize) {
                    if (y >= probabilityButtonLocations[i].y && y <= probabilityButtonLocations[i].y + probabilityButtonSize) {
                        typingProbabilities[i] = true;
                    }
                }
            }
        } else if (flagSetting){
            if (x >= closeFlagSettingsButtonLocation.x && x <= closeFlagSettingsButtonLocation.x + closeFlagSettingsButtonSize) {
                if (y >= closeFlagSettingsButtonLocation.y && y <= closeFlagSettingsButtonLocation.y + closeFlagSettingsButtonSize) {
                    flagSetting = false;
                }
            }
            if (x >= rateButtonLocation.x && x <= rateButtonLocation.x + flagRateButtonSize) {
                if (y >= rateButtonLocation.y && y <= rateButtonLocation.y + flagRateButtonSize) {
                    typingLossRate = true;
                }
            }
            if (x >= gainButtonLocation.x && x <= gainButtonLocation.x + flagRateButtonSize) {
                if (y >= gainButtonLocation.y && y <= gainButtonLocation.y + flagRateButtonSize) {
                    typingNoteGain = true;
                }
            }
        } else if (instrumentSetting){
            if (x >= instrumentsSettingsCloseButtonLocation.x && x <= instrumentsSettingsCloseButtonLocation.x + instrumentSettingsButtonSize) {
                if (y >= instrumentsSettingsCloseButtonLocation.y && y <= instrumentsSettingsCloseButtonLocation.y + instrumentSettingsButtonSize) {
                    instrumentSetting = false;
                }
            }
            for (int i = 0; i < laneNumber; i++){
                if (x >= instrumentsButtonLocations[i].x && x <= instrumentsButtonLocations[i].x + instrumentSettingsButtonSize) {
                    if (y >= instrumentsButtonLocations[i].y && y <= instrumentsButtonLocations[i].y + instrumentSettingsButtonSize) {
                         if(instruments[i] == Instrument.VIOLIN){
                             instruments[i] = Instrument.PIANO;
                         }
                         else if(instruments[i] == Instrument.PIANO){
                            instruments[i] = Instrument.DRUM;
                         }
                         else if(instruments[i] == Instrument.DRUM){
                            instruments[i] = Instrument.VOICE;
                         }
                         else if(instruments[i] == Instrument.VOICE){
                            instruments[i] = Instrument.VIOLIN;
                         }
                    }
                }
            }
        } else if (thresholdSetting){

        }
    }

    /**
     * Changes the value of the selected held note duration by an increment based on the current
     * place type and place precision.
     * @param increase True if increasing the duration, false if decreasing the duration
     */
    private void incrementDuration(boolean increase){
        int add = 0;
        if (currentPlaceType == PlaceType.QUARTER){
            add = beat / (2 * (int) Math.pow((double) 2, (double) precision-1));
        }
        if (currentPlaceType == PlaceType.THIRD){
            add = beat / (3 * (int) Math.pow((double) 2, (double) precision-1));
        }
        if (PlacePosition(selectedDuration, currentPlaceType) <= selectedDuration && increase){
            selectedDuration += add;
        }
        if ((PlacePosition(selectedDuration, currentPlaceType) >= selectedDuration && !increase)){
            selectedDuration -= add;
        }
        selectedDuration = PlacePosition(selectedDuration, currentPlaceType);
    }

    private void setToPlaceNotes(){
        placing_notes = true;
        placing_flags = false;
        placing_hits = false;
        placing_start = false;
    }

    private void togglePlaceHits(){
        placing_hits = !placing_hits;
        if (placing_hits) {
            placing_flags = false;
            placing_start = false;
            placing_notes = false;
        }
        else {
            placing_notes = true;
        }
    }

    private void togglePlaceFlags(){
        placing_flags = !placing_flags;
        if (placing_flags) {
            placing_start = false;
            placing_hits = false;
            placing_notes = false;
        }
        else {
            placing_notes = true;
        }
    }

    private void togglePlaceStart(){
        placing_start = !placing_start;
        if (placing_start) {
            placing_flags = false;
            placing_hits = false;
            placing_notes = false;
        }
        else {
            placing_notes = true;
        }
    }


    private void resolveAction(){
        if (!(typing || setting || hitSetting || flagSetting)) {
            //Get Movement Input
            moves = inputController.getMoves();
            if (moves[0]) {
                songPosition -= (int) ((speed / 20f) * beat * (1 / zoom));
            }
            if (moves[1]) {
                songPosition += (int) ((speed / 20f) * beat * (1 / zoom));
            }
            if (moves[2]) {
                zoom *= 1.01f;
            }
            if (moves[3]) {
                zoom *= 0.99f;
            }

            //Get Place Note Input
            if (inputController.didClick()) {
                float mouseX = inputController.getMouseX();
                float mouseY = canvas.getHeight() - inputController.getMouseY();
                int clickedLane = screenXtoline(mouseX)[0];
                int clickedLine = screenXtoline(mouseX)[1];
                int clickedSongPosition = screenYtoSongPos(mouseY);
                int duration = 0;
                if (placing_notes && clickedLane != -1 && clickedSongPosition != -3 * beat && clickedSongPosition >= 0) {
                    if (selectedNoteType == EditorNote.NoteType.SWITCH) {
                        clickedLine = -1;
                    }
                    if (selectedNoteType == EditorNote.NoteType.HELD) {
                        duration = selectedDuration;
                    }
                    EditorNote note = addNote(selectedNoteType, clickedLane, clickedLine, clickedSongPosition, duration);
                    if (note != null) {
                        lastActions.add(Action.PLACE);
                        lastNotes.add(note);
                        undoActions.clear();
                        undoNotes.clear();
                    }
                }
                if (placing_start && clickedLane != -1 && clickedSongPosition != -3 * beat && clickedSongPosition <= 0) {
                    startPosition = PlacePosition(clickedSongPosition, currentPlaceType);
                }
                if (placing_flags && clickedLane != -1 && clickedSongPosition != -3 * beat && clickedSongPosition > 0) {
                    addFlag(clickedLane, clickedSongPosition, selectedLossRate, selectedNoteGain);
                }
                if (placing_hits  && clickedLane != -1 && clickedSongPosition != -3 * beat && clickedSongPosition > 0) {
                    addHit(clickedSongPosition, selectedProbabilities);
                }
                buttonClick(mouseX, mouseY);
            }

            //Get Erase Note Input
            if (inputController.didErase()) {
                float mouseX = inputController.getMouseX();
                float mouseY = canvas.getHeight() - inputController.getMouseY();
                int clickedLane = screenXtoline(mouseX)[0];
                int clickedLine = screenXtoline(mouseX)[1];
                int clickedSongPosition = screenYtoSongPos(mouseY);
                if (placing_notes && clickedLane != -1 && clickedSongPosition != -1) {
                    if (selectedNoteType == EditorNote.NoteType.SWITCH) {
                        clickedLine = -1;
                    }
                    EditorNote note = deleteNote(clickedLane, clickedLine, clickedSongPosition);
                    if (note != null) {
                        lastActions.add(Action.ERASE);
                        lastNotes.add(note);
                        undoActions.clear();
                        undoNotes.clear();
                    }
                }
                if (placing_flags && clickedLane != -1 && clickedSongPosition != -3 * beat) {
                    deleteFlag(clickedLane, clickedSongPosition);
                }
                if (placing_hits  && clickedLane != -1 && clickedSongPosition != -3 * beat) {
                    deleteHit(clickedSongPosition);
                }
            }

            //Get Placement Type Input
            if (inputController.didSetFree()) {
                currentPlaceType = PlaceType.FREE;
            }
            if (inputController.didSetThird()) {
                currentPlaceType = PlaceType.THIRD;
            }
            if (inputController.didSetQuarter()) {
                currentPlaceType = PlaceType.QUARTER;
            }

            //Get Placement Note Type Input
            if (inputController.setSwitchNotes()) {
                selectedNoteType = EditorNote.NoteType.SWITCH;
                setToPlaceNotes();
            }
            if (inputController.setBeatNotes()) {
                selectedNoteType = EditorNote.NoteType.BEAT;
                setToPlaceNotes();
            }
            if (inputController.setHeldNotes()) {
                selectedNoteType = EditorNote.NoteType.HELD;
                setToPlaceNotes();
            }

            if (inputController.changedMeow()) {
                if (meowLane < laneNumber-1){
                    meowLane++;
                } else {
                    meowLane = 0;
                }
            }

            if (inputController.durationUp()) {
                incrementDuration(true);
            }
            if (inputController.durationDown()) {
                incrementDuration(false);
            }

            //Get Placement Precision Input
            if (inputController.didPrecision1()) {
                precision = 1;
            }
            if (inputController.didPrecision2()) {
                precision = 2;
            }
            if (inputController.didPrecision3()) {
                precision = 3;
            }

            //Get Undo/Redo Input
            if (placing_notes && inputController.didUndo()) {
                undo();
            }
            if (placing_notes && inputController.didRedo()) {
                redo();
            }

            //Get Play input
            if (inputController.didPressPlay(false)) {
                togglePlay(false);
            }
            if (inputController.didPressPlay(true)) {
                togglePlay(true);
            }
            if (inputController.didPressTrack()) {
                trackSong = !trackSong;
                if (playing) {
                    songPosition = playPosition;
                }
            }
            if (inputController.didResetSong()) {
                resetSong();
            }

            //Get Speed Input
            if (inputController.didSpeedUp()) {
                speed = 6;
            } else {
                speed = 2;
            }

            //Get Place Start Input
            if (inputController.pressedPlaceStart()) {
               togglePlaceStart();
            }

            //Get Place Flags Input
            if (inputController.pressedPlaceFlags()) {
                togglePlaceFlags();
            }

            //Get Place Hits Input
            if (inputController.pressedPlaceHits()) {
                togglePlaceHits();
            }

            //Get Save Input
            if (inputController.didSave()) {
                saveLevel("level_test");
            }

            if (inputController.didLoad()) {
                loadLevel(defaultLevel);
            }
        } else if (typing){
            if (typingBPM || typingLaneNum || typingLineNum || typingMaxComp || typingLossRate || typingNoteGain || typingFallSpeed) {
                typePrompt(true);
            } else if (typingName || typingSongName) {
                typePrompt(false);
            }
            for (int i=0;i<laneNumber;i++){
                if (typingProbabilities[i]){
                    typePrompt(true);
                }
            }
            if (inputController.finishedTyping()){
                resolveTypePrompt();
            }
        } else {
            if (inputController.didClick()) {
                float mouseX = inputController.getMouseX();
                float mouseY = canvas.getHeight() - inputController.getMouseY();
                buttonClick(mouseX, mouseY);
            }
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

    private String instrumentToString(Instrument inst) {
        if (inst == Instrument.VIOLIN){
            return "violin";
        }
        if (inst == Instrument.PIANO){
            return "piano";
        }
        if (inst == Instrument.DRUM){
            return "drum";
        }
        if (inst == Instrument.VOICE){
            return "voice";
        }
        return "null";
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

            //draw negative pos shades
            if (onScreen(0)){
                canvas.drawRect(x, songPosToScreenY(0), x + laneWidth, y+laneHeight, Color.DARK_GRAY, true);
            }
            if (!onScreen(0) && songPosition < 0){
                canvas.drawRect(x, y, x + laneWidth, y+laneHeight, Color.DARK_GRAY, true);
            }

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

            //draw start bar
            if (onScreen(startPosition)){
                l = songPosToScreenY(startPosition);
                canvas.drawLine(x, l, x + laneWidth, l, 8, Color.GOLDENROD);
            }

            //draw center bar
            l = songPosToScreenY(songPosition);
            canvas.drawLine(x, l, x + laneWidth, l, 4, Color.PURPLE);


            x += laneWidth + laneSpacing;

        }

        //draw flags
        if (placing_flags) {
            for (EditorFlag flag : Flags) {
                if (flag.OnScreen()) {
                    flag.draw(canvas, zoom, getLaneEdge(flag.getLane()), getLaneWidth(), displayFont);
                }
            }
        }

        //draw hits
        if (placing_hits) {
            for (EditorHit hit : Hits) {
                if (hit.OnScreen()) {
                    hit.draw(canvas, zoom, laneEdges, getLaneWidth(), displayFont);
                }
            }
        }

        //draw notes
        if (placing_notes) {
            for (EditorNote note : Notes) {
                if (note.getType() == EditorNote.NoteType.HELD) {
                    if (note.getPos() + note.getDuration() > songPosition - (int) ((4 / zoom) * beat)) {
                        if (note.getPos() < songPosition + (int) ((4 / zoom) * beat)) {
                            float trailTop;
                            float trailBot;
                            if (onScreen(note.getPos())) {
                                trailTop = songPosToScreenY(note.getPos());
                            } else {
                                trailTop = bottomBound + laneHeight;
                            }
                            if (onScreen(note.getPos() + note.getDuration())) {
                                trailBot = songPosToScreenY(note.getPos() + note.getDuration());
                            } else {
                                trailBot = bottomBound;
                            }
                            canvas.drawLine(note.getX(), trailBot, note.getX(), trailTop, 8, Color.YELLOW);
                        }
                    }
                }
                if (note.OnScreen()) {
                    note.draw(canvas, zoom, getLaneEdge(note.getLane()), getLaneWidth());
                }
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
        catNoteAnimator.setFrame(0);
        selectBoxX = beatButtonLocation.x;
        if (selectedNoteType == EditorNote.NoteType.BEAT ){
            selectBoxY = beatButtonLocation.y;
        }
        if (selectedNoteType == EditorNote.NoteType.HELD){
            selectBoxY = heldButtonLocation.y;
        }
        if (selectedNoteType == EditorNote.NoteType.SWITCH){
            selectBoxY = switchButtonLocation.y;
        }
        if (placing_notes) {
            canvas.draw(catNoteAnimator, Color.CYAN, catNoteOrigin.x, catNoteOrigin.y, selectBoxX, selectBoxY, 0.0f, noteButtonSize * 1.4f, noteButtonSize * 1.4f);
        }

        canvas.draw(catNoteAnimator, Color.WHITE, catNoteOrigin.x, catNoteOrigin.y, beatButtonLocation.x, beatButtonLocation.y, 0.0f, noteButtonSize, noteButtonSize);
        canvas.draw(catNoteAnimator, Color.SALMON, catNoteOrigin.x, catNoteOrigin.y, heldButtonLocation.x, heldButtonLocation.y, 0.0f, noteButtonSize, noteButtonSize);
        canvas.draw(catNoteAnimator, Color.GREEN, catNoteOrigin.x, catNoteOrigin.y, switchButtonLocation.x, switchButtonLocation.y, 0.0f, noteButtonSize, noteButtonSize);

        //draw flag and hit type buttons
        hitAnimator.setFrame(0);
        flagAnimator.setFrame(0);
        selectBoxX = flagButtonLocation.x;
        if (placing_flags) {
            selectBoxY = flagButtonLocation.y;
            canvas.draw(flagAnimator, Color.LIME, flagOrigin.x, flagOrigin.y, selectBoxX, selectBoxY, 0.0f, flagButtonSize * 1.4f, flagButtonSize * 1.4f);
        }
        if (placing_hits) {
            selectBoxY = hitButtonLocation.y;
            canvas.draw(hitAnimator, Color.LIME, hitOrigin.x, hitOrigin.y, selectBoxX, selectBoxY, 0.0f, hitButtonSize * 1.4f, hitButtonSize * 1.4f);
        }
        canvas.draw(flagAnimator, Color.RED, flagOrigin.x, flagOrigin.y, flagButtonLocation.x, flagButtonLocation.y, 0.0f, flagButtonSize, flagButtonSize);
        canvas.draw(hitAnimator, Color.WHITE, hitOrigin.x, hitOrigin.y, hitButtonLocation.x, hitButtonLocation.y, 0.0f, hitButtonSize, hitButtonSize);

        canvas.drawRect(flagSettingsButtonLocation, flagSettingsButtonSize, flagSettingsButtonSize, Color.RED, true);
        canvas.drawRect(flagSettingsButtonLocation, flagSettingsButtonSize, flagSettingsButtonSize, Color.BLACK, false);
        canvas.drawRect(hitSettingsButtonLocation, hitSettingsButtonSize, hitSettingsButtonSize, Color.YELLOW, true);
        canvas.drawRect(hitSettingsButtonLocation, hitSettingsButtonSize, hitSettingsButtonSize, Color.BLACK, false);


        //draw duration up/down buttons
        canvas.drawRect(upDurationButtonLocation, durationButtonSize, durationButtonSize, Color.SALMON, true);
        canvas.drawRect(upDurationButtonLocation, durationButtonSize, durationButtonSize, Color.BLACK, false);
        canvas.drawRect(downDurationButtonLocation, durationButtonSize, durationButtonSize, Color.SALMON, true);
        canvas.drawRect(downDurationButtonLocation, durationButtonSize, durationButtonSize, Color.BLACK, false);

        //draw play/track buttons
        canvas.drawRect(playButtonLocation, playButtonDimensions.x, playButtonDimensions.y, Color.GREEN, true);
        canvas.drawRect(playButtonLocation, playButtonDimensions.x, playButtonDimensions.y, Color.BLACK, false);

        canvas.drawRect(trackButtonLocation, trackButtonDimensions.x, trackButtonDimensions.y, Color.FOREST, true);
        canvas.drawRect(trackButtonLocation, trackButtonDimensions.x, trackButtonDimensions.y, Color.BLACK, false);

        canvas.drawRect(resetButtonLocation, resetButtonSize, resetButtonSize, Color.DARK_GRAY, true);
        canvas.drawRect(resetButtonLocation, resetButtonSize, resetButtonSize, Color.BLACK, false);

        if (playing) {
            selectBoxX = playButtonLocation.x;
            selectBoxY = playButtonLocation.y;
            canvas.drawRect(selectBoxX + playButtonDimensions.x / 4f, selectBoxY + playButtonDimensions.y / 4f, selectBoxX + playButtonDimensions.x * (1f - 1f / 4f), selectBoxY + playButtonDimensions.y * (1f - 1f / 4f), Color.RED, true);
        }
        if (trackSong) {
            selectBoxX = trackButtonLocation.x;
            selectBoxY = trackButtonLocation.y;
            canvas.drawRect(selectBoxX + trackButtonDimensions.x / 4f, selectBoxY + trackButtonDimensions.y / 4f, selectBoxX + trackButtonDimensions.x * (1f - 1f / 4f), selectBoxY + trackButtonDimensions.y * (1f - 1f / 4f), Color.NAVY, true);
        }

        //draw undo/redo buttons
        canvas.drawRect(undoButtonLocation, undoButtonSize, undoButtonSize, Color.GRAY, true);
        canvas.drawRect(undoButtonLocation, undoButtonSize, undoButtonSize, Color.BLACK, false);
        canvas.drawRect(redoButtonLocation, undoButtonSize, undoButtonSize, Color.GRAY, true);
        canvas.drawRect(redoButtonLocation, undoButtonSize, undoButtonSize, Color.BLACK, false);

        //draw open settings button
        canvas.drawRect(settingsButtonLocation, settingsButtonSize, settingsButtonSize, Color.CORAL, true);
        canvas.drawRect(settingsButtonLocation, settingsButtonSize, settingsButtonSize, Color.BLACK, false);

        if(setting){
            //draw settings screen
            canvas.drawRect(settingsScreenLocation, settingsScreenDimensions.x, settingsScreenDimensions.y, Color.CORAL, true);
            canvas.drawRect(settingsScreenLocation, settingsScreenDimensions.x, settingsScreenDimensions.y, Color.BLACK, false);

            //draw settings UI buttons
            canvas.drawRect(changeNameButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeNameButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeSongNameButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeSongNameButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeBPMButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeBPMButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeFallSpeedButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeFallSpeedButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeMaxCompButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeMaxCompButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeInstrumentsButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeInstrumentsButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeThresholdsButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeThresholdsButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeLaneButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeLaneButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(changeLineButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(changeLineButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);
            canvas.drawRect(closeSettingsButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.RED, true);
            canvas.drawRect(closeSettingsButtonLocation, settingsButtonsSize, settingsButtonsSize, Color.BLACK, false);

            //draw settings text
            canvas.drawText("SETTINGS", displayFont, settingsTitleTextLocation.x, settingsTitleTextLocation.y);
            String nameMsg = "Name: " + levelName;
            String songNameMsg = "Song: " + songName;
            String BPMMsg = "BPM: " + String.valueOf(BPM);
            String fallSpeedMsg = "Fall Speed: " + String.valueOf(fallSpeed);
            String maxCompMsg = "Max Competency: " + String.valueOf(maxCompetency);
            String laneMsg = "Lane Number: " + String.valueOf(laneNumber);
            String lineMsg = "Line Number: " + String.valueOf(lineNumber);
            String ThresholdsMsg = "Grades: S-" + SThreshold + ", A-" + AThreshold + ", B-" + BThreshold + ", C-" + CThreshold;
            String InstrumentsMsg = "Instruments: ";
            for (int i = 0; i < laneNumber; i++){
                InstrumentsMsg += instrumentToString(instruments[i]) + ", ";
            }
            canvas.drawText(nameMsg, displayFont, nameTextLocation.x, nameTextLocation.y);
            canvas.drawText(songNameMsg, displayFont, songNameTextLocation.x, songNameTextLocation.y);
            canvas.drawText(BPMMsg, displayFont, BPMTextLocation.x, BPMTextLocation.y);
            canvas.drawText(fallSpeedMsg, displayFont, fallSpeedTextLocation.x, fallSpeedTextLocation.y);
            canvas.drawText(maxCompMsg, displayFont, maxCompTextLocation.x, maxCompTextLocation.y);
            canvas.drawText(InstrumentsMsg, displayFont, instrumentsTextLocation.x, instrumentsTextLocation.y);
            canvas.drawText(ThresholdsMsg, displayFont, thresholdsTextLocation.x, thresholdsTextLocation.y);
            canvas.drawText(laneMsg, displayFont, laneTextLocation.x, laneTextLocation.y);
            canvas.drawText(lineMsg, displayFont, lineTextLocation.x, lineTextLocation.y);
        }

        if (hitSetting) {
            canvas.drawRect(hitSettingsScreenLocation, hitSettingsScreenDimensions.x, hitSettingsScreenDimensions.y, Color.YELLOW, true);
            canvas.drawRect(hitSettingsScreenLocation, hitSettingsScreenDimensions.x, hitSettingsScreenDimensions.y, Color.BLACK, false);
            canvas.drawText("Select Random Hit Probabilities", displayFont, hitSettingsTitleLocation.x, hitSettingsTitleLocation.y);

            canvas.drawRect(closeHitSettingsButtonLocation, closeHitSettingsButtonSize, closeHitSettingsButtonSize, Color.RED, true);
            canvas.drawRect(closeHitSettingsButtonLocation, closeHitSettingsButtonSize, closeHitSettingsButtonSize, Color.BLACK, false);

            for (int i=0; i < laneNumber; i++){
                canvas.drawRect(probabilityButtonLocations[i], probabilityButtonSize, probabilityButtonSize, Color.LIGHT_GRAY, true);
                canvas.drawRect(probabilityButtonLocations[i], probabilityButtonSize, probabilityButtonSize, Color.BLACK, false);
                canvas.drawText(String.valueOf(selectedProbabilities[i]) + "%", displayFont, probabilityTextLocations[i].x, probabilityTextLocations[i].y);
            }
        }

        if (flagSetting) {
            canvas.drawRect(flagSettingsScreenLocation, flagSettingsScreenDimensions.x, flagSettingsScreenDimensions.y, Color.FIREBRICK, true);
            canvas.drawRect(flagSettingsScreenLocation, flagSettingsScreenDimensions.x, flagSettingsScreenDimensions.y, Color.BLACK, false);
            canvas.drawText("Select Competency Loss Rate and Note Gain", displayFont, flagSettingsTitleLocation.x, flagSettingsTitleLocation.y);

            canvas.drawRect(closeFlagSettingsButtonLocation, closeFlagSettingsButtonSize, closeFlagSettingsButtonSize, Color.RED, true);
            canvas.drawRect(closeFlagSettingsButtonLocation, closeFlagSettingsButtonSize, closeFlagSettingsButtonSize, Color.BLACK, false);

            canvas.drawRect(rateButtonLocation, flagRateButtonSize, flagRateButtonSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(rateButtonLocation, flagRateButtonSize, flagRateButtonSize, Color.BLACK, false);
            canvas.drawRect(gainButtonLocation, flagRateButtonSize, flagRateButtonSize, Color.LIGHT_GRAY, true);
            canvas.drawRect(gainButtonLocation, flagRateButtonSize, flagRateButtonSize, Color.BLACK, false);

            canvas.drawText("Loss Rate: " + String.valueOf(selectedLossRate), displayFont, rateTextLocation.x, rateTextLocation.y);
            canvas.drawText("Note Gain: " + String.valueOf(selectedNoteGain), displayFont, gainTextLocation.x, gainTextLocation.y);
        }

        if (instrumentSetting) {
            canvas.drawRect(instrumentsSettingsScreenLocation, instrumentsSettingsScreenDimensions.x, instrumentsSettingsScreenDimensions.y, Color.YELLOW, true);
            canvas.drawRect(instrumentsSettingsScreenLocation, instrumentsSettingsScreenDimensions.x, instrumentsSettingsScreenDimensions.y, Color.BLACK, false);

            canvas.drawRect(instrumentsSettingsCloseButtonLocation, instrumentSettingsButtonSize, instrumentSettingsButtonSize, Color.RED, true);
            canvas.drawRect(instrumentsSettingsCloseButtonLocation, instrumentSettingsButtonSize, instrumentSettingsButtonSize, Color.BLACK, false);

            for (int i=0; i < laneNumber; i++){
                canvas.drawRect(instrumentsButtonLocations[i], instrumentSettingsButtonSize, instrumentSettingsButtonSize, Color.LIGHT_GRAY, true);
                canvas.drawRect(instrumentsButtonLocations[i], instrumentSettingsButtonSize, instrumentSettingsButtonSize, Color.BLACK, false);
                canvas.drawText(instrumentToString(instruments[i]), displayFont, instrumentsTextLocations[i].x,instrumentsTextLocations[i].y);
            }
        }

        if (typing) {
            canvas.drawRect(textPromptBarLocation, textPromptBarDimensions.x, textPromptBarDimensions.y, Color.NAVY, true);
            canvas.drawRect(textPromptBarLocation, textPromptBarDimensions.x, textPromptBarDimensions.y, Color.BLACK, false);
            canvas.drawText(typedString, displayFont, promptTextLocation.x, promptTextLocation.y);
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
                listener.exitScreen(this, ExitCode.TO_MENU);
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
