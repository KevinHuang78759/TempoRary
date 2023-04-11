package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.LinkedList;
import java.util.List;

public class CalibrationMode implements Screen {

    /** Whether this player mode is still active */
    private boolean active;

    // ASSETS
    /** The font for giving messages to the player */
    private BitmapFont displayFont;
    /** The note texture */
    private Texture catNote;
    /** The song */
    private MusicQueue music;
    /** The background texture */
    private Texture background;

    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    // TODO: CONVERT TO SAMPLES
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

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
        userHitBeats = new LinkedList<>();
        offset = 0;
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
        displayFont = directory.getEntry("times", BitmapFont.class);
        music = directory.getEntry("calibration", MusicQueue.class);
        background  = directory.getEntry("background",Texture.class); //calibration background?
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /** Draws elements to the screen */
    private void draw(float delta) {
        canvas.begin();
        canvas.drawBackground(background,0,0);

        // draw a hit bar
        // Change line color if it is triggered
        Color lineColor = inputController.didHoldPlay() ? Color.TEAL : Color.NAVY;
        canvas.drawLine(canvas.getWidth()/2-canvas.getWidth()/12, canvas.getHeight()/2, canvas.getWidth()/2-canvas.getWidth()/12+(canvas.getWidth()/6), canvas.getHeight()/2, 200, lineColor);

        if (isCalibrated) {
            canvas.drawTextCentered("" + onBeat, displayFont, 150);
            canvas.drawText("You have been calibrated!\nYou can exit this screen\nwith the esc key", displayFont, 100, canvas.getWidth() / 2);
        }

        canvas.drawTextCentered("Calibration", displayFont,200);
        canvas.end();
    }

    /** Updates the note states */
    private void update(float delta) {
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
        System.out.println("offset: " + offset);
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
                System.out.println("hit at pos: " + currPosInMs + " attempted beat hit: " + actualBeat + " diff: " + diff);
            }
        }
    }

    /**
     * checks whether use is on beat or not
     * TODO: move this someplace else???
     * currentHitPosition should be the song position at which you hit the "beat"
     */
    public boolean isOnBeat(int actualBeatPosition, int currentHitPosition) {
        int adjustedPosition = currentHitPosition - this.offset;
        int lowerRange = actualBeatPosition - BASE_OFFSET;
        int higherRange = actualBeatPosition + BASE_OFFSET;
        System.out.println(lowerRange + ", " + higherRange + "; " + adjustedPosition +  " " + actualBeatPosition + " " + (adjustedPosition >= lowerRange && adjustedPosition <= higherRange));
        return adjustedPosition >= lowerRange && adjustedPosition <= higherRange;
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
        music.stop();
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
