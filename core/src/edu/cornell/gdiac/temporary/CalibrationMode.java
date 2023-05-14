package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.LinkedList;
import java.util.List;

public class CalibrationMode implements Screen {

    /** Whether this player mode is still active */
    private boolean active;

    // ASSETS
    /** The font for giving instructions to the player */
    private BitmapFont instructionsFont;
    /** The smaller font for giving instructions to the player */
    private BitmapFont smallerFont;
    private GlyphLayout layout;
    /** The song */
    private MusicQueue music;
    /** Song source */
    private AudioSource songSource;
    /** The background texture */
    private Texture background;
    /** The back arrow */
    private Texture backArrow;
    /** The header for this screen */
    private Texture calibrationHeader;
    /** Lines surrounding the header */
    private Texture headerLine;
    /** The calibration display when not hit */
    private Texture calibrationNote;
    /** The calibration display when input hit */
    private Texture calibrationNoteHit;
    /** The indicator to show how many you need to hit */
    private Texture circleIndicator;
    /** The indicator to show how many you have hit */
    private Texture circleIndicatorHit;

    /** Color of the text for interface */
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

    /** Specified number of beats to hit */
    private final int NUM_BEATS_TO_HIT = 12;
    /** Specified number of hit beats to remove from count in calibration calculation */
    private final int NUM_BEATS_REMOVED = 2;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = InputController.getInstance();
        this.canvas = canvas;
        userHitBeats = new LinkedList<>();
        offset = 0;
        isCalibrated = false;
        layout = new GlyphLayout();
    }

    /** Resets the calibration mode by clearing beats, calibration is false, and resetting music */
    private void reset() {
        userHitBeats.clear();
        isCalibrated = false;
        music.stop();
        music.reset();
        music.clearSources();
        music = ((AudioEngine) Gdx.audio).newMusicBuffer( songSource.getChannels() == 1, songSource.getSampleRate() );
        music.addSource(songSource);
        music.setLooping(true);
    }

    /** Returns the calibration offset */
    public int getOffset() {
        return offset;
    }

    /**
     * Returns true if the player is done calibrating and wants to go back to the main menu.
     * @return true if the player is ready to exit calibration mode
     */
    public boolean isReady() {
        // Process the input into screen
        boolean backButtonPressed = false;

        if (inputController.didMouseLift()) {
            int screenX = (int) inputController.getMouseX();
            int screenY = (int) inputController.getMouseY();
            screenY = canvas.getHeight() - screenY;

            float xRadius = backArrow.getWidth()/2.0f;
            float xCoord = 50 + xRadius;
            boolean xInBounds = xCoord - xRadius <= screenX && xCoord + xRadius >= screenX;
            float yRadius = backArrow.getHeight()/2.0f;
            float yCoord = canvas.getHeight() - 50 - yRadius;
            boolean yInBounds = yCoord - yRadius <= screenY && yCoord + yRadius >= screenY;
            backButtonPressed = xInBounds && yInBounds;
        }

        return inputController.didExit() || backButtonPressed || isCalibrated;
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
        JsonReader jr = new JsonReader();
        JsonValue assets = jr.parse(Gdx.files.internal("assets.json"));

        // fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        parameter.color = textColor;
        instructionsFont = generator.generateFont(parameter);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-SemiBold.ttf"));
        parameter.size = 30;
        smallerFont = generator.generateFont(parameter);

        music = ((AudioEngine) Gdx.audio).newMusic(Gdx.files.internal(assets.get("samples").getString("calibration")));
        songSource = music.getSource(0);
        background = directory.getEntry("calibration-background", Texture.class);
        calibrationHeader = directory.getEntry("calibration-header", Texture.class);
        headerLine = directory.getEntry("header-line", Texture.class);
        backArrow = directory.getEntry("back-arrow", Texture.class);
        calibrationNote = directory.getEntry("calibration-note", Texture.class);
        calibrationNoteHit = directory.getEntry("calibration-note-hit", Texture.class);
        circleIndicator = directory.getEntry("calibration-circle", Texture.class);
        circleIndicatorHit = directory.getEntry("calibration-circle-filled", Texture.class);

        music.setLooping(true);
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
        canvas.drawBackground(background,0,0);

        canvas.draw(calibrationHeader, Color.WHITE, calibrationHeader.getWidth()/2, calibrationHeader.getHeight()/2, canvas.getWidth()/2, canvas.getHeight() - 80, 0, 1, 1);

        float noteScale = 0.3f;
        // draw hit indicator
        if (inputController.didCalibrationPress()) {
            canvas.draw(calibrationNoteHit, Color.WHITE, calibrationNoteHit.getWidth() / 2, calibrationNoteHit.getHeight() / 2, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, noteScale, noteScale);
        } else {
            canvas.draw(calibrationNote, Color.WHITE, calibrationNote.getWidth() / 2, calibrationNote.getHeight() / 2, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, noteScale, noteScale);
        }

        String instruction = "PRESS THE SPACEBAR TO THE BEAT";
        canvas.drawTextCentered(instruction, instructionsFont,200);
        layout.setText(instructionsFont, instruction);
        canvas.draw(headerLine, Color.WHITE, headerLine.getWidth(), 0, canvas.getWidth()/2 - layout.width/2 - 20, 200 + canvas.getHeight()/2 - layout.height + 15, 0, 0.75f, 1);
        canvas.draw(headerLine, Color.WHITE, 0, 0, canvas.getWidth()/2 + layout.width/2 + 20, 200 + canvas.getHeight()/2 - layout.height + 15, 0, 0.75f, 1);
        canvas.drawTextCentered("Make sure not to click out of the window while calibrating!", smallerFont,-280);
//        canvas.drawTextCentered("while calibrating!", smallerFont,-320);

        int totalHits = NUM_BEATS_TO_HIT + NUM_BEATS_REMOVED;
        int spaceApart = 10;
        float circleIndicatorScale = 0.75f;
        float circleIndicatorTrueWidth = 0.75f * circleIndicator.getWidth();
        float startingX = canvas.getWidth()/2f - (spaceApart * (totalHits / 2f - 1) + circleIndicatorTrueWidth * (totalHits / 2f));
        float circleDrawY = canvas.getHeight()/2 - noteScale * calibrationNote.getHeight()/2 - 80;

        // draw the beat needed:
        int i = 0;
        while (i < userHitBeats.size()) {
            canvas.draw(circleIndicatorHit, Color.WHITE, circleIndicator.getWidth()/2, circleIndicator.getHeight()/2,
                    startingX + i * (circleIndicatorTrueWidth + spaceApart), circleDrawY, 0,
                    circleIndicatorScale, circleIndicatorScale);
            i++;
        }
        while (i < totalHits) {
            canvas.draw(circleIndicator, Color.WHITE, circleIndicator.getWidth()/2, circleIndicator.getHeight()/2,
                    startingX + i * (circleIndicatorTrueWidth + spaceApart), circleDrawY, 0,
                    circleIndicatorScale, circleIndicatorScale);
            i++;
        }

        canvas.draw(backArrow, Color.WHITE, 0, backArrow.getHeight(), 50, canvas.getHeight() - 50, 0, 1f, 1f);

        canvas.end();
    }

    /** Updates the note states */
    private void update() {
        // Process the input into screen
        inputController.readInput();

        // resolve inputs from the user
        resolveInputs();

        // check music and calibration states
        if (userHitBeats.size() - NUM_BEATS_REMOVED >= NUM_BEATS_TO_HIT && !isCalibrated) {
            setCalibration();
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
        for (int i = NUM_BEATS_REMOVED; i < userHitBeats.size(); i++) {
            sum += userHitBeats.get(i);
        }
        this.offset = userHitBeats.size() > 0 ? sum / userHitBeats.size() : 0;
        isCalibrated = true;
        //System.out.println("offset: " + offset);
    }

    /** Resolves inputs from the input controller */
    private void resolveInputs() {
        // use space to take inputs
        boolean hitSpace = inputController.didCalibrationHit();

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
    }

    @Override
    public void hide() {
        active = false;
        reset();
    }

    @Override
    public void resize(int width, int height) {
        // TODO: Auto-generated method stub
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
        // TODO: dispose all assets
        if (music != null){
            music.dispose();
        }

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
