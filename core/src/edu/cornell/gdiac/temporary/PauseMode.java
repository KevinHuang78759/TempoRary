package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class PauseMode implements Screen, InputProcessor, ControllerListener {

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Background texture */
    private Texture background;

    /* BUTTON LOCATIONS */
    /** Scale at which to draw the buttons */
    private static float BUTTON_SCALE  = 0.75f;
    /** The y-coordinate of the center of the progress bar (artifact of LoadingMode) */
    private int centerY;
    /** The x-coordinate of the center of the progress bar (artifact of LoadingMode) */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

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
    /** Initial button state */
    private static final int NO_BUTTON_PRESSED = 0;

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
    }

    /**
     * Returns true if all assets are loaded and the player presses on a button
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == ExitCode.TO_PLAYING
                || pressState == ExitCode.TO_EDITOR
                || pressState == ExitCode.TO_CALIBRATION;
    }

    /**
     * Resets the MenuMode
     */
    public void reset() {
        pressState = NO_BUTTON_PRESSED;
        Gdx.input.setInputProcessor( this );
    }

    /**
     * Creates a MenuMode
     *
     * @param canvas 	The game canvas to draw to
     */
    public PauseMode(GameCanvas canvas) {
        this.canvas  = canvas;
        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        active = false;
        reset();
    }

    @Override
    public void render(float delta) {
        if (active) {
            //draw();
            // We are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, pressState);
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
        canvas.end();
    }

    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

//        this.width = (int)(BAR_WIDTH_RATIO*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;
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

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }
}
