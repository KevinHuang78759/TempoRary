package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * This class is just a ported over LoadingMode without the asset loading part, only the menu part
 */
public class MenuMode implements Screen, InputProcessor, ControllerListener {

    SoundController<Integer> s;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Background texture */
    private Texture background;
    private Texture catOrnament;
    private Texture settingsBackground;
    /** Tempo-Rary logo */
    private Texture logo;
    /** Buttons */
    private Texture playButton;
    private Texture calibrationButton;
    private Texture calibrationButtonPressed;
    private Texture settingsButton;
    private Texture exitButton;
    private Texture settingsButtonHover;
    private Texture exitButtonHover;
    private Texture playButtonHover;

    /* BUTTON LOCATIONS */
    /** Scale at which to draw the buttons */
    private static float BUTTON_SCALE  = 0.7f;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1200;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 800;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of each button */
    private int pressState;
    private int hoverState;

    /* PRESS STATES **/
    /** Initial button state */
    private static final int NO_BUTTON_PRESSED = 100;
    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 101;
    /** Pressed down button state for the level editor button */
    private static final int LEVEL_EDITOR_PRESSED = 102;
    private static final int SETTINGS_PRESSED = 104;
    private static final int EXIT_PRESSED = 105;

    private static final int NO_BUTTON_HOVERED = 106;
    private static final int PLAY_HOVERED = 107;
    private static final int SETTINGS_HOVERED = 110;
    private static final int EXIT_HOVERED = 111;

    // START SETTINGS

    /** */
    private int currBandMemberCount;

    /** Values for the volumes */
    private static float musicVolume;
    private static float soundFXVolume;

    private MenuState currentMenuState;

    // SCENES FOR THE SETTINGS PAGE
    private Stage stage;
    private Table mainTable;
    private final Table controlTable = new Table();
    private Container<Table> tableContainer;
    private ButtonGroup<TextButton> buttonGroup;
    private Dialog dialog;

    // styles
    private Button.ButtonStyle backButtonStyle = new Button.ButtonStyle();
    private Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
    private Label.LabelStyle labelStyle = new Label.LabelStyle();
    private Label.LabelStyle headerStyle = new Label.LabelStyle();
    private Label.LabelStyle header2Style = new Label.LabelStyle();
    private Label.LabelStyle boldLabelStyle = new Label.LabelStyle();
    private Label.LabelStyle regularStyle = new Label.LabelStyle();
    private CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
    private SelectBox.SelectBoxStyle dropdownStyle = new SelectBox.SelectBoxStyle();
    private Window.WindowStyle windowStyle = new Window.WindowStyle();

    // Scene2D UI components
    // settings image assets
    private Texture settingsHeader;
    private Texture backButtonTexture;
    private Texture headerLine;
    private Texture primaryBox;
    private Texture secondaryBox;
    private Texture primaryBoxActive;
    private Texture primaryBoxUnbound;
    private Texture secondaryBoxActive;
    private Texture secondaryBoxUnbound;
    private Texture fishOutline;
    private Texture catOutline;
    private Texture sliderBackground;
    private Texture sliderForeground;
    private Texture sliderButton;
    private Texture checkboxOff;
    private Texture checkboxOn;
    private Texture selectBackground;
    private Texture scrollBackground;
    private Texture listSelectBackground;
    private Texture dialogBackground;
    private Texture leftButtonGroup;
    private Texture middleButtonGroup;
    private Texture rightButtonGroup;
    private Texture buttonGroupSelected;
    private Texture leftButtonGroupSelected;
    private Texture rightButtonGroupSelected;

    private Texture switchSelectBackground;

    // fonts
    private BitmapFont blinkerBold;
    private BitmapFont blinkerSemiBold;
    private BitmapFont blinkerSemiBoldSmaller;
    private BitmapFont blinkerRegular;

    private Color fontColor = new Color(24f/255, 2f/255, 99f/255, 1);

    private int laneChange = 0;
    private boolean switchChanged = false;
    private boolean changeMain = true;
    private int prevWidth;
    private int prevHeight;

    private ObjectMap<String, Texture> keyImageMap = new ObjectMap<>();

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

        logo = directory.getEntry("meowzart-title", Texture.class);
        background = directory.getEntry( "menu-background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        catOrnament = directory.getEntry("menu-background-cat", Texture.class);
        settingsBackground = directory.getEntry("settings-background", Texture.class);
        playButton = directory.getEntry("play-button", Texture.class);
        playButtonHover = directory.getEntry("play-button-active", Texture.class);
        settingsButton = directory.getEntry("settings", Texture.class);
        settingsButtonHover = directory.getEntry("settings-active", Texture.class);
        exitButton = directory.getEntry("quit-button-menu", Texture.class);
        exitButtonHover = directory.getEntry("quit-button-active-menu", Texture.class);

        // UI assets for settings
        settingsHeader = directory.getEntry("settings-header", Texture.class);
        backButtonTexture = directory.getEntry("back-arrow", Texture.class);
        headerLine = directory.getEntry("header-line", Texture.class);
        primaryBox = directory.getEntry("primary-box", Texture.class);
        primaryBoxActive = directory.getEntry("primary-box-active", Texture.class);
        primaryBoxUnbound = directory.getEntry("primary-box-unbound", Texture.class);
        secondaryBox = directory.getEntry("secondary-box", Texture.class);
        secondaryBoxActive = directory.getEntry("secondary-box-active", Texture.class);
        secondaryBoxUnbound = directory.getEntry("secondary-box-unbound", Texture.class);
        fishOutline = directory.getEntry("fish-outline", Texture.class);
        catOutline = directory.getEntry("cat-outline", Texture.class);
        sliderBackground = directory.getEntry("slider-background", Texture.class);
        sliderForeground = directory.getEntry("slider-foreground", Texture.class);
        sliderButton = directory.getEntry("slider-button", Texture.class);
        checkboxOff = directory.getEntry("checkbox", Texture.class);
        checkboxOn = directory.getEntry("checkbox-checked", Texture.class);
        selectBackground = directory.getEntry("select-background", Texture.class);
        scrollBackground = directory.getEntry("scroll-background", Texture.class);
        listSelectBackground =  directory.getEntry("list-select-background", Texture.class);
        dialogBackground = directory.getEntry("dialog-background", Texture.class);
        calibrationButton = directory.getEntry("calibration-button", Texture.class);
        calibrationButtonPressed = directory.getEntry("calibration-button-pressed", Texture.class);
        leftButtonGroup = directory.getEntry("left-button-group", Texture.class);
        middleButtonGroup = directory.getEntry("middle-button-group", Texture.class);
        rightButtonGroup = directory.getEntry("right-button-group", Texture.class);
        leftButtonGroupSelected = directory.getEntry("left-selected-button-group", Texture.class);
        buttonGroupSelected = directory.getEntry("button-group-selected", Texture.class);
        rightButtonGroupSelected = directory.getEntry("right-selected-button-group", Texture.class);

        switchSelectBackground = directory.getEntry("switch-select-background", Texture.class);

        keyImageMap.put("Left", directory.getEntry("left-arrow-graphic", Texture.class));
        keyImageMap.put("Right", directory.getEntry("right-arrow-graphic", Texture.class));
        keyImageMap.put("Down", directory.getEntry("down-arrow-graphic", Texture.class));
        keyImageMap.put("Up", directory.getEntry("up-arrow-graphic", Texture.class));

        // add Scene2D
        defineStyles();
        dialog = new Dialog("", windowStyle);
        addSceneElements();
    }

    private void defineStyles() {
        backButtonStyle.up = new TextureRegionDrawable(backButtonTexture);
        backButtonStyle.down = new TextureRegionDrawable(backButtonTexture);

        sliderStyle.background = new TextureRegionDrawable(sliderBackground);
        sliderStyle.knob = new TextureRegionDrawable(sliderButton);
        sliderStyle.knobBefore = new TextureRegionDrawable(sliderForeground);

        labelStyle = new Label.LabelStyle();
        labelStyle.font = blinkerSemiBold;
        labelStyle.fontColor = fontColor;

        headerStyle = new Label.LabelStyle();
        headerStyle.font = blinkerBold;
        headerStyle.fontColor = fontColor;

        header2Style = new Label.LabelStyle();
        header2Style.font = blinkerBold;
        header2Style.fontColor = fontColor;

        boldLabelStyle = new Label.LabelStyle();
        boldLabelStyle.font = blinkerSemiBoldSmaller;
        boldLabelStyle.fontColor = fontColor;

        regularStyle = new Label.LabelStyle();
        regularStyle.font = blinkerRegular;
        regularStyle.fontColor = fontColor;

        checkBoxStyle.checkboxOff = new TextureRegionDrawable(checkboxOff);
        checkBoxStyle.checkboxOn = new TextureRegionDrawable(checkboxOn);
        checkBoxStyle.font = blinkerRegular;

        dropdownStyle.font = blinkerSemiBoldSmaller;
        dropdownStyle.fontColor = fontColor;
        dropdownStyle.background = new TextureRegionDrawable(selectBackground);
        dropdownStyle.listStyle = new List.ListStyle();
        dropdownStyle.listStyle.selection = new TextureRegionDrawable(listSelectBackground);
        dropdownStyle.listStyle.font = blinkerSemiBoldSmaller;
        dropdownStyle.listStyle.fontColorUnselected = fontColor;
        dropdownStyle.listStyle.fontColorSelected = Color.WHITE;
        dropdownStyle.scrollStyle = new ScrollPane.ScrollPaneStyle();
        dropdownStyle.scrollStyle.background = new TextureRegionDrawable(scrollBackground);

        windowStyle.background = new TextureRegionDrawable(dialogBackground);
        windowStyle.titleFont = blinkerRegular;
    }

    // TODO: ADD A WAY TO RESET ALL SETTINGS AND CLEAR SAVE DATA
    private void addSceneElements() {
        // MAIN TABLE IS 3 COLUMNS, EACH TABLE ADDED SHOULD SPAN 3 COLUMNS
        Table headerTable = new Table();
        Table switchTable = new Table();
        Table volumeTable = new Table();

        final Button backButton = new Button(backButtonStyle);

        final Slider musicVolumeSlider = new Slider(0f, 1f, 0.05f, false, sliderStyle);
        musicVolumeSlider.setValue(musicVolume);

        final Slider fxVolumeSlider = new Slider(0f, 1f, 0.05f, false, sliderStyle);
        fxVolumeSlider.setValue(soundFXVolume);

        mainTable.row().padBottom(10).expandX().fill();

        // back button
        headerTable.add(backButton).top().left();
        // SETTINGS header
        headerTable.add(new Image(settingsHeader)).expand();

        ImageButton.ImageButtonStyle calibrationButtonStyle = new ImageButton.ImageButtonStyle();
        calibrationButtonStyle.up = new TextureRegionDrawable(calibrationButton);
        calibrationButtonStyle.down = new TextureRegionDrawable(calibrationButtonPressed);
        Button calibButton = new ImageButton(calibrationButtonStyle);
        headerTable.add(calibButton);

        headerTable.add(switchTable);

        mainTable.add(headerTable).expandX().colspan(3);

        mainTable.row();

        mainTable.add(new Image(headerLine)).bottom().right().expandX().padBottom(12).padRight(20);
        mainTable.add(new Label("CLICK TO REBIND CONTROLS", header2Style));
        mainTable.add(new Image(headerLine)).bottom().left().expandX().padBottom(12).padLeft(20);

        mainTable.row().expand().fill();

        // make control table
        regenerateControlTable(controlTable);

        mainTable.add(controlTable).expandX().colspan(3);

        mainTable.row();

        // Volume table stuff

        mainTable.add(new Image(headerLine)).bottom().right().expandX().padBottom(12).padRight(10);
        mainTable.add(new Label("DRAG TO ADJUST VOLUME", header2Style));
        mainTable.add(new Image(headerLine)).bottom().left().expandX().padBottom(12).padLeft(10);

        mainTable.row().padLeft(10).padRight(35).expand().fill();
        mainTable.add(volumeTable).expand().colspan(3);

        volumeTable.add(new Label("Music", labelStyle)).expandX();
        volumeTable.add(musicVolumeSlider).width(800);
        volumeTable.row().padTop(30);

        volumeTable.add(new Label("Sound FX", labelStyle)).expandX();
        volumeTable.add(fxVolumeSlider).width(800);

        // WINDOW FOR KEYBINDING

        dialog.setModal(true);
        dialog.setMovable(false);
        dialog.text(new Label("Press a button to rebind", labelStyle));
        dialog.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    dialog.hide(null);
                } else {
                    if (!switchChanged) {
                        InputController.getInstance().setKeybinding(laneChange, keycode, changeMain);
                    } else {
                        InputController.getInstance().setKeybinding(currBandMemberCount - 1, laneChange, keycode, changeMain);
                    }
                    dialog.hide(null);
                }
                controlTable.clear();
                regenerateControlTable(controlTable);
                return true;
            }

            public boolean keyUp(InputEvent event, int keycode) {
                return true;
            }
        });

        // LISTENERS
        calibButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                s.playSound(0, 0.3f);
                pressState = ExitCode.TO_CALIBRATION;
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                s.playSound(0, 0.3f);
                SaveManager.getInstance().saveSettings(InputController.triggerBindingsMain,
                        InputController.switchesBindingsMain,
                        musicVolume,
                        soundFXVolume);
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
                float oldVal = soundFXVolume;
                soundFXVolume = ((Slider) actor).getValue();
                SoundController.setVolumeAdjust(soundFXVolume);
                if (oldVal != ((Slider) actor).getValue()) {
                    s.playSound(0, 0.3f);
                }
            }
        });
    }

    private void regenerateControlTable(final Table table) {
        // button group
        HorizontalGroup horizontalGroup = new HorizontalGroup();
        buttonGroup = new ButtonGroup<>();

        for (int i = 1; i < InputController.MAX_BAND_MEMBERS; i++) {
            TextButton.TextButtonStyle tempStyle = new TextButton.TextButtonStyle();
            if (i == 1) {
                tempStyle.up = new TextureRegionDrawable(leftButtonGroup);
                tempStyle.checked = new TextureRegionDrawable(leftButtonGroupSelected);
            } else if (i == InputController.MAX_BAND_MEMBERS - 1) {
                tempStyle.up = new TextureRegionDrawable(rightButtonGroup);
                tempStyle.checked = new TextureRegionDrawable(rightButtonGroupSelected);
            } else {
                tempStyle.up = new TextureRegionDrawable(middleButtonGroup);
                tempStyle.checked = new TextureRegionDrawable(buttonGroupSelected);
            }
            tempStyle.checkedFontColor = Color.WHITE;
            tempStyle.font = blinkerSemiBoldSmaller;
            tempStyle.fontColor = fontColor;
            TextButton memberSelectButton = new TextButton(i + 1 + " Members", tempStyle);

            buttonGroup.add(memberSelectButton);

            memberSelectButton.setChecked(i == currBandMemberCount - 1);
            horizontalGroup.addActor(memberSelectButton);
        }

        // controls for note hits
        table.add(new Label("Note Hits", labelStyle)).padBottom(30).expandX();
        String[] currentTriggerKeybinds = InputController.triggerKeyBinds(true);

        for (int i = 0; i < 4; i++) {
            table.add(getControlWidget(i + 1, currentTriggerKeybinds[i], fishOutline)).padBottom(30);
        }

        table.row();

        table.add();
        table.add(horizontalGroup).colspan(4).left();
        table.row();

        // controls for switching
        table.add(new Label("Members Switches", labelStyle));

        String[] currentSwitchKeybinds = InputController.switchKeyBinds(currBandMemberCount - 1);

        for (int i = 0; i < currBandMemberCount; i++) {
            table.add(getControlWidget(i + 1, currentSwitchKeybinds[i], catOutline)).expandX();
        }
        for (int i = 0; i < 4 - currBandMemberCount; i++) {
            table.add(new Table()).expandX();
        }
    }

    private Table getControlWidget(final int nth, final String primaryKeybind, final Texture outlineImage) {
        Table tempTable = new Table();
        tempTable.add(new Label("" + nth + "", boldLabelStyle));

        tempTable.row();

        Image image = new Image(outlineImage);
        tempTable.add(image);

        TextButton.TextButtonStyle primaryKeyStyle = new TextButton.TextButtonStyle();
        TextButton.TextButtonStyle secondaryKeyStyle = new TextButton.TextButtonStyle();

        primaryKeyStyle.up = new TextureRegionDrawable(primaryBox);
        primaryKeyStyle.down = new TextureRegionDrawable(primaryBoxActive);
        primaryKeyStyle.font = blinkerRegular;
        primaryKeyStyle.fontColor = fontColor;

        secondaryKeyStyle.up = new TextureRegionDrawable(secondaryBox);
        secondaryKeyStyle.down = new TextureRegionDrawable(secondaryBoxActive);
        secondaryKeyStyle.font = blinkerRegular;
        secondaryKeyStyle.fontColor = fontColor;

        final Button primaryKeyButton = keyImageMap.get(primaryKeybind) != null ?
                new ImageButton(new TextureRegionDrawable(keyImageMap.get(primaryKeybind))) : new TextButton(primaryKeybind, primaryKeyStyle);
        if (primaryKeybind.equals("")) {
            primaryKeyButton.getStyle().up = new TextureRegionDrawable(primaryBoxUnbound);
        } else {
            primaryKeyButton.getStyle().up = new TextureRegionDrawable(primaryBox);
        }
        primaryKeyButton.getStyle().down = new TextureRegionDrawable(primaryBoxActive);
        primaryKeyButton.getStyle().checked = new TextureRegionDrawable(primaryBoxActive);
        if (primaryKeyButton instanceof TextButton) {
            ((TextButton) primaryKeyButton).getStyle().checkedFontColor = Color.WHITE;
            ((TextButton) primaryKeyButton).getStyle().downFontColor = Color.WHITE;
        }

        ChangeListener buttonChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                s.playSound(0, 0.3f);
                laneChange = nth - 1;
                switchChanged = outlineImage == catOutline;
                changeMain = actor == primaryKeyButton;
                dialog.show(stage,null);
                dialog.setPosition((float)Math.round((stage.getWidth() - dialog.getWidth()) / 2.0f), (float)Math.round((stage.getHeight() - dialog.getHeight()) / 2.0f));
                ((Button) actor).setChecked(true);
            }
        };

        tempTable.add(primaryKeyButton);

        primaryKeyButton.addListener(buttonChangeListener);

        return tempTable;
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
        hoverState = NO_BUTTON_HOVERED;
    }

    /**
     * Creates a MenuMode
     *
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        reset();

        s = new SoundController<>();
        s.addSound(0, "sound/click.ogg");
        this.canvas = canvas;
        stage = new Stage(new ExtendViewport(1200, 800));
        mainTable = new Table();
        tableContainer = new Container<>();

        // scene 2D UI
        tableContainer.setFillParent(true);
        tableContainer.fill();
        tableContainer.setActor(mainTable);
        stage.addActor(tableContainer);

        tableContainer.pad(50);

        buttonGroup = new ButtonGroup<>();

        // fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        blinkerBold = generator.generateFont(parameter);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-SemiBold.ttf"));
        parameter.size = 35;
        blinkerSemiBold = generator.generateFont(parameter);

        parameter.size = 20;
        blinkerSemiBoldSmaller = generator.generateFont(parameter);

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Regular.ttf"));
        parameter.size = 20;
        blinkerRegular = generator.generateFont(parameter);

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        currBandMemberCount = 2;
        musicVolume = SaveManager.getInstance().getMusicVolume();
        soundFXVolume = SaveManager.getInstance().getFXVolume();
        active = false;
    }

    @Override
    public void render(float v) {
        if (active) {
            switch (currentMenuState) {
                case HOME:
                    draw();
                    break;
                case SETTINGS:
                    int prev = currBandMemberCount;
                    currBandMemberCount = buttonGroup.getCheckedIndex() + 2;
                    if (prev != currBandMemberCount) {
                        controlTable.clear();
                        regenerateControlTable(controlTable);
                    }
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
        canvas.drawBackground(background, 0, 0);

        canvas.draw(logo, Color.WHITE, logo.getWidth()/2, logo.getHeight()/2,
                canvas.getWidth()/2, 0.65f * canvas.getHeight(), 0, 0.52f * scale, 0.52f * scale);

        canvas.drawBackground(catOrnament, 0, 0);

        if (pressState == PLAY_PRESSED || hoverState == PLAY_HOVERED) {
            canvas.draw(playButtonHover, Color.WHITE, playButtonHover.getWidth() / 2, playButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.35f * canvas.getHeight(), 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        } else {
            canvas.draw(playButton, Color.WHITE, playButton.getWidth() / 2, playButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.35f * canvas.getHeight(), 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        }

        Color settingsTint = (hoverState == SETTINGS_HOVERED || pressState == SETTINGS_PRESSED ? Color.LIGHT_GRAY: Color.WHITE);
        canvas.draw(settingsButton, settingsTint, settingsButton.getWidth() / 2, settingsButton.getHeight() / 2,
                0.92f * canvas.getWidth(), 0.9f * canvas.getHeight(), 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        Color exitTint = (hoverState == EXIT_HOVERED || pressState == EXIT_PRESSED ? Color.LIGHT_GRAY: Color.WHITE);
        canvas.draw(exitButton, exitTint, exitButton.getWidth() / 2, exitButton.getHeight() / 2,
                0.08f * canvas.getWidth(), 0.9f * canvas.getHeight(), 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        canvas.end();
    }

    private void drawSettings() {
        canvas.begin();
        canvas.draw(settingsBackground, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    // TODO: ADD IT BACK AS A STATIC FIELD
    // SETTINGS VALUES (this needs to change after the fact)
    // also add keybinds
    public static float getMusicVolumeSetting() {
        return musicVolume;
    }

    public static float getFXVolumeSetting() {
        return soundFXVolume;
    }

    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (Math.min(sx, sy));

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
        stage = null;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // TODO: DOCUMENT/MOVE
    public boolean isButtonPressed(int screenX, int screenY, Texture buttonTexture, float x, float y, float scale) {
        // buttons are rectangles
        // buttonCoords hold the center of the rectangle, buttonTexture has the width and height
        // get half the x length of the button portrayed
        float xRadius = scale * buttonTexture.getWidth() / 2.0f;
        boolean xInBounds = x - xRadius <= screenX && x + xRadius >= screenX;

        // get half the y length of the button portrayed
        float yRadius = scale * buttonTexture.getHeight() / 2.0f;
        boolean yInBounds = y - yRadius <= screenY && y + yRadius >= screenY;
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
        // Flip to match graphics coordinates
        screenY = heightY - screenY;
        if (currentMenuState == MenuState.HOME) {// check if buttons get pressed appropriately
            if (isButtonPressed(screenX, screenY, playButton, canvas.getWidth()/2, canvas.getHeight()*0.35f, BUTTON_SCALE)) {
                s.playSound(0, 0.3f);
                pressState = PLAY_PRESSED;
            }
            if (isButtonPressed(screenX, screenY, settingsButton, 0.92f * canvas.getWidth(), 0.9f * canvas.getHeight(), BUTTON_SCALE)) {
                s.playSound(0, 0.3f);
                pressState = SETTINGS_PRESSED;
            }
            if (isButtonPressed(screenX, screenY, exitButton, 0.08f * canvas.getWidth(), 0.9f * canvas.getHeight(), BUTTON_SCALE)) {
                s.playSound(0, 0.3f);
                pressState = EXIT_PRESSED;
            }
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
                return true;
            case LEVEL_EDITOR_PRESSED:
                pressState = ExitCode.TO_EDITOR;
                return true;
            case SETTINGS_PRESSED:
                currentMenuState = MenuState.SETTINGS;
                Gdx.input.setInputProcessor(stage);
                break;
            case EXIT_PRESSED:
                pressState = ExitCode.TO_EXIT;
            default:
                break;
        }
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
    public boolean touchDragged(int screenX, int screenY, int pointer) {return true;}

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
    public boolean mouseMoved(int screenX, int screenY) {
        screenY = heightY - screenY;

        if (isButtonPressed(screenX, screenY, playButton, canvas.getWidth()/2, canvas.getHeight()*0.35f, BUTTON_SCALE)) {
            hoverState = PLAY_HOVERED;
        } else if (isButtonPressed(screenX, screenY, settingsButton, 0.92f * canvas.getWidth(), 0.9f * canvas.getHeight(), BUTTON_SCALE)) {
            hoverState = SETTINGS_HOVERED;
        } else if (isButtonPressed(screenX, screenY, exitButton, 0.08f * canvas.getWidth(), 0.9f * canvas.getHeight(), BUTTON_SCALE)) {
            hoverState = EXIT_HOVERED;
        } else {
            hoverState = NO_BUTTON_HOVERED;
        }
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
