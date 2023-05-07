/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Particle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements Screen {

	/**
	 * Track the current state of the game for the update loop.
	 */
	public enum GameState {
		/** Before the game has started */
		INTRO,
		/** While we are playing the game */
		PLAY,
		/** When the ships is dead (but shells still work) */
		OVER,
		/** When there are no more notes and competency bar is not zero */
		WON,
		/** When the game is paused */
		PAUSE
	}

	// Loaded assets
	// background images
	private FilmStrip streetLevelBackground;

	/** Textures for the intro sequence of the level*/
	private Texture introMask;
	private Texture introThree;
	private Texture introTwo;
	private Texture introOne;
	private Texture introGo;

	private SoundController<String> introOneSFX;
	private SoundController<String> introTwoSFX;
	private SoundController<String> introThreeSFX;
	private SoundController<String> introGoSFX;

	private boolean saidThree;
	private boolean saidTwo;
	private boolean saidOne;
	private Color mask = new Color();
	/** The font for giving messages to the player */
	private BitmapFont displayFont;
	/** Button textures */
	private Texture resumeButton;
	private Texture restartButton;
	private Texture levelButton;
	private Texture menuButton;

	/* BUTTON LOCATIONS */
	/** Resume button x and y coordinates represented as a vector */
	private Vector2 resumeCoords;
	/** Restart button x and y coordinates represented as a vector */
	private Vector2 restartCoords;
	/** Level select button x and y coordinates represented as a vector */
	private Vector2 levelCoords;
	/** Main menu button x and y coordinates represented as a vector */
	private Vector2 menuCoords;

	/** Play button to display when done */
	private Texture playButton;

	private static float BUTTON_SCALE  = 0.25f;

	/// CONSTANTS
	/** Offset for the game over message on the screen */
	private static final float GAME_OVER_OFFSET = 40.0f;
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
	/** Ration of the bar height to the screen (artifact of LoadingMode) */
	private static float BAR_HEIGHT_RATIO = 0.25f;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;

	/** The current state of each button */
	private int pressState;

	/* PRESS STATES **/
	/** Initial button state */
	private static final int NO_BUTTON_PRESSED = 0;
	/** Pressed down button state for the resume button */
	private static final int RESUME_PRESSED = 101;
	/** Pressed down button state for the restart button */
	private static final int RESTART_PRESSED = 102;
	/** Pressed down button state for the level select button */
	private static final int LEVEL_PRESSED = 103;
	/** Pressed down button state for the main menu button */
	private static final int MENU_PRESSED = 104;

	/** Reference to drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	/** Reads input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;
	/** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;
	/** Asset directory for level loading */
	private AssetDirectory assetDirectory;
	/** Lets the intro phase know to just resume the gameplay and not to reset the level */
	private boolean justPaused;

	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;

	/** Whether or not this player mode is still active */
	private boolean active;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** used for tracking frames during intro and then again during play */
	private int ticks = 0;

	/** time in special units for measuring how far we are in the intro sequence */
	private int introTime;

	/** Play button x and y coordinates represented as a vector */
	private Vector2 playButtonCoords;

	/** the current level */
	private int currLevel;

	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas)  {
		this.canvas = canvas;
		active = false;
		playButton = null;
		saidThree = false;
		saidTwo = false;
		saidOne = false;

		// Null out all pointers, 0 out all ints, etc.
		gameState = GameState.INTRO;

		// Create the controllers.
		gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
		introOneSFX = new SoundController<>();
		introTwoSFX = new SoundController<>();
		introThreeSFX = new SoundController<>();
		introGoSFX = new SoundController<>();
	}

	/**
	 * reset the GameMode state
	 */
	public void reset(){
		gameState = GameState.INTRO;
		pressState = NO_BUTTON_PRESSED;
	}

	/**
	 * Initializes the offset to use for the gameplayController
	 * @param offset the offset from CalibrationMode
	 */
	public void initializeOffset(int offset) {
		gameplayController.setOffset(offset);
	}

	public void readLevel(String level, AssetDirectory directory) {
		JsonReader jr = new JsonReader();
		SetCurrLevel("1");
		JsonValue levelData = jr.parse(Gdx.files.internal(level));
		System.out.println("level read");
		gameplayController.loadLevel(levelData, directory);
		if (gameplayController.NUM_LANES == 2) {
			inputController = new InputController(new int[]{1, 2},  new int[gameplayController.lpl]);
		} else {
			inputController = new InputController(gameplayController.NUM_LANES, gameplayController.lpl);
		}
	}

	public void setSoundVolume(float fxVolume, float musicVolume) {
		gameplayController.setFxVolume(fxVolume);
		gameplayController.level.setMusicVolume(musicVolume);
	}

	/**
	 * Get the current level from LevelSelect
	 * Note this function
	 */
	public void SetCurrLevel(String level) {
//		TODO: need to merge with level screen
//		currLevel is some versions of levelscreen.getSelectedJson();
		currLevel = Integer.parseInt(level);
	}


	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		gameplayController.dispose();
		gameplayController = null;
		canvas = null;
	}

	/**
	 * Populates this mode from the given the directory.
	 *
	 * The asset directory is a dictionary that maps string keys to assets.
	 * Assets can include images, sounds, and fonts (and more). This
	 * method delegates to the gameplay controller
	 *
	 * @param directory     Reference to the asset directory.
	 */
	public void populate(AssetDirectory directory) {
		assetDirectory = directory;
		streetLevelBackground = new FilmStrip(directory.getEntry("street-background", Texture.class), 1, 1);
		displayFont = directory.getEntry("times",BitmapFont.class);
		gameplayController.populate(directory);
		playButton = directory.getEntry("restart-button",Texture.class);
		playButtonCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/2 - 50);
		inputController.setEditorProcessor();
		resumeButton = directory.getEntry("resume-button", Texture.class);
		restartButton = directory.getEntry("restart-button", Texture.class);
		levelButton = directory.getEntry("level-select-button", Texture.class);
		menuButton = directory.getEntry("quit-button", Texture.class);
		introMask = directory.getEntry("white-background", Texture.class);
		introThree = directory.getEntry("intro-3", Texture.class);
		introTwo = directory.getEntry("intro-2", Texture.class);
		introOne = directory.getEntry("intro-1", Texture.class);
		introGo = directory.getEntry("intro-go", Texture.class);
		introOneSFX.addSound("one", "sound/1.mp3");
		introTwoSFX.addSound("two", "sound/2.mp3");
		introThreeSFX.addSound("three", "sound/3.mp3");
		introGoSFX.addSound("go", "sound/go.mp3");
		resumeCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/2 + 250);
		restartCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/2 + 100);
		levelCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/2 - 100);
		menuCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/2 - 250);
	}

	/**
	 * Update the game state.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */

	private void update(float delta) {
		// Process the game input
		inputController.readInput();

		boolean didInput = inputController.didClick();

		// Test whether to reset the game.
		switch (gameState) {
			case INTRO:
				for (boolean k : inputController.getTriggers()){
					if (k) {
						gameplayController.sfx.playSound("tap", 0.2f);
					}
				}
				// wait a few frames before starting
				if (ticks == 0) {
					gameplayController.start();
					gameplayController.update();
					saidThree = false;
					saidTwo = false;
					saidOne = false;
				}
				introTime = gameplayController.updateIntro(ticks);
				ticks++;
				if (introTime >= 0 && !saidThree){
					introThreeSFX.playSound("three", 0.3f);
					saidThree = true;
				}
				if (introTime >= 100 && !saidTwo){
					introTwoSFX.playSound("two", 0.3f);
					saidTwo = true;
				}
				if (introTime >= 200 && !saidOne){
					introOneSFX.playSound("one", 0.3f);
					saidOne = true;
				}
				if (introTime >= 300) {
					introGoSFX.playSound("go", 0.3f);
					introTime = 0;
					ticks = 0;
					gameState = GameState.PLAY;
					gameplayController.level.startmusic();
				}
				break;
			case OVER:
				if (inputController.didReset()) {
					resetLevel();
				}
				break;
			case PLAY:
				if (inputController.didExit()) {
					gameplayController.level.pauseMusic();
					gameState = GameState.PAUSE;
				} else {
					play(delta);
					ticks++;
				}
				break;
			case PAUSE:
				if (didInput) {
					int screenX = (int) inputController.getMouseX();
					int screenY = (int) inputController.getMouseY();
					screenY = canvas.getHeight() - screenY;
					boolean didResume = (isButtonPressed(screenX, screenY, resumeButton, resumeCoords));
					boolean didLevel = (isButtonPressed(screenX, screenY, levelButton, levelCoords));
					boolean didMenu = (isButtonPressed(screenX, screenY, menuButton, menuCoords));
					boolean didRestart = (isButtonPressed(screenX, screenY, restartButton, restartCoords));
					if (didLevel) {
						pressState = ExitCode.TO_LEVEL;
					} else if (didMenu) {
						pressState = ExitCode.TO_MENU;
					} else if (didResume) {
						ticks = 0;
						justPaused = true;
						gameState = GameState.INTRO;
					} else if (didRestart) {
						resetLevel();
					}
				}
				break;
			case WON:
				int screenX = (int) inputController.getMouseX();
				int screenY = (int) inputController.getMouseY();
				screenY = canvas.getHeight() - screenY;
				if (didInput && isButtonPressed(screenX, screenY, playButton, playButtonCoords)) {
					resetLevel();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * All the logic to reset a level
	 */
	private void resetLevel() {
		gameplayController.reset();
		gameplayController.reloadLevel();
		ticks = 0;
		gameState = GameState.INTRO;
	}

	/**
	 * This method processes a single step in the game loop.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	protected void play(float delta) {
		// Update objects.
		gameplayController.handleActions(inputController);
		gameplayController.update();

		// if we have a competency bar at 0
		if (gameplayController.hasZeroCompetency()) {
			gameState = GameState.OVER;
			gameplayController.level.stopMusic();
		}

		// in the future, we should prob move this else where.
		// so that it is checked near the end of the game.
		if (gameplayController.checkWinCon()){
			gameState = GameState.WON;
			gameplayController.level.stopMusic();
		}

		// Clean up destroyed objects
		gameplayController.garbageCollect();
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
		// First draw the background
		// TODO: SWITCH BACKGROUND BASED ON LEVEL JSON (may need to move this to a different location)
		canvas.drawBackground(streetLevelBackground.getTexture(),0,0);
		if (gameState == GameState.OVER) {
			//Draw game over text
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Game Over!", displayFont, GAME_OVER_OFFSET+50);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Press ENTER to Restart", displayFont, 0);
		} else if (gameState == GameState.WON) {
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("You won!", displayFont, GAME_OVER_OFFSET+100);
			canvas.drawTextCentered("Final score: " + gameplayController.sb.getScore(), displayFont, GAME_OVER_OFFSET+50);
			canvas.draw(playButton, Color.WHITE, playButton.getWidth()/2, playButton.getHeight()/2,
					playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE, BUTTON_SCALE);
		} else if (gameState == GameState.PAUSE) {
			//Draw the buttons for the pause menu
			canvas.draw(resumeButton, Color.WHITE, resumeButton.getWidth()/2, resumeButton.getHeight()/2,
					resumeCoords.x, resumeCoords.y, 0, BUTTON_SCALE, BUTTON_SCALE);
			canvas.draw(restartButton, Color.WHITE, restartButton.getWidth()/2, restartButton.getHeight()/2,
					restartCoords.x, restartCoords.y, 0, BUTTON_SCALE, BUTTON_SCALE);
			canvas.draw(levelButton, Color.WHITE, levelButton.getWidth()/2, levelButton.getHeight()/2,
					levelCoords.x, levelCoords.y, 0, BUTTON_SCALE, BUTTON_SCALE);
			canvas.draw(menuButton, Color.WHITE, menuButton.getWidth()/2, menuButton.getHeight()/2,
					menuCoords.x, menuCoords.y, 0, BUTTON_SCALE, BUTTON_SCALE);
		} else{
			//Draw everything in the current level
			gameplayController.level.drawEverything(canvas,
					gameplayController.activeBandMember, gameplayController.goalBandMember,
					inputController.triggerPress, inputController.switches(),
					gameplayController.inBetweenWidth/5f);

			// Draw the particles on top
			for (Particle o : gameplayController.getParticles()) {
				o.draw(canvas);
			}

			for(BandMember bandMember : gameplayController.level.getBandMembers()){
				//Draw the band member sprite and competency bar
				bandMember.drawCharacterSprite(canvas);
				bandMember.drawHPBar(canvas);
			}

			// draw the scoreboard
			gameplayController.sb.displayScore(gameplayController.LEFTBOUND, gameplayController.TOPBOUND + gameplayController.inBetweenWidth/4f, canvas);

			// draw the countdown
			if (gameState == GameState.INTRO) {
				float scl = 1f;
				float lerpFactor = 0f;
				if (introTime%100 <= 20){
					//lerp from 0 to 1.25
					lerpFactor = ((float) (introTime%100))/20f;
					scl = 1.25f*lerpFactor;
				} else if (introTime%100 <= 30){
					//lerp from 1.25 to 1
					lerpFactor = ((float) ((introTime%100)-20))/10f;
					scl = 1.25f*(1f - lerpFactor) + 1.0f*lerpFactor;
				} else {
					scl = 1f;
				}
				if (introTime < 0) {
					mask.set(0.0f,0.0f,0.0f,1f);
					if (ticks >= 30 && ticks <= 90) {
						mask.set(0.0f, 0.0f, 0.0f, (1f - ((float) ticks-30) / 60f));
					}
					if (ticks > 90) {
						mask.set(0.0f, 0.0f, 0.0f, 0.0f);
					}
					canvas.draw(introMask, mask, canvas.getWidth()/2, canvas.getHeight()/2, 0, 0, 0, 3, 3);
				} else if (introTime < 100){
					canvas.draw(introThree, Color.WHITE, introThree.getWidth()/2, introThree.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, scl, scl);
				} else if (introTime < 200){
					canvas.draw(introTwo, Color.WHITE, introTwo.getWidth()/2, introTwo.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, scl, scl);
				} else if (introTime < 300){
					canvas.draw(introOne, Color.WHITE, introOne.getWidth()/2, introOne.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, scl, scl);
				}
			}
			if (gameState == GameState.PLAY){
				if (ticks <= 75) {
					mask.set(1.0f, 1.0f, 1.0f, 1f);
					if (ticks >= 15) {
						mask.set(1.0f, 1.0f, 1.0f, 1f - ((float) ticks - 15) / 60f);
					}
					canvas.draw(introGo, mask, introGo.getWidth() / 2, introGo.getHeight() / 2, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, 1.25f, 1.25f);
				}
			}
		}
		canvas.end();
	}

	/**
	 * Returns true if all assets are loaded and the player presses on a button
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return pressState == ExitCode.TO_MENU || pressState == ExitCode.TO_LEVEL;
	}


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
		float xRadius = BUTTON_SCALE * buttonTexture.getWidth()/2.0f;
		boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

		// get half the y length of the button portrayed
		float yRadius = BUTTON_SCALE * buttonTexture.getHeight()/2.0f;
		boolean yInBounds = buttonCoords.y - yRadius <= screenY && buttonCoords.y + yRadius >= screenY;
		return xInBounds && yInBounds;
	}

	/**
	 * Called when the Screen is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		gameplayController.resize(Math.max(250,width), Math.max(200,height));
//		// Compute the drawing scale
//		float sx = ((float)width)/STANDARD_WIDTH;
//		float sy = ((float)height)/STANDARD_HEIGHT;
//		scale = (sx < sy ? sx : sy);
//
////        this.width = (int)(BAR_WIDTH_RATIO*width);
//		centerY = (int)(BAR_HEIGHT_RATIO*height);
//		centerX = width/2;
//		heightY = height;
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw();
			if (isReady() && listener != null) {
				listener.exitScreen(this, pressState);
			}
		}
	}

	/**
	 * Called when the Screen is paused.
	 *
	 * This is usually when it's not active or visible on screen. An Application is
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
		ticks = 0;
		gameState = GameState.INTRO;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
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