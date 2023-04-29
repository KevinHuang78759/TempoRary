package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.util.ScreenListener;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

public class CalibrationMode implements Screen {

    /** Whether this player mode is still active */
    private boolean active;

    // ASSETS
    /** The font for giving messages to the player */
    private BitmapFont displayFont;
    /** The song */
    private MusicQueue music;
    /** The background texture */
    private Texture background;
    /** White background */
    private Texture whiteBackground;
    /** The back arrow */
    private Texture backArrow;
    /** The calibration display when not hit */
    private Texture calibrationNote;
    /** The calibration display when input hit */
    private Texture calibrationNoteHit;


    private Color textColor = new Color(27f / 255, 1f / 255, 103f / 255, 1);


    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    // new system (based on milliseconds)
    /** Represents the amount of leeway for hitting on the beat (in milliseconds) */
    public final int BASE_OFFSET = 70;
    /** List of beats that user has hit */
    private List<Integer> userHitBeats;
    /** offset after calibration  */
    private int offset;
    /** whether we finished calibration */
    private boolean isCalibrated;
    /** Beats per minute (BPM) of the calibration beat */
    private final int BPM = 100;
    /** Distance between beats in milliseconds */
    private final int DIST_BETWEEN_BEAT = 60000 / BPM;
    /** temp variable to draw whether the hit was on beat or not */
    private boolean onBeat;

    private final int NUM_BEATS_TO_HIT = 12;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
        userHitBeats = new LinkedList<>();
        offset = 0;
        reset();
    }

    private void reset() {
        userHitBeats.clear();
        isCalibrated = false;
    }

    /** Returns the offset */
    public int getOffset() {
        return offset;
    }

    /**
     * Returns true if the player is done calibrating and wants to go back to the main menu.
     * @return true if the player is ready to exit calibration mode
     */
    public boolean isReady() {
        return inputController.didExit();
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
        displayFont = directory.getEntry("main", BitmapFont.class);
        music = directory.getEntry("calibration", MusicQueue.class);
        background  = directory.getEntry("calibration-background", Texture.class); //calibration background?
        whiteBackground = directory.getEntry("white-background", Texture.class);
        backArrow = directory.getEntry("calibration-back-arrow", Texture.class);
        calibrationNote = directory.getEntry("calibration-note", Texture.class);
        calibrationNoteHit = directory.getEntry("calibration-note-hit", Texture.class);;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update();
            draw();
            if (isReady() && listener != null) {
                listener.exitScreen(this, ExitCode.TO_MENU);
            }
        }
    }

    /** Draws elements to the screen */
    private void draw() {
        canvas.begin();
        canvas.drawBackground(whiteBackground,0,0);
        canvas.drawBackground(background,0,0);

        float noteScale = 0.25f;
        // draw hit indicator
        if (inputController.didHoldPlay()) {
            canvas.draw(calibrationNoteHit, Color.WHITE, calibrationNoteHit.getWidth() / 2, calibrationNoteHit.getHeight() / 2, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, noteScale, noteScale);
        } else {
            canvas.draw(calibrationNote, Color.WHITE, calibrationNote.getWidth() / 2, calibrationNote.getHeight() / 2, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, noteScale, noteScale);
        }

        if (isCalibrated) {
            canvas.drawTextCentered("" + onBeat, displayFont, 150);
            canvas.drawText("You have been calibrated!\nYou can exit this screen\nwith the esc key", displayFont, 100, canvas.getWidth() / 2);
        }

        canvas.draw(backArrow, Color.WHITE, 0, backArrow.getHeight(), 25, canvas.getHeight() - 40, 0, 0.1f, 0.1f);

        canvas.drawTextCentered("Press the space bar to the beat", displayFont,200, textColor);
        canvas.drawTextCentered("Make sure not to click out of the window while calibrating", displayFont,-300, textColor);

        canvas.end();
    }

    /** Updates the note states */
    private void update() {
        // Process the input into screen
        inputController.readInput();
        // resolve inputs from the user
        resolveInputs();

        // check music and calibration states
        if (!music.isPlaying() && !isCalibrated) {
            setCalibration();
        }
        else if (!music.isPlaying() && isCalibrated) {
            music.setLooping(true);
            music.play();
        }
    }

    /**
     * computes values of userHitBeats to set the offset after calibration
     */
    private void setCalibration() {
        // average
        // desync between video and audio (there will always be maybe a little bit of a desync) - can use data to move it
        //     make beat simple, figure out where the "note" is
        // desync between user input and when it's processed
        int sum = 0;
        // skip first two because of potential initial noisy data
        for (int i = 2; i < userHitBeats.size(); i++) {
            sum += userHitBeats.get(i);
        }
        this.offset = userHitBeats.size() > 0 ? sum / userHitBeats.size() : 0;
        isCalibrated = true;
        //System.out.println("offset: " + offset);
    }

    /** Resolves inputs from the input controller */
    private void resolveInputs() {
        // use space to take inputs
        boolean hitSpace = inputController.didPressPlay();

        // essentially, resolve the current position at which you hit the space bar
        // assign the beat it's at, and then determine how far off you are
        if (hitSpace) {
            // TODO: rename all of these variables
            int currPosInMs = Math.round(music.getPosition() * 1000);
            // your beat that you hit the space bar at
            int hitBeat = Math.round((float) (currPosInMs) / DIST_BETWEEN_BEAT);
            // the beat we are actually at
            int actualBeat = hitBeat * DIST_BETWEEN_BEAT;
            int diff = currPosInMs - actualBeat;

            if (isCalibrated) {
                onBeat = isOnBeat(actualBeat, currPosInMs);
            }
            else {
                userHitBeats.add(diff);
                //System.out.println("hit at pos: " + currPosInMs + " attempted beat hit: " + actualBeat + " diff: " + diff);
            }
        }
    }

    /**
     * checks whether use is on beat or not
     * currentHitPosition should be the song position at which you hit the "beat"
     */
    public boolean isOnBeat(int actualBeatPosition, int currentHitPosition) {
        int adjustedPosition = currentHitPosition - this.offset;
        int lowerRange = actualBeatPosition - BASE_OFFSET;
        int higherRange = actualBeatPosition + BASE_OFFSET;
        //System.out.println(lowerRange + ", " + higherRange + "; " + adjustedPosition +  " " + actualBeatPosition + " " + (adjustedPosition >= lowerRange && adjustedPosition <= higherRange));
        return adjustedPosition >= lowerRange && adjustedPosition <= higherRange;
    }

    @Override
    public void show() {
        active = true;
        music.play();
        reset();
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
