package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * This class is just a ported over LoadingMode without the asset loading part, only the menu part
 */
public class MenuMode implements Screen, InputProcessor, ControllerListener {

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

    /* PRESS STATES **/
    /** Initial button state */
    private static final int NO_BUTTON_PRESSED = 0;
    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 101;
    /** Pressed down button state for the level editor button */
    private static final int LEVEL_EDITOR_PRESSED = 102;
    /** Pressed down button state for the calibration button */
    private static final int CALIBRATION_PRESSED = 103;

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
        background = directory.getEntry( "loading-background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        playButton = directory.getEntry("play", Texture.class);
        levelEditorButton = directory.getEntry("level-editor", Texture.class);
        calibrationButton = directory.getEntry("play-old", Texture.class);
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
        return pressState == ExitCode.TO_LEVEL
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
    public MenuMode(GameCanvas canvas) {
        this.canvas  = canvas;
        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        active = false;
        reset();
    }

    @Override
    public void render(float v) {
        if (active) {
            draw();
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
        Color playButtonTint = (pressState == PLAY_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color levelEditorButtonTint = (pressState == LEVEL_EDITOR_PRESSED ? Color.GREEN: Color.WHITE);
        canvas.draw(levelEditorButton, levelEditorButtonTint, levelEditorButton.getWidth()/2, levelEditorButton.getHeight()/2,
                    levelEditorButtonCoords.x, levelEditorButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        canvas.draw(logo, Color.WHITE, logo.getWidth()/2, logo.getHeight()/2,
                    logo.getWidth()/2+50, centerY+300, 0,scale, scale);

        //draw calibration button
        Color tintCalibration = (pressState == CALIBRATION_PRESSED ? Color.GRAY: Color.RED);
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

    // PROCESSING PLAYER INPUT

    /**
     * Checks to see if the location clicked at `screenX`, `screenY` are within the bounds of the given button
     * `buttonTexture` and `buttonCoords` should refer to the appropriate button parameters
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param buttonTexture the specified button texture
     * @param buttonCoords the specified button coordinates as a Vector2 object
     * @return whether the button specified was pressed
     */
    public boolean isButtonPressed(int screenX, int screenY, Texture buttonTexture, Vector2 buttonCoords) {
        // buttons are rectangles
        // buttonCoords hold the center of the rectangle, buttonTexture has the width and height
        // get half the x length of the button portrayed
        float xRadius = BUTTON_SCALE * scale * buttonTexture.getWidth()/2.0f;
        boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

        // get half the y length of the button portrayed
        float yRadius = BUTTON_SCALE * scale * buttonTexture.getHeight()/2.0f;
        boolean yInBounds = buttonCoords.y - yRadius <= screenY && buttonCoords.y + yRadius >= screenY;
        return xInBounds && yInBounds;
    }

    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressState == ExitCode.TO_LEVEL
                || pressState == ExitCode.TO_EDITOR
                || pressState == ExitCode.TO_CALIBRATION) {
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        // check if buttons get pressed appropriately
        if (isButtonPressed(screenX, screenY, playButton, playButtonCoords)) {
//            pressState = PLAY_PRESSED;
            pressState = PLAY_PRESSED;
        }
        if (isButtonPressed(screenX, screenY, levelEditorButton, levelEditorButtonCoords)) {
            pressState = LEVEL_EDITOR_PRESSED;
        }

        float radius = BUTTON_SCALE*scale*calibrationButton.getWidth()/2.0f;
        float dist = (screenX-calibrationButtonCoords.x)*(screenX-calibrationButtonCoords.x)
                +(screenY-calibrationButtonCoords.y)*(screenY-calibrationButtonCoords.y);
        if (dist < radius*radius) {
            pressState = ExitCode.TO_CALIBRATION;
        }
        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        switch (pressState){
            case PLAY_PRESSED:
                pressState = ExitCode.TO_LEVEL;
                return false;
            case LEVEL_EDITOR_PRESSED:
                pressState = ExitCode.TO_EDITOR;
                return false;
            case CALIBRATION_PRESSED:
                pressState = ExitCode.TO_CALIBRATION;
            default:
                return true;
        }
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        // note: does not work for level editor
        if (pressState == NO_BUTTON_PRESSED) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart) {
                pressState = PLAY_PRESSED;
                return false;
            }
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        // note: does not work for level editor
        if (pressState == PLAY_PRESSED) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                pressState = ExitCode.TO_LEVEL;
                return false;
            }
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char keycode) {
        return true;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }
}
