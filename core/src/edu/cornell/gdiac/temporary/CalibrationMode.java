package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
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
    private Note[] noteList;

    // important music calculation things
    /** Current frame we're on */
    private int frame;

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

    /** Beats per minute (BPM) of the calibration beat */
    private final int BPM = 90;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
        this.frame = 0;
        // TODO: change it so that this is not explicit
        this.frameRate = 60;
        this.noteList = new Note[10];
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
        music = directory.getEntry("calibration", MusicQueue.class);
        music.setLooping(true);
        // define parts of the music
        this.sampleRate = music.getSampleRate();
        this.beat = (int) (((float) sampleRate)/(((float) BPM)/60f));
        this.samplesPerFrame = sampleRate/frameRate;
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
        canvas.end();
    }

    /** Updates the note states */
    private void update(float delta) {
        // Process the input into screen
        inputController.readInput();

        // update each of the notes
        for (Note note : noteList) {
            // TODO: update notes
        }
        musicPosition += samplesPerFrame;

        if (musicPosition % beat == 0) {
            System.out.println("beat " + musicPosition);
        }
        // resolve inputs from the user
        resolveInputs();
    }

    /** Resolves inputs from the input controller */
    private void resolveInputs() {
        // use space to take inputs
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
