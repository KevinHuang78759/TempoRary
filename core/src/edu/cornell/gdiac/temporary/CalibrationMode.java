package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.CalibNote;
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

    /** List of notes that are being used in calculation */
    private CalibNote[] noteList;

    // new system (based on milliseconds)
    /** Represents the base amount of leeway for hitting on the beat (in milliseconds) */
    public final int BASE_OFFSET = 40;
    /** Represents the start of the song, i.e. where the beats begin (in milliseconds) */
//    public final int START_OFFSET = 30;
//    /** List of the beats and their positions (based on ms) */
//    private int[] beats;
    /** List of beats that user has hit */
    private List<Integer> userHitBeats;

    /** offset when hitting early */
    private int beforeOffset;

    /** offset for hitting late */
    private int afterOffset;

    /** whether we finished calibration */
    private boolean isCalibrated;

    // new system (based on samples)
    /** List of the beats and their positions (based on samples */
//    private int[] beatsSamples;

    /** Beats per minute (BPM) of the calibration beat */
    private final int BPM = 100;

    /** Distance between beats in milliseconds */
    private final int DIST_BETWEEN_BEAT = 60000 / BPM;

    /** Position of hit line in the y position */
    private final int LINE_Y = 350;

    /** Distance between each of the notes */
    private final int DIST_BETWEEN_NOTE = 275;

    /** temp variable to draw whether the hit was on beat or not */
    private boolean onBeat;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
        noteList = new CalibNote[8];
        for (int i = 0; i < noteList.length; i++) {
            CalibNote note = new CalibNote();
            noteList[i] = note;
            note.setX(canvas.getWidth()/2);
            note.setY(LINE_Y + i * DIST_BETWEEN_NOTE);
            note.setVX(0);
            note.setVY(-calculateVY());

            // notes for initialization
            // target is the line drawn for the hit (it's at y = 100 right now)
            // the middle of the cat note itself should be at 100 every 666.666 milliseconds
            // the above will determine where the initial y of the cat note is and the velocity of the note
            // y should be the start position of the note
        }
        userHitBeats = new LinkedList<>();
        beforeOffset = afterOffset = 0;
        isCalibrated = false;
    }

    /**
     * Calculates the velocity of a note based on the constants NOTE_DIST and DIST_BETWEEN_BEAT
     *
     * @return "velocity" of note as a float
     */
    private float calculateVY() {
        // velocity = distance / time
        // since you are about NOTE_DIST apart, you need to get to the line in NOTE_DIST / DIST_BETWEEN_BEAT
        // velocity is
        // with the delta, it means that you have about 0.016667 seconds between each frame
        // but here, since you are drawing every frame, the velocity is technically units/frame
        // time = DIST_BETWEEN_BEAT

        // (float) NOTE_DIST / DIST_BETWEEN_BEAT (units / ms) * 1000 ms / 1 second (units / second) * 1 second / 60 frames
        return ((float) DIST_BETWEEN_NOTE / DIST_BETWEEN_BEAT) * (1000) * (1f / 60) ;
    }

    /** gets offset for early notes */
    public int getBeforeOffset() {
        return beforeOffset;
    }

    /** gets offset for late notes */
    public int getAfterOffset() {
        return afterOffset;
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
        catNote = directory.getEntry("catnote", Texture.class);
        music = directory.getEntry("calibration", MusicQueue.class);
        background  = directory.getEntry("background",Texture.class); //calibration background?
//        music.getSource(0).getStream().getSampleOffset();
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
        canvas.drawBackground(background,0,0);

        // drawing a lane
        Vector2 bottomLeft = new Vector2(canvas.getWidth()/2-canvas.getWidth()/12, 40);
        canvas.drawRect(bottomLeft, canvas.getWidth()/6, canvas.getHeight()-80, Color.LIME, false);

        // draw a hit bar
        // Change line color if it is triggered
        Color lineColor = inputController.didPressPlay() ? Color.PINK : Color.NAVY;
        canvas.drawLine(canvas.getWidth()/2-canvas.getWidth()/12, LINE_Y, canvas.getWidth()/2-canvas.getWidth()/12+(canvas.getWidth()/6), LINE_Y, 3, lineColor);

        // draw each note
        for (CalibNote c : noteList) {
            c.setTexture(catNote);
            c.draw(canvas);
        }

        if (isCalibrated) {
            canvas.drawTextCentered("" + onBeat, displayFont, 150);
        }

        canvas.drawTextCentered("Calibration", displayFont,canvas.getHeight() / 2 - 20);
        canvas.end();
    }

    /** Updates the note states */
    private void update(float delta) {
        // Process the input into screen
        inputController.readInput();

        float maxY = 0;
        for (CalibNote note : noteList) {
            maxY = Math.max(note.getY(), maxY);
        }

        // update each of the notes
        for (CalibNote note : noteList) {
            // check if out of bounds, bring back to the top
            if (note.getY() + note.getRadius() < 0) {
                note.setY(maxY + DIST_BETWEEN_NOTE);
            }
            note.update(delta);
        }

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

    private void setCalibration() {
        if (userHitBeats.isEmpty()) {
            beforeOffset = afterOffset = BASE_OFFSET;
        }
        else {
            int before = 0, beforeCount = 0, after = 0, afterCount = 0;
            for (Integer diff : userHitBeats) {
                if (diff < 0) {
                    before += diff;
                    beforeCount++;
                }
                else if (diff > 0) {
                    after += diff;
                    afterCount++;
                }
                else {
                    beforeCount++;
                    afterCount++;
                }
            }
            this.beforeOffset = beforeCount > 0 ? Math.abs(before) / beforeCount + BASE_OFFSET : BASE_OFFSET;
            this.afterOffset = afterCount > 0 ? after / afterCount + BASE_OFFSET : BASE_OFFSET;
            isCalibrated = true;
            System.out.println("before " + beforeOffset);
            System.out.println("after " + afterOffset);
        }
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
            }

            System.out.println("hit at pos: " + currPosInMs + " attempted beat hit: " + actualBeat + " diff: " + (currPosInMs - actualBeat));
        }
    }

    /**
     * checks whether use is on beat or not
     * TODO: move this someplace else
     */
    private boolean isOnBeat(int hitPosition, int currPosition) {
        int lowerRange = hitPosition - this.beforeOffset;
        int higherRange = hitPosition + this.afterOffset;
        System.out.println(lowerRange + " " + higherRange + "  " + currPosition +  " " + hitPosition + " " + (currPosition >= lowerRange && currPosition <= higherRange));
        return currPosition >= lowerRange && currPosition <= higherRange;
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
