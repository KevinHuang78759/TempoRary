package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
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

    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;

    private Array<GameObject> objects;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** List of notes that are being used in calculation */
    private CalibNote[] noteList;

    private Texture background;

    // important music calculation variables
    // old code based on Sami's method
    //    /** Represents the base amount of leeway for hitting on the beat (in samples) */
//    public final int BASE_OFFSET = 6000;
    /** number of song samples per second*/
//    private int sampleRate;
//    /** number of frames per second */
//    private int frameRate;
//    /** Spacing (in samples) between beat lines in the song*/
//    private int beat;
//    /** Amount of samples per frame */
//    private int samplesPerFrame;
//    /** Position of the "music" */
//    private int musicPosition;

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
    private int[] beatsSamples;

    /** Beats per minute (BPM) of the calibration beat */
    private final int BPM = 90;

    private boolean onBeat;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationMode(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
        // frame rate can be calculated by 1 / delta
//        frameRate = 60;
        noteList = new CalibNote[10];
        for (int i = 0; i < noteList.length; i++) {
            noteList[i] = new CalibNote();
        }
        userHitBeats = new LinkedList<>();
        beforeOffset = afterOffset = 0;
        isCalibrated = false;

        objects = new Array<GameObject>();
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
        catNote = directory.getEntry("catnote", Texture.class);
        music = directory.getEntry("calibration", MusicQueue.class);
//        music.setLooping(true);
//        music.getSource(0).getStream().getSampleOffset();
        // define parts of the music
//        this.sampleRate = music.getSampleRate();
        // sample / second / (beat / second * second / frame) -->
        // sample / second * (frame / beat) --> sample * frame / beat * second
        // distance between two beats
//        this.beat = Math.round(((float) sampleRate) / (((float) BPM) / frameRate));
//        this.samplesPerFrame = sampleRate / frameRate;

        background  = directory.getEntry("background",Texture.class); //calibration background?

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
        canvas.drawBackground(background,0,0);
        canvas.drawTextCentered("Calibration", displayFont,50);
        canvas.drawTextCentered("" + Math.round(1 / delta), displayFont, 100);
        canvas.drawTextCentered("" + onBeat, displayFont, 150);

        //drawing a lane
        Vector2 bottomLeft = new Vector2(canvas.getWidth()/2-canvas.getWidth()/12, 40);
        canvas.drawRect(bottomLeft, canvas.getWidth()/6, canvas.getHeight()-80, Color.LIME, false);
        //draw a hit bar
        canvas.drawLine(canvas.getWidth()/2-canvas.getWidth()/12, 100, canvas.getWidth()/2-canvas.getWidth()/12+(canvas.getWidth()/6), 100, 3, Color.BLACK);
        // add notes


        for (int i=0; i<100;i++){
            Note s = new Note(0, Note.NType.BEAT);
            s.setX(canvas.getWidth()/2);
            s.setTexture(catNoteTexture);
            s.setY(100+200*i);
            s.setVX(0);
            objects.add(s);
        }

        for (GameObject o : objects) {
            o.draw(canvas);
        }

        if (isCalibrated) {
            canvas.drawTextCentered("" + onBeat, displayFont, 150);
        }
        canvas.end();
    }

    /** Updates the note states */
    private void update(float delta) {
        // Process the input into screen
        inputController.readInput();

        // update each of the notes
        for (CalibNote note : noteList) {
            // TODO: update notes
        }

//        musicPosition += samplesPerFrame;
//        musicPosition += sampleRate * delta;

        // resolve inputs from the user
        resolveInputs();

        if (!music.isPlaying() && !isCalibrated) {
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
                this.beforeOffset = Math.abs(before) / beforeCount + BASE_OFFSET;
                this.afterOffset = after / afterCount + BASE_OFFSET;
                isCalibrated = true;
                System.out.println("before " + beforeOffset);
                System.out.println("after " + afterOffset);
            }
        }

        if (!music.isPlaying() && isCalibrated) {
            music.setLooping(true);
            music.play();
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
            int tempBeat = (60000 / BPM);
            // your beat that you hit the space bar at
            int hitBeat = Math.round((float) (currPosInMs) / tempBeat);
            // the beat we are actually at
            int actualBeat = hitBeat * tempBeat;
//            int tempBeat = Math.round(((float) sampleRate) / (((float) BPM) / (1 / delta)));
//            int currentBeat = Math.round((float) musicPosition / tempBeat);
//            int attemptedBeat = currentBeat * tempBeat;
            if (isCalibrated) {
                onBeat = isOnBeat(actualBeat, currPosInMs);
            }
//            onBeat = isOnBeat(attemptedBeat, musicPosition);
            int diff = currPosInMs - actualBeat;
            userHitBeats.add(diff);

//            System.out.println(isOnBeat(attemptedBeat, musicPosition));
//            System.out.println("hit at beat: " + musicPosition + " attempted beat hit: " + attemptedBeat + " diff: " + (musicPosition - attemptedBeat));
            System.out.println("hit at pos: " + currPosInMs + " attempted beat hit: " + actualBeat + " diff: " + (currPosInMs - actualBeat));

        }
    }

    /** checks whether use is on beat or not */
    private boolean isOnBeat(int hitPosition, int currPosition) {
        int lowerRange = currPosition - this.beforeOffset;
        int higherRange = currPosition + this.afterOffset;
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
