package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
    private Texture plainBackground;
    /** Tempo-Rary logo */
    private Texture logo;
    /** Buttons */
    private Texture playButton;
    private Texture calibrationButton;
    private Texture levelEditorButton;
    private Texture settingsButton;
    private Texture exitButton;

    private Texture backButton;

    /** Left cap to the status background (grey region) */
    private TextureRegion statusBkgLeft;
    /** Middle portion of the status background (grey region) */
    private TextureRegion statusBkgMiddle;
    /** Right cap to the status background (grey region) */
    private TextureRegion statusBkgRight;
    /** Left cap to the status forground (colored region) */
    private TextureRegion statusFrgLeft;
    /** Middle portion of the status forground (colored region) */
    private TextureRegion statusFrgMiddle;
    /** Right cap to the status forground (colored region) */
    private TextureRegion statusFrgRight;

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

    /** Height of the progress bar */
    private static int PROGRESS_HEIGHT = 93;
    /** Height of the inner progress bar */
    private static int INNER_PROGRESS_HEIGHT = 77;
    /** Padding on the left and right sides of the inner bar */
    private static int X_PADDING = 8;
    /** Width of the rounded cap on left or right */
    private static int PROGRESS_CAP    = 42;
    /** Width of the rounded cap on the left or right for the inner bar */
    private static int INNER_PROGRESS_CAP    = 33;

    private int barWidth;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1200;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 800;
    /** Ratio of the bar width to the screen (artifact of LoadingMode) */
    private static float BAR_WIDTH_RATIO  = 0.5f;
    /** Ration of the bar height to the screen (artifact of LoadingMode) */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of each button */
    private int pressState;

    /** States for the volumes */
    private float musicVolume;
    private float soundFXVolume;

    /* PRESS STATES **/
    /** Initial button state */
    private static final int NO_BUTTON_PRESSED = 100;
    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 101;
    /** Pressed down button state for the level editor button */
    private static final int LEVEL_EDITOR_PRESSED = 102;
    /** Pressed down button state for the calibration button */
    private static final int CALIBRATION_PRESSED = 103;
    private static final int SETTINGS_PRESSED = 104;
    private static final int EXIT_PRESSED = 105;

    private MenuState currentMenuState;
    private int innerWidth;
    private boolean holdingBar;

    // SCENES FOR THE SETTINGS PAGE
    private Stage stage;
    private Table table;
    Container<Table> tableContainer;
    private BitmapFont font;

    // MenuState
    private enum MenuState {
        HOME,
        SETTINGS
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
        logo = directory.getEntry("title", Texture.class);
        background = directory.getEntry( "loading-background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        plainBackground = directory.getEntry("background", Texture.class);
        playButton = directory.getEntry("play", Texture.class);
        levelEditorButton = directory.getEntry("level-editor", Texture.class);
        calibrationButton = directory.getEntry("play-old", Texture.class);
        settingsButton = directory.getEntry("play-old", Texture.class);
        exitButton = directory.getEntry("play-old", Texture.class);
        playButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), canvas.getHeight()/2 + 200);
        levelEditorButtonCoords = new Vector2(centerX + levelEditorButton.getWidth(), canvas.getHeight()/2);
        calibrationButtonCoords = new Vector2(centerX + calibrationButton.getWidth()*2 , centerY);

        statusBkgLeft = directory.getEntry( "slider.backleft", TextureRegion.class );
        statusBkgRight = directory.getEntry( "slider.backright", TextureRegion.class );
        statusBkgMiddle = directory.getEntry( "slider.background", TextureRegion.class );

        statusFrgLeft = directory.getEntry( "slider.foreleft", TextureRegion.class );
        statusFrgRight = directory.getEntry( "slider.foreright", TextureRegion.class );
        statusFrgMiddle = directory.getEntry( "slider.foreground", TextureRegion.class );

        backButton = directory.getEntry("play-old", Texture.class);

        font = directory.getEntry("main", BitmapFont.class);

        addSceneElements();
    }

    private void addSceneElements() {

        Table headerTable = new Table();
        Table controlTable = new Table();
        Table volumeTable = new Table();

        table.add(headerTable).top().left();;
        table.row();
        table.add(controlTable).expand();
        table.row();
        table.add(volumeTable).expand();

        // TODO: add calibration button

        Button.ButtonStyle backButtonStyle = new Button.ButtonStyle();
        backButtonStyle.up = new TextureRegionDrawable(exitButton);
        backButtonStyle.down = new TextureRegionDrawable(exitButton);

        final Button button = new Button(backButtonStyle);

        Slider.SliderStyle musicVolumeSliderStyle = new Slider.SliderStyle();
        musicVolumeSliderStyle.background = new TextureRegionDrawable(statusFrgMiddle);
        musicVolumeSliderStyle.knob = new TextureRegionDrawable(statusFrgRight);

        final Slider musicVolumeSlider = new Slider(0f, 1f, 0.1f, false, musicVolumeSliderStyle);
        musicVolumeSlider.setValue(musicVolume);

        Slider.SliderStyle fxVolumeSliderStyle = new Slider.SliderStyle();
        fxVolumeSliderStyle.background = new TextureRegionDrawable(statusFrgMiddle);
        fxVolumeSliderStyle.knob = new TextureRegionDrawable(statusFrgRight);

        final Slider fxVolumeSlider = new Slider(0f, 1f, 0.05f, false, fxVolumeSliderStyle);
        fxVolumeSlider.setValue(soundFXVolume);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.BLACK;

        final Label musicLabel = new Label("Music Volume", labelStyle);
        final Label fxLabel = new Label("Sound Effects Volume", labelStyle);

        table.row();

        headerTable.add(button);

        table.row();

        controlTable.add(new Label("Notes", labelStyle)).colspan(2);

        // TODO: CHANGE THIS TO LOOP AND ADD SWITCHES
        controlTable.row();

        controlTable.add(new Label("Lane 1", labelStyle)).expand();
        controlTable.add(new Label("D", labelStyle)).expand();

        controlTable.row();

        controlTable.add( new Label("Lane 2", labelStyle)).expandX();
        controlTable.add( new Label("F", labelStyle)).expandX();

        controlTable.row();

        controlTable.add( new Label("Lane 3", labelStyle)).expandX();
        controlTable.add( new Label("J", labelStyle)).expandX();

        controlTable.row();

        controlTable.add( new Label("Lane 4", labelStyle)).expandX();
        controlTable.add( new Label("K", labelStyle)).expandX();

        table.row();

        volumeTable.add(musicLabel);
        volumeTable.add(musicVolumeSlider).width(800);
        volumeTable.row();

        volumeTable.add(fxLabel);
        volumeTable.add(fxVolumeSlider).width(800);

        // LISTENERS
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                switchToHome();
            }
        });

        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                musicVolume = ((Slider) actor).getValue();
            }
        });

        fxVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                soundFXVolume = ((Slider) actor).getValue();
            }
        });
    }

    private void switchToHome() {
        reset();
        Gdx.input.setInputProcessor( this );
    }

    /**
     * Returns true if all assets are loaded and the player presses on a button
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == ExitCode.TO_LEVEL
                || pressState == ExitCode.TO_EDITOR
                || pressState == ExitCode.TO_CALIBRATION
                || pressState == ExitCode.TO_EXIT;
    }

    /**
     * Resets the MenuMode
     */
    public void reset() {
        currentMenuState = MenuState.HOME;
        pressState = NO_BUTTON_PRESSED;
    }

    /**
     * Creates a MenuMode
     *
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        this.canvas = canvas;
        stage = new Stage();
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
//        table.setDebug(true);
//        tableContainer = new Container<>();

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        musicVolume = 1f;
        soundFXVolume = 1f;
        active = false;
        reset();
    }

    @Override
    public void render(float v) {
        if (active) {
            switch (currentMenuState) {
                case HOME:
                    draw();
                    break;
                case SETTINGS:
                    drawSettings();
                    break;
            }
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
        // TODO: make these methods instead
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

        Color settingsButtonTint = (pressState == SETTINGS_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(settingsButton, settingsButtonTint, settingsButton.getWidth()/2, settingsButton.getHeight()/2,
                calibrationButtonCoords.x - settingsButton.getWidth(), calibrationButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color exitButtonTint = (pressState == EXIT_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(exitButton, exitButtonTint, exitButton.getWidth()/2, exitButton.getHeight()/2,
                calibrationButtonCoords.x - 2 * exitButton.getWidth(), calibrationButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
        canvas.end();
    }

    private void drawSettings() {
        canvas.begin();
        canvas.draw(plainBackground, 0, 0, canvas.getWidth(), canvas.getHeight());
//        canvas.draw(backButton, Color.WHITE, backButton.getWidth()/2, backButton.getHeight()/2,
//                calibrationButton.getWidth(), calibrationButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
//
//        float scale = 0.5f;
//
//        canvas.draw(statusBkgLeft,   centerX-barWidth/2, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
//        canvas.draw(statusBkgRight,  centerX+barWidth/2-scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
//        canvas.draw(statusBkgMiddle, centerX-barWidth/2+scale*PROGRESS_CAP, centerY, barWidth-2*scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
//
//        float padding = (barWidth - innerWidth) / 2f;
//        canvas.draw(statusFrgLeft,   centerX-innerWidth/2, centerY +(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
//        float span = (musicVolume)*(innerWidth-2*scale*INNER_PROGRESS_CAP);
//        canvas.draw(statusFrgRight,  centerX-innerWidth/2+scale*INNER_PROGRESS_CAP+span, centerY+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
//        canvas.draw(statusFrgMiddle, centerX-innerWidth/2+scale*INNER_PROGRESS_CAP, centerY+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, span, scale*INNER_PROGRESS_HEIGHT);
//
//        canvas.draw(statusBkgLeft,   centerX-barWidth/2, centerY + 100, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
//        canvas.draw(statusBkgRight,  centerX+barWidth/2-scale*PROGRESS_CAP, centerY + 100, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
//        canvas.draw(statusBkgMiddle, centerX-barWidth/2+scale*PROGRESS_CAP, centerY + 100, barWidth-2*scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
//
//        canvas.draw(statusFrgLeft,   centerX-innerWidth/2, centerY + 100+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
//        float span2 = (soundFXVolume)*(innerWidth-2*scale*INNER_PROGRESS_CAP);
//        canvas.draw(statusFrgRight,  centerX-innerWidth/2+scale*INNER_PROGRESS_CAP+span2, centerY + 100+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, scale*INNER_PROGRESS_CAP, scale*INNER_PROGRESS_HEIGHT);
//        canvas.draw(statusFrgMiddle, centerX-innerWidth/2+scale*INNER_PROGRESS_CAP, centerY + 100+(PROGRESS_HEIGHT - INNER_PROGRESS_HEIGHT)/4f+1, span2, scale*INNER_PROGRESS_HEIGHT);
        canvas.end();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // SETTINGS VALUES (this needs to change after the fact)
    // also add keybinds
    public float getMusicVolumeSetting() {
        return musicVolume;
    }

    public float getFXVolumeSetting() {
        return soundFXVolume;
    }

    // TODO: fix this method
    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        barWidth = (int)(BAR_WIDTH_RATIO*width);
        innerWidth = (int)(BAR_WIDTH_RATIO*0.97*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;

        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor( this );
        active = true;
    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {
        canvas = null;
        stage.dispose();
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

    public boolean isButtonPressed(int screenX, int screenY, float width, float height, Vector2 buttonCoords) {
        float xRadius = width/2.0f;
        boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;
        float yRadius = height/2.0f;
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
                || pressState == ExitCode.TO_CALIBRATION
                || pressState == ExitCode.TO_EXIT) {
            return true;
        }
        float radius = BUTTON_SCALE * scale * calibrationButton.getWidth() / 2.0f;
        // Flip to match graphics coordinates
        screenY = heightY - screenY;
        switch (currentMenuState) {
            case HOME:
            // check if buttons get pressed appropriately
            if (isButtonPressed(screenX, screenY, playButton, playButtonCoords)) {
        //            pressState = PLAY_PRESSED;
                pressState = PLAY_PRESSED;
            }
            if (isButtonPressed(screenX, screenY, levelEditorButton, levelEditorButtonCoords)) {
                pressState = LEVEL_EDITOR_PRESSED;
            }

            float dist = (screenX - calibrationButtonCoords.x) * (screenX - calibrationButtonCoords.x)
                    + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
            if (dist < radius * radius) {
                pressState = CALIBRATION_PRESSED;
            }

            float distSettings = (screenX - (calibrationButtonCoords.x - calibrationButton.getWidth())) * (screenX - (calibrationButtonCoords.x - calibrationButton.getWidth()))
                    + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
            if (distSettings < radius * radius) {
                pressState = SETTINGS_PRESSED;
            }

            float distExit = (screenX - (calibrationButtonCoords.x - 2 * calibrationButton.getWidth())) * (screenX - (calibrationButtonCoords.x - 2 * calibrationButton.getWidth()))
                    + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
            if (distExit < radius * radius) {
                pressState = EXIT_PRESSED;
            }
            break;

            case SETTINGS:
                float distBack = (screenX - (calibrationButton.getWidth())) * (screenX - (calibrationButton.getWidth()))
                        + (screenY - calibrationButtonCoords.y) * (screenY - calibrationButtonCoords.y);
                if (distBack < radius * radius) {
                    currentMenuState = MenuState.HOME;
                    Gdx.input.setInputProcessor(this);
                }
                float barWidth = (innerWidth-2*scale*INNER_PROGRESS_CAP);
                if (isButtonPressed(screenX, screenY, barWidth, scale*INNER_PROGRESS_HEIGHT, new Vector2(centerX, centerY))) {
                    holdingBar = true;
                }
                break;
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
                return false;
            case SETTINGS_PRESSED:
                currentMenuState = MenuState.SETTINGS;
                pressState = NO_BUTTON_PRESSED;
                Gdx.input.setInputProcessor(stage);
                break;
            case EXIT_PRESSED:
                pressState = ExitCode.TO_EXIT;
            default:
                break;
        }
        holdingBar = false;
        return true;
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

    /**
     * Called when the mouse or finger was dragged.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (holdingBar) {
            musicVolume = 1 - ((screenX - centerX - innerWidth / 2) * 1.0f / innerWidth * -1);
            if (musicVolume > 1) musicVolume = 1;
            if (musicVolume < 0) musicVolume = 0;
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
    public boolean keyDown(int keycode) {return true;}

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char keycode) {return true;}

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {return true;}

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {return true;}

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {return true;}

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
    public boolean axisMoved (Controller controller, int axisCode, float value) {return true;}
}
