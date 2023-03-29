package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.CalibNote;
import edu.cornell.gdiac.temporary.entity.Note;
import edu.cornell.gdiac.util.ScreenListener;

public class CalibrationMode implements Screen {

    /** Whether this player mode is still active */
    private boolean active;

    // ASSETS
    /** The font for giving messages to the player */
    private BitmapFont displayFont;
    /** The song */
    private MusicQueue music;

    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** List of notes that are being used in calculation */
    private CalibNote[] noteList;

//    /** Represents the base amount of leeway for hitting on the beat (in samples) */
    public final int BASE_OFFSET = 6000;

    // important music calculation variables
    /** number of song samples per second*/
    private int sampleRate;

    /** number of frames per second */
    private int frameRate;

    /** Spacing (in samples) between beat lines in the song*/
    private int beat;

    /** Amount of samples per frame */
    private int samplesPerFrame;

    /** Position of the "music" */
    private int musicPosition;

    private boolean onBeat;

    private Texture catNoteTexture;


    /** Beats per minute (BPM) of the calibration beat */
    private final int BPM = 89;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
        // TODO: change it so that this is not explicit?
        // frame rate can be calculated by 1 / delta
        frameRate = 60;
        noteList = new CalibNote[10];
        for (int i = 0; i < noteList.length; i++) {
            noteList[i] = new CalibNote();
        }
        musicPosition = 0;
    }

    // TODO: finish method
    public int getOffset() {
        return 0;
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
        displayFont = directory.getEntry("times", BitmapFont.class);
        catNoteTexture = directory.getEntry("catnote", Texture.class);
        music = directory.getEntry("calibration", MusicQueue.class);
        music.setLooping(true);
        // define parts of the music
        this.sampleRate = music.getSampleRate();
        // sample / second / (beat / second * second / frame) -->
        // sample / second * (frame / beat) --> sample * frame / beat * second
        // distance between two beats
        this.beat = Math.round(((float) sampleRate) / (((float) BPM) / frameRate));
        this.samplesPerFrame = sampleRate / frameRate;

        // time between beats is a lot of
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
            if (inputController.didExit() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /** Draws elements to the screen */
    private void draw(float delta) {
        canvas.begin();
        displayFont.setColor(Color.NAVY);
        canvas.drawTextCentered("Calibration", displayFont,50);
        canvas.drawTextCentered("" + Math.round(1 / delta), displayFont, 100);
        canvas.drawTextCentered("" + onBeat, displayFont, 150);
        canvas.end();
    }

    /** Updates the note states */
    private void update(float delta) {
        // Process the input into screen
        inputController.readInput();

        System.out.println();

        // update each of the notes
        for (CalibNote note : noteList) {
            // TODO: update notes
        }

//        musicPosition += samplesPerFrame;
        musicPosition += sampleRate * delta;

        // resolve inputs from the user
        resolveInputs(delta);
    }

    /** Resolves inputs from the input controller */
    private void resolveInputs(float delta) {
        // use space to take inputs
        boolean hitSpace = inputController.didPressPlay();
        // essentially, resolve the current position at which you hit the space bar
        // assign the beat it's at, and then determine how far off you are
        if (hitSpace) {
            int tempBeat = Math.round(((float) sampleRate) / (((float) BPM) / (1 / delta)));
            int currentBeat = Math.round((float) musicPosition / tempBeat);
            int attemptedBeat = currentBeat * tempBeat;
            onBeat = isOnBeat(attemptedBeat, musicPosition);
//            System.out.println(isOnBeat(attemptedBeat, musicPosition));
//            System.out.println("hit at beat: " + musicPosition + " attempted beat hit: " + attemptedBeat + " diff: " + (musicPosition - attemptedBeat));
        }
    }

    /** checks whether use is on beat or not */
    private boolean isOnBeat(int hitPosition, int currPosition) {
        int lowerRange = currPosition - BASE_OFFSET;
        int higherRange = currPosition + BASE_OFFSET;
        return hitPosition >= lowerRange && hitPosition <= higherRange;
    }

    @Override
    public void show() {
        active = true;
        music.play();
    }

    @Override
    public void hide() {
        active = false;
        music.pause();
    }

    @Override
    public void resize(int width, int height) {
        // Auto-generated method stub
    }

    @Override
    public void pause() {
        // Auto-generated method stub
    }

    @Override
    public void resume() {
        // Auto-generated method stub
    }

    @Override
    public void dispose() {
        inputController = null;
        canvas = null;
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
