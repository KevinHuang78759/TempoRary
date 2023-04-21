package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * This class is just a ported over LoadingMode without the asset loading part, only the menu part
 */
public class MenuMode implements Screen, InputProcessor {

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Background texture */
    private Texture background;
    /** Tempo-Rary logo */
    private Texture logo;
    /** Buttons */
    private Texture playButton;
    private Texture calibrationButton;
    private Texture levelEditorButton;

    /* BUTTON LOCATIONS */
    /** Play button x and y coordinates represented as a vector */
    private Vector2 playButtonCoords;
    /** Level editor button x and y coordinates represented as a vector */
    private Vector2 levelEditorButtonCoords;
    /** Calibration button x and y coordinates represented as a vector */
    private Vector2 calibrationButtonCoords;
    /** Scale at which to draw the buttons */
    private static float BUTTON_SCALE  = 0.75f;
    /** The y-coordinate of the center of the progress bar (artifact of LoadingMode) */
    private int centerY;
    /** The x-coordinate of the center of the progress bar (artifact of LoadingMode) */
    private int centerX;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1200;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 800;
    /** Ratio of the bar width to the screen (artifact of LoadingMode) */
    private static float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen (artifact of LoadingMode) */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of each button */
    private int pressState;

    /* PRESS STATES **/
    // TODO: potentially change this to enums
    /** Initial button state */
    private static final int INITIAL = 0;
    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 1;
    /** Exit code for button to go to the game */
    public static final int TO_GAME = 2;
    /** Pressed down button state for the level editor button */
    private static final int LEVEL_EDITOR_PRESSED = 3;
    /** Exit code for the button to go to the level editor */
    public static final int TO_LEVEL_EDITOR = 4;
    /** Exit code for the button to go to the level editor */
    public static final int TO_CALIBRATION = 5;

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
        logo = directory.getEntry("title", Texture.class);
        background = directory.getEntry( "background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        playButton = directory.getEntry("play",Texture.class);
        levelEditorButton = directory.getEntry("level-editor",Texture.class);
        calibrationButton = directory.getEntry("play-old",Texture.class);
        playButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), canvas.getHeight()/2 + 200);
        levelEditorButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), canvas.getHeight()/2);
        calibrationButtonCoords = new Vector2(centerX + calibrationButton.getWidth()*2 , centerY);
    }

    /**
     * Returns true if all assets are loaded and the player presses on a button
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == TO_GAME || pressState == TO_LEVEL_EDITOR || pressState == TO_CALIBRATION;
    }

    /**
     * Creates a MenuMode
     *
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        this.canvas  = canvas;
        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        pressState = INITIAL;
        Gdx.input.setInputProcessor( this );
    }

    @Override
    public void render(float v) {
        if (active) {
            draw();

            // We are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background, 0, 0, canvas.getWidth(), canvas.getHeight());
        Color playButtonTint = (pressState == PLAY_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color levelEditorButtonTint = (pressState == LEVEL_EDITOR_PRESSED ? Color.GREEN: Color.WHITE);
        canvas.draw(levelEditorButton, levelEditorButtonTint, levelEditorButton.getWidth()/2, levelEditorButton.getHeight()/2,
                    levelEditorButtonCoords.x, levelEditorButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        canvas.draw(logo, Color.WHITE, logo.getWidth()/2, logo.getHeight()/2,
                    logo.getWidth()/2+50, centerY+300, 0,scale, scale);

        //draw calibration button
        Color tintCalibration = (pressState == TO_CALIBRATION ? Color.GRAY: Color.RED);
        canvas.draw(calibrationButton, tintCalibration, calibrationButton.getWidth()/2, calibrationButton.getHeight()/2,
                    calibrationButtonCoords.x, calibrationButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
        canvas.end();
    }

    // TODO: fix this method
    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

//        this.width = (int)(BAR_WIDTH_RATIO*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
//        heightY = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {

    }

    // PROCESSING PLAYER INPUT
    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }
}
