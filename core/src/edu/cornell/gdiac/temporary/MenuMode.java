package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
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
    private Texture settingsBackground;
    /** Tempo-Rary logo */
    private Texture logo;
    /** Buttons */
    private Texture playButton;
    private Texture calibrationButton;
    private Texture levelEditorButton;
    private Texture settingsButton;
    private Texture exitButton;
    private Texture settingsButtonHover;
    private Texture exitButtonHover;
    private Texture playButtonHover;

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
    private static float BAR_WIDTH_RATIO  = 0.5f;
    /** Ration of the bar height to the screen (artifact of LoadingMode) */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of each button */
    private int pressState;

    private Vector2 v;

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
    /** Pressed down button state for the level editor button */
    private static final int LEVEL_EDITOR_HOVERED = 108;
    /** Pressed down button state for the calibration button */
    private static final int SETTINGS_HOVERED = 110;
    private static final int EXIT_HOVERED = 111;

    // START SETTINGS

    /** */
    private int currBandMemberCount;

    /** Values for the volumes */
    private float musicVolume;
    private float soundFXVolume;

    private MenuState currentMenuState;

    // SCENES FOR THE SETTINGS PAGE
    private Stage stage;
    private Table mainTable;
    private Container<Table> tableContainer;
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
    private TextButton.TextButtonStyle primaryKeyStyle = new TextButton.TextButtonStyle();
    private TextButton.TextButtonStyle secondaryKeyStyle = new TextButton.TextButtonStyle();

    // Scene2D UI components
    // settings image assets
    private Texture settingsHeader;
    private Texture backButtonTexture;
    private Texture headerLine;
    private Texture primaryBox;
    private Texture secondaryBox;
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
        logo = directory.getEntry("title", Texture.class);
        background = directory.getEntry( "loading-background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        settingsBackground = directory.getEntry("settings-background", Texture.class);
        playButton = directory.getEntry("play-button", Texture.class);
        playButtonHover = directory.getEntry("play-button-active", Texture.class);
        levelEditorButton = directory.getEntry("level-editor", Texture.class);
        calibrationButton = directory.getEntry("play-old", Texture.class);
        settingsButton = directory.getEntry("settings", Texture.class);
        settingsButtonHover = directory.getEntry("settings-active", Texture.class);
        exitButton = directory.getEntry("quit-button-menu", Texture.class);
        exitButtonHover = directory.getEntry("quit-button-active-menu", Texture.class);

        // UI assets for settings
        settingsHeader = directory.getEntry("settings-header", Texture.class);
        backButtonTexture = directory.getEntry("back-arrow", Texture.class);
        headerLine = directory.getEntry("header-line", Texture.class);
        primaryBox = directory.getEntry("primary-box", Texture.class);
        secondaryBox = directory.getEntry("secondary-box", Texture.class);
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

        primaryKeyStyle.up = new TextureRegionDrawable(primaryBox);
        primaryKeyStyle.down = new TextureRegionDrawable(primaryBox);
        primaryKeyStyle.font = blinkerRegular;
        primaryKeyStyle.fontColor = fontColor;

        secondaryKeyStyle.up = new TextureRegionDrawable(secondaryBox);
        secondaryKeyStyle.down = new TextureRegionDrawable(secondaryBox);
        secondaryKeyStyle.font = blinkerRegular;
        secondaryKeyStyle.fontColor = fontColor;
    }

    // TODO: ADD A WAY TO RESET ALL SETTINGS AND CLEAR SAVE DATA
    private void addSceneElements() {
        // MAIN TABLE IS 3 COLUMNS, EACH TABLE ADDED SHOULD SPAN 3 COLUMNS

        Table headerTable = new Table();
        Table switchTable = new Table();
        final Table controlTable = new Table();
        Table volumeTable = new Table();

        // TODO: add calibration button

        final Button backButton = new Button(backButtonStyle);

        final Slider musicVolumeSlider = new Slider(0f, 1f, 0.05f, false, sliderStyle);
        musicVolumeSlider.setValue(musicVolume);

        final Slider fxVolumeSlider = new Slider(0f, 1f, 0.05f, false, sliderStyle);
        fxVolumeSlider.setValue(soundFXVolume);

        mainTable.row().padLeft(10).padBottom(10).expandX().fill();

        // back button
        headerTable.add(backButton).top().left();
        // SETTINGS header
        headerTable.add(new Image(settingsHeader)).expand();

        switchTable.add(new Label("Full Screen", labelStyle)).left().padRight(30).padBottom(5);

        final CheckBox fullscreenCheckbox = new CheckBox("", checkBoxStyle);
        switchTable.add(fullscreenCheckbox).padBottom(5);
        // TODO: ADD BACK SPACEBAR MODE WHEN IT'S IMPLEMENTED
//        switchTable.row();
//        switchTable.add(new Label("Spacebar Mode", labelStyle)).left().padRight(30);
//        switchTable.add(new CheckBox("", checkBoxStyle));

        headerTable.add(switchTable);

        mainTable.add(headerTable).expandX().colspan(3);

        mainTable.row().padBottom(10);

        TextButton.TextButtonStyle calibrationButtonStyle = new TextButton.TextButtonStyle();
        calibrationButtonStyle.font = blinkerSemiBold;
        calibrationButtonStyle.fontColor = fontColor;
        Button calibButton = new TextButton("CALIBRATION", calibrationButtonStyle);
        mainTable.add(calibButton).colspan(3);

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
            // hide for now because mouse clicking for keybindings is kind of tough
//            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
//                System.out.println(button);
//                dialog.hide(null);
//                return false;
//            }
//
//            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
//            }

            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    dialog.hide(null);
                } else {
                    if (!switchChanged) {
                        InputController.getInstance().setKeybinding(laneChange, keycode, changeMain);
                    } else {
                        InputController.getInstance().setKeybinding(currBandMemberCount - 1, laneChange, keycode, changeMain);
                    }
                    controlTable.clear();
                    regenerateControlTable(controlTable);
                    dialog.hide(null);
                }
                return true;
            }

            public boolean keyUp (InputEvent event, int keycode) {
                return true;
            }
        });

        // LISTENERS
        calibButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                pressState = ExitCode.TO_CALIBRATION;
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                SaveManager.getInstance().saveSettings(InputController.triggerBindingsMain, InputController.switchesBindingsMain, musicVolume, soundFXVolume);
                switchToHome();
            }
        });

        fullscreenCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
                // TODO: cache previous size before windowed mode
                if (!fullscreenCheckbox.isChecked()) {
                    Gdx.graphics.setWindowedMode(prevWidth, prevHeight);
                } else {
                    prevWidth = canvas.getWidth();
                    prevHeight = canvas.getHeight();
                    Gdx.graphics.setFullscreenMode(currentMode);
                }
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

    private void regenerateControlTable(final Table table) {
        // controls for note hits
        table.add(new Label("Note Hits", labelStyle)).padBottom(30).expandX();
        String[] currentTriggerKeybinds = InputController.triggerKeyBinds(true);
        String[] currentTriggerKeybindsAlt = InputController.triggerKeyBinds(false);

        for (int i = 0; i < 4; i++) {
            table.add(getControlWidget(i + 1, currentTriggerKeybinds[i], currentTriggerKeybindsAlt[i], fishOutline)).padBottom(30);
        }

        table.row();

        // controls for switching
        VerticalGroup membersLabel = new VerticalGroup();
        membersLabel.addActor(new Label("Members", labelStyle));

        final SelectBox<Integer> dropdown = new SelectBox<>(dropdownStyle);
        dropdown.setItems(2, 3, 4);
        dropdown.setSelected(currBandMemberCount);
        dropdown.setAlignment(Align.center);
        dropdown.getList().setAlignment(Align.center);
        dropdown.getList().setSelected(currBandMemberCount);
        membersLabel.addActor(dropdown);

        table.add(membersLabel).expandX().fillX();
        String[] currentSwitchKeybinds = InputController.switchKeyBinds(currBandMemberCount - 1, true);
        String[] currentSwitchKeybindsAlt = InputController.switchKeyBinds(currBandMemberCount - 1, false);

        for (int i = 0; i < currBandMemberCount; i++) {
            table.add(getControlWidget(i + 1, currentSwitchKeybinds[i], currentSwitchKeybindsAlt[i], catOutline)).expandX();
        }
        for (int i = 0; i < 4 - currBandMemberCount; i++) {
            table.add(new Table()).expandX();
        }

        dropdown.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                currBandMemberCount = dropdown.getSelected();
                table.clear();
                regenerateControlTable(table);
            }
        });
    }

    private Table getControlWidget(final int nth, final String primaryKeybind, String secondaryKeybind, final Texture outlineImage) {
        Table tempTable = new Table();
        tempTable.add(new Label("" + nth + "", boldLabelStyle));

        tempTable.row();

        Image image = new Image(outlineImage);
        tempTable.add(image);

        VerticalGroup stack = new VerticalGroup();
        stack.space(10);

        final Button primaryKeyButton = keyImageMap.get(primaryKeybind) != null ?
                new ImageButton(new TextureRegionDrawable(keyImageMap.get(primaryKeybind))) : new TextButton(primaryKeybind, primaryKeyStyle);
        primaryKeyButton.getStyle().up = new TextureRegionDrawable(primaryBox);
        primaryKeyButton.getStyle().down = new TextureRegionDrawable(primaryBox);

        final Button secondaryKeyButton = keyImageMap.get(secondaryKeybind) != null ?
                new ImageButton(new TextureRegionDrawable(keyImageMap.get(secondaryKeybind))) : new TextButton(secondaryKeybind, secondaryKeyStyle);
        secondaryKeyButton.getStyle().up = new TextureRegionDrawable(secondaryBox);
        secondaryKeyButton.getStyle().down = new TextureRegionDrawable(secondaryBox);

        ChangeListener buttonChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                laneChange = nth - 1;
                switchChanged = outlineImage == catOutline;
                changeMain = actor == primaryKeyButton;
                dialog.show(stage,null);
                dialog.setPosition((float)Math.round((stage.getWidth() - dialog.getWidth()) / 2.0f), (float)Math.round((stage.getHeight() - dialog.getHeight()) / 2.0f));
            }
        };

        stack.addActor(primaryKeyButton);
        stack.addActor(secondaryKeyButton);

        primaryKeyButton.addListener(buttonChangeListener);
        secondaryKeyButton.addListener(buttonChangeListener);

        tempTable.add(stack);

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
    }

    /**
     * Creates a MenuMode
     *
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        this.canvas = canvas;
        stage = new Stage(new ExtendViewport(1200, 800));
        mainTable = new Table();
        tableContainer = new Container<>();

        float sw = canvas.getWidth();
        float sh = canvas.getHeight();

        float cw = sw * 0.90f;
        float ch = sh * 0.90f;

        // scene 2D UI
        tableContainer.setSize(cw, ch);
        tableContainer.setPosition((sw - cw) / 2.0f, (sh - ch) / 2.0f);
        tableContainer.fill();
        tableContainer.setActor(mainTable);
        stage.addActor(tableContainer);

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
        currBandMemberCount = 4;
        musicVolume = SaveManager.getInstance().getMusicVolume();
        soundFXVolume = SaveManager.getInstance().getFXVolume();
        active = false;
        v = new Vector2();
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
        canvas.draw(background, 0, 0, canvas.getWidth(), canvas.getHeight());

//        canvas.draw(logo, Color.WHITE, logo.getWidth()/2, logo.getHeight()/2,
//                logo.getWidth()/2+50, centerY+300, 0,scale, scale);

        if (pressState == PLAY_PRESSED || pressState == PLAY_HOVERED) {
            canvas.draw(playButtonHover, Color.WHITE, playButtonHover.getWidth() / 2, playButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.75f * canvas.getHeight(), 0, BUTTON_SCALE, BUTTON_SCALE);
        } else {
            canvas.draw(playButton, Color.WHITE, playButton.getWidth() / 2, playButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.75f * canvas.getHeight(), 0, BUTTON_SCALE, BUTTON_SCALE);
        }

        if (pressState == SETTINGS_PRESSED || pressState == SETTINGS_HOVERED) {
            canvas.draw(settingsButtonHover, Color.WHITE, settingsButtonHover.getWidth() / 2, settingsButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.6f * canvas.getHeight(), 0, BUTTON_SCALE, BUTTON_SCALE);
        } else {
            canvas.draw(settingsButton, Color.WHITE, settingsButton.getWidth() / 2, settingsButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.6f * canvas.getHeight(), 0, BUTTON_SCALE, BUTTON_SCALE);
        }

        if (pressState == EXIT_PRESSED || pressState == EXIT_HOVERED) {
            canvas.draw(exitButtonHover, Color.WHITE, exitButtonHover.getWidth() / 2, exitButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.45f * canvas.getHeight(), 0, BUTTON_SCALE, BUTTON_SCALE);
        } else {
            canvas.draw(exitButton, Color.WHITE, exitButton.getWidth() / 2, exitButton.getHeight() / 2,
                    canvas.getWidth() / 2, 0.45f * canvas.getHeight(), 0, BUTTON_SCALE, BUTTON_SCALE);
        }

        Color levelEditorButtonTint = (pressState == LEVEL_EDITOR_HOVERED || pressState == LEVEL_EDITOR_PRESSED ? Color.GREEN: Color.WHITE);
        canvas.draw(levelEditorButton, levelEditorButtonTint, levelEditorButton.getWidth()/2, levelEditorButton.getHeight()/2,
                canvas.getWidth() / 2, 0.25f * canvas.getHeight(), 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

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

        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;

        stage.getViewport().update(width, height, true);
        float cw = (float) width * 0.90f;
        float ch = (float) height * 0.90f;
        tableContainer.setPosition(((float) width - cw) / 2.0f, ((float) height - ch) / 2.0f);
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
        float xRadius = scale * buttonTexture.getWidth()/2.0f;
        boolean xInBounds = x - xRadius <= screenX && x + xRadius >= screenX;

        // get half the y length of the button portrayed
        float yRadius = scale * buttonTexture.getHeight()/2.0f;
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
            if (isButtonPressed(screenX, screenY, playButton, canvas.getWidth()/2, canvas.getHeight()*0.75f, BUTTON_SCALE)) {
                pressState = PLAY_PRESSED;
            }
            if (isButtonPressed(screenX, screenY, settingsButton, canvas.getWidth()/2, canvas.getHeight()*0.6f, BUTTON_SCALE)) {
                pressState = SETTINGS_PRESSED;
            }
            if (isButtonPressed(screenX, screenY, exitButton, canvas.getWidth()/2, canvas.getHeight()*0.45f, BUTTON_SCALE)) {
                pressState = EXIT_PRESSED;
            }
            if (isButtonPressed(screenX, screenY, levelEditorButton, canvas.getWidth()/2, canvas.getHeight()*0.25f, BUTTON_SCALE * scale)) {
                pressState = LEVEL_EDITOR_PRESSED;
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
                pressState = NO_BUTTON_PRESSED;
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
        if (isButtonPressed(screenX, screenY, playButton, canvas.getWidth()/2, canvas.getHeight()*0.75f, BUTTON_SCALE)) {
            pressState = PLAY_HOVERED;
        } else if (isButtonPressed(screenX, screenY, settingsButton, canvas.getWidth()/2, canvas.getHeight()*0.6f, BUTTON_SCALE)) {
            pressState = SETTINGS_HOVERED;
        } else if (isButtonPressed(screenX, screenY, exitButton, canvas.getWidth()/2, canvas.getHeight()*0.45f, BUTTON_SCALE)) {
            pressState = EXIT_HOVERED;
        } else if (isButtonPressed(screenX, screenY, levelEditorButton, canvas.getWidth()/2, canvas.getHeight()*0.25f, BUTTON_SCALE * scale)) {
            pressState = LEVEL_EDITOR_HOVERED;
        } else {
            pressState = NO_BUTTON_HOVERED;
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
