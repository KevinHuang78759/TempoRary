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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.temporary.entity.Particle;


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

	SoundController<Integer> s;

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

	private SoundController<String> introSFX;

	private boolean saidThree;
	private boolean saidTwo;
	private boolean saidOne;
	private Color mask = new Color();

	private FilmStrip winBackground;

	private FilmStrip loseBackground;
	/** The font for giving messages to the player */
	private BitmapFont displayFont;

	private BitmapFont lucidaFont;

	/** Values used for resizing */
	private static int STANDARD_WIDTH = 1200;
	private static int STANDARD_HEIGHT = 800;
	private float scale = 1.0f;
	private int centerX = STANDARD_WIDTH/2;
	private int centerY = STANDARD_HEIGHT/2;


	/** Button textures */
	private Texture resumeButton;
	private Texture restartButton;
	private Texture levelAlbumCover;
	private Texture ruinShow;
	private Texture resultIcon;
	private Texture difficultyIcon;
	private Texture levelButton;
	private Texture menuButton;
	private Texture pauseBackground;
	private Texture whiteBackground;
	private Texture nextButtonWon;
	private Texture restartButtonWon;
	private Texture levelButtonWon;

	/** Play button to display when done */
	float WON_BUTTON_SCALE = 0.6f;

	private Texture goBack;

	private static float BUTTON_SCALE = 1.0f;

	private static float ALBUM_SCALE  = 0.7f;

	/** The current state of each button */
	private int pressState;

	/* PRESS STATES **/
	/** Initial button state */
	private static final int NO_BUTTON_PRESSED = 0;

	/** Reference to drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	private BitmapFont blinkerRegular;

	/** Reads input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;
	/** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;
	/** Lets the intro phase know to just resume the gameplay and not to reset the level */
	private boolean justPaused;

	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;

	/** Whether or not this player mode is still active */
	private boolean active;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** used for tracking frames during intro and then again during play and game over */
	private int ticks = 0;

	/** time in special units for measuring how far we are in the intro sequence */
	private int introTime;
	/** time in special units for measuring how far we are in the intro sequence */
	private int endTime = 0;
/** volume of the music */
    private float volume;
	/** the current level */
	private int currLevel;
	private int currDifficulty;

	/** the index of the next game */
	private int nextIdx;
	private Texture cross;
	private Texture combo;
	private Texture perfect;
	private Texture good;
	private Texture ok;
	private Texture miss;
	private Texture line;
	private Texture scoreIcon;
	AssetDirectory directory;
	FreeTypeFontGenerator generator;
	FreeTypeFontGenerator.FreeTypeFontParameter parameter;

	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas)  {
		this.canvas = canvas;
		active = false;
		saidThree = false;
		saidTwo = false;
		saidOne = false;

		// Null out all pointers, 0 out all ints, etc.
		gameState = GameState.INTRO;

		// Create the controllers.
		gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
		introSFX = new SoundController<>();
		s = new SoundController<>();
		scoreLayout = new GlyphLayout();
	}

	/**
	 * reset the GameMode state
	 */
	public void reset(){
		gameState = GameState.INTRO;
		pressState = NO_BUTTON_PRESSED;
		gameplayController.garbageCollectNoteIndicators();
		gameplayController.reset();
		ticks = 0;
		endTime = 0;
//		resetLevel();
	}

	/**
	 * Initializes the offset to use for the gameplayController
	 * @param offset the offset from CalibrationMode
	 */
	public void initializeOffset(int offset) {
		gameplayController.setOffset(offset);
	}



	public void readLevel(String levelString, AssetDirectory assetDirectory, int selectedLevel, int difficulty) {
		currLevel = selectedLevel;
		currDifficulty = difficulty;
		directory = assetDirectory;
		nextIdx = (difficulty)+(((selectedLevel+1)*3));

		JsonReader jr = new JsonReader();

		JsonValue levelData = jr.parse(Gdx.files.internal(levelString));
		gameplayController.loadLevel(levelData, directory);
		inputController = InputController.getInstance();
	}

	public void setSoundVolume(float fxVolume, float musicVolume) {
		gameplayController.level.setMusicVolume(musicVolume);
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		if(gameplayController!=null){
			gameplayController.dispose();
		}
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
		generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Regular.ttf"));
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 30;
		blinkerRegular = generator.generateFont(parameter);

		streetLevelBackground = new FilmStrip(directory.getEntry("street-background", Texture.class), 1, 1);
		winBackground = new FilmStrip(directory.getEntry("win-background", Texture.class), 1, 1);
		loseBackground = new FilmStrip(directory.getEntry("lose-background", Texture.class), 1, 1);
		displayFont = directory.getEntry("times",BitmapFont.class);
		lucidaFont = directory.getEntry("lucida", BitmapFont.class);
		gameplayController.populate(directory);
		resumeButton = directory.getEntry("resume-button", Texture.class);
		restartButton = directory.getEntry("restart-button", Texture.class);
		levelButton = directory.getEntry("level-select-button", Texture.class);

		resultIcon =  directory.getEntry("result", Texture.class);
		nextButtonWon = directory.getEntry("win-lose-next", Texture.class);
		restartButtonWon = directory.getEntry("win-lose-restart", Texture.class);
		levelButtonWon = directory.getEntry("win-lose-select", Texture.class);
		cross = directory.getEntry("x", Texture.class);
		ruinShow = directory.getEntry("ruin-show",Texture.class);


		menuButton = directory.getEntry("menu-button", Texture.class);
		pauseBackground = directory.getEntry("pause-background", Texture.class);

		whiteBackground = directory.getEntry("white-background", Texture.class);
		Texture albumCovers[] = LevelSelect.getAlbumCovers();
		levelAlbumCover = albumCovers[currLevel];
		difficultyIcon = directory.getEntry(matchDifficulty(currDifficulty), Texture.class);

		goBack = directory.getEntry("go-back", Texture.class);
		combo = directory.getEntry("combo", Texture.class);
		perfect = directory.getEntry("perfect", Texture.class);
		good=directory.getEntry("good", Texture.class);
		ok=directory.getEntry("ok", Texture.class);
		miss=directory.getEntry("miss", Texture.class);
		line=directory.getEntry("line", Texture.class);
		scoreIcon = directory.getEntry("score", Texture.class);
		introMask = directory.getEntry("white-background", Texture.class);
		introThree = directory.getEntry("intro-3", Texture.class);
		introTwo = directory.getEntry("intro-2", Texture.class);
		introOne = directory.getEntry("intro-1", Texture.class);
		introGo = directory.getEntry("intro-go", Texture.class);
		introSFX.addSound("one", "sound/1.mp3");
		introSFX.addSound("two", "sound/2.mp3");
		introSFX.addSound("three", "sound/3.mp3");
		introSFX.addSound("go", "sound/go.mp3");
		s.addSound(0, "sound/click.ogg");
	}

	private String matchDifficulty(int diff) {
		if (diff == 1){
			return "easy";
		} else if (diff ==2) {
			return "medium";
		} else if (diff==3) {
			return "hard";
		} else{
			System.out.println("difficulty not selected");
			return "easy";
		}
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
		inputController.readInput(gameplayController.NUM_LANES);

		boolean didInput = inputController.didMouseLift();

		// Test whether to reset the game.
		switch (gameState) {
			case INTRO:
				gameplayController.recieveInput(inputController);
				// wait a few frames before starting
				if (ticks == 0) {
					gameplayController.start();
					gameplayController.update(0, 0);
					saidThree = false;
					saidTwo = false;
					saidOne = false;
				}
				introTime = gameplayController.updateIntro(ticks);
				ticks++;
				if (!justPaused) {
					gameplayController.reactToAction();
					gameplayController.update(0, ticks);
				}
				if (introTime >= 0 && !saidThree){
					introSFX.playSound("three", 0.3f);
					saidThree = true;
				}
				if (introTime >= 100 && !saidTwo){
					introSFX.playSound("two", 0.3f);
					saidTwo = true;
				}
				if (introTime >= 200 && !saidOne){
					introSFX.playSound("one", 0.3f);
					saidOne = true;
				}
				if (introTime >= 300) {
					introSFX.playSound("go", 0.3f);
					introTime = 0;
					ticks = 0;
					justPaused = false;
					gameState = GameState.PLAY;
					gameplayController.level.startmusic();
				}
				break;
			case WON:
				if (didInput) {
					int screenX = (int) inputController.getMouseX();
					int screenY = (int) inputController.getMouseY();
					screenY = canvas.getHeight() - screenY;
					Vector2 goBackCoords = new Vector2(centerX*0.2f, 1.8f*centerY);
					boolean didGoBack = (isButtonPressed(screenX, screenY, goBack, goBackCoords, WON_BUTTON_SCALE));

					Vector2 restartWonCoords= new Vector2(1.25f*centerX, centerY*0.3f);
					boolean didRestartWon = (isButtonPressed(screenX, screenY, restartButtonWon, restartWonCoords, WON_BUTTON_SCALE));

					Vector2 levelWonCoords= new Vector2(1.5f*centerX, centerY*0.3f);
					boolean didLevel = (isButtonPressed(screenX, screenY, levelButtonWon, levelWonCoords, WON_BUTTON_SCALE));

					Vector2 nextWonCoords = new Vector2(1.75f*centerX, centerY*0.3f);
					boolean didNext = (isButtonPressed(screenX, screenY, nextButtonWon, nextWonCoords, WON_BUTTON_SCALE));

					if (didGoBack) {
						s.playSound(0, 0.3f);
						pressState = ExitCode.TO_LEVEL;
					}
					if (didRestartWon) {
						s.playSound(0, 0.3f);
						pressState = ExitCode.TO_PLAYING;

						resetLevel();
					}
					if (didLevel) {
						s.playSound(0, 0.3f);
						pressState = ExitCode.TO_LEVEL;
					}
					if ((nextIdx<LevelSelect.getnLevels()) && didNext) {
						s.playSound(0, 0.3f);

						goNextLevel();
//						reset();
					}
				}
				break;
			case OVER:
				if (ticks >= 120) {
					if (didInput) {
						int screenX = (int) inputController.getMouseX();
						int screenY = (int) inputController.getMouseY();
						screenY = canvas.getHeight() - screenY;
						Vector2 goBackCoords = new Vector2(centerX*0.2f, 1.8f*centerY);
						boolean didGoBack = (isButtonPressed(screenX, screenY, goBack, goBackCoords, WON_BUTTON_SCALE));

						Vector2 restartWonCoords= new Vector2(1.25f*centerX, centerY*0.3f);
						boolean didRestartWon = (isButtonPressed(screenX, screenY, restartButtonWon, restartWonCoords, WON_BUTTON_SCALE));

						Vector2 levelWonCoords= new Vector2(1.5f*centerX, centerY*0.3f);
						boolean didLevel = (isButtonPressed(screenX, screenY, levelButtonWon, levelWonCoords, WON_BUTTON_SCALE));

						Vector2 nextWonCoords = new Vector2(1.75f*centerX, centerY*0.3f);
						boolean didNext = (isButtonPressed(screenX, screenY, nextButtonWon, nextWonCoords, WON_BUTTON_SCALE));

						if (didGoBack) {
							s.playSound(0, 0.3f);
							pressState = ExitCode.TO_LEVEL;
						}

						if (didRestartWon) {
							s.playSound(0, 0.3f);
							resetLevel();
						}

						if (didLevel) {
							s.playSound(0, 0.3f);
							pressState = ExitCode.TO_LEVEL;
						}
					}
					if (inputController.didReset()) {
						s.playSound(0, 0.3f);
						resetLevel();
					}
				} else {
					play(delta);
					ticks++;
				}
				break;
			case PLAY:
				gameplayController.recieveInput(inputController);
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
					boolean didResume = (isButtonPressed(screenX, screenY, resumeButton, centerX, 1.325f*centerY, BUTTON_SCALE * scale));
					boolean didRestart = (isButtonPressed(screenX, screenY, restartButton, centerX, 1.1075f*centerY, BUTTON_SCALE * scale));
					boolean didLevel = (isButtonPressed(screenX, screenY, levelButton, centerX, 0.8925f*centerY, BUTTON_SCALE * scale));
					boolean didMenu = (isButtonPressed(screenX, screenY, menuButton, centerX, 0.675f*centerY, BUTTON_SCALE * scale));
					if (didLevel) {
						s.playSound(0, 0.3f);
						pressState = ExitCode.TO_LEVEL;
					} else if (didMenu) {
						s.playSound(0, 0.3f);
						pressState = ExitCode.TO_MENU;
					} else if (didResume) {
						s.playSound(0, 0.3f);
						ticks = 60;
						saidThree = false;
						saidTwo = false;
						saidOne = false;
						justPaused = true;
						gameState = GameState.INTRO;
					} else if (didRestart) {
						s.playSound(0, 0.3f);
						resetLevel();
					}
				}
				break;
			default:
				break;
		}
	}

	private GlyphLayout scoreLayout;
	private float scoreScale = 1f;

	public void setScoreScale(float heightConfine){
		String disp = "Score 0000000";
		scoreLayout.setText(blinkerRegular, disp);
		scoreScale *= heightConfine/scoreLayout.height;
		blinkerRegular.getData().setScale(scoreScale);
	}

	private void goNextLevel(){
		// load in album cover
		currLevel ++; //level is song
		Texture allAlbums[] = LevelSelect.getAlbumCovers();
		levelAlbumCover = allAlbums[currLevel];

		//load in next json
		String levelNames[]=LevelSelect.getAllLevels();
		LevelSelect.setSelectedJson(levelNames[nextIdx]);

		readLevel(levelNames[nextIdx], directory, currLevel, currDifficulty);
		pressState = ExitCode.TO_PLAYING;
		gameState = GameState.PLAY;
	}

	/**
	 * All the logic to reset a level
	 */
	private void resetLevel() {
		gameplayController.reset();
		gameplayController.reloadLevel();
		ticks = 0;
		endTime = 0;
		gameState = GameState.INTRO;
	}

	/**
	 * This method processes a single step in the game loop.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	protected void play(float delta) {
		// Update objects.

		if (gameState == GameState.PLAY) {
			gameplayController.reactToAction();
			gameplayController.update(1, ticks);

		} else {
			gameplayController.update(2, ticks);
		}

		// if we have a competency bar at 0
		if (gameplayController.hasZeroCompetency() && gameState != GameState.OVER) {
			ticks = 0;
			gameState = GameState.OVER;
			gameplayController.level.stopMusic();
		}

		// in the future, we should prob move this else where.
		// so that it is checked near the end of the game.
		if (gameplayController.checkWinCon()){
			endTime++;
			SaveManager.getInstance().saveGame(gameplayController.level.getLevelName(), gameplayController.sb.getScore());
			if (endTime < 150) {
				gameplayController.level.setMusicVolume(volume*(1- ((float) endTime / 150)));
			}
			if (endTime == 150) {
				gameplayController.level.stopMusic();
			}
			if (endTime >= 180) {
				gameState = GameState.WON;
				endTime = 0;
			}
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
		if (gameState == GameState.OVER) {
			if (ticks >= 120) {
				drawLose();
			}
		}
		if (gameState == GameState.WON || (gameState == GameState.PLAY && endTime >= 150) ) {
			drawWin();
		} else if (gameState == GameState.PLAY || gameState == GameState.INTRO || gameState == GameState.PAUSE || (gameState == GameState.OVER && ticks < 120)){
//			Draw everything in the current level
			gameplayController.level.drawEverything(canvas,
					gameplayController.activeBandMember, gameplayController.goalBandMember,
					inputController.triggerPress, inputController.didSwitch(),
					gameplayController.inBetweenWidth/5f);

			// Draw the particles on top
			for (Particle o : gameplayController.getParticles()) {
				o.draw(canvas);
			}

			for (Particle o : gameplayController.getNoteIndicatorParticles()){
				o.draw(canvas);
			}

			// draw the scoreboard
			gameplayController.sb.displayScoreBoard(canvas);
			// draw pause menu UI if paused
			if (gameState == GameState.PAUSE) {
				//Draw the buttons for the pause menu
				Color color = new Color(1f, 1f, 1f, 0.65f);
				canvas.draw(whiteBackground, color, 0, 0, 0, 0, 0, 1f, 1f);
				canvas.draw(pauseBackground, Color.WHITE, pauseBackground.getWidth() / 2, pauseBackground.getHeight() / 2,
						centerX, centerY, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
				canvas.draw(resumeButton, Color.WHITE, resumeButton.getWidth() / 2, resumeButton.getHeight() / 2,
						centerX, 1.325f*centerY, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
				canvas.draw(restartButton, Color.WHITE, restartButton.getWidth() / 2, restartButton.getHeight() / 2,
						centerX, 1.1075f*centerY, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
				canvas.draw(levelButton, Color.WHITE, levelButton.getWidth() / 2, levelButton.getHeight() / 2,
						centerX, 0.8925f*centerY, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
				canvas.draw(menuButton, Color.WHITE, menuButton.getWidth() / 2, menuButton.getHeight() / 2,
						centerX, 0.675f*centerY, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
			}
			float scl;
			float lerpFactor;
			// draw the countdown
			if (gameState == GameState.INTRO) {
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
					if (!justPaused) {
						mask.set(0.0f, 0.0f, 0.0f, 1f);
						if (ticks >= 30 && ticks <= 90) {
							mask.set(0.0f, 0.0f, 0.0f, (1f - ((float) ticks - 30) / 60f));
						}
						if (ticks > 90) {
							mask.set(0.0f, 0.0f, 0.0f, 0.0f);
						}
						canvas.draw(introMask, mask, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, 0, 0, 3, 3);
					}
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
			if (gameState == GameState.PLAY && endTime >= 120) {
				if (endTime <= 150) {
					mask.set(1f, 1f, 1f, (((float)(endTime-120))/30f));
					canvas.draw(introMask, mask, introMask.getWidth()/2, introMask.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, 3, 3);
				} else {
					canvas.draw(introMask, Color.WHITE, introMask.getWidth()/2, introMask.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, 3, 3);
				}
			}
			if ((gameState == GameState.OVER && ticks < 120)){
				//gray lose filter
				if (ticks < 20){
					mask.set(0.5f, 0.4f, 0.6f, 0.7f*((float) ticks)/20f);
				} else {
					mask.set(0.5f, 0.4f, 0.6f, 0.7f);
				}
				if (ticks > 110) {
					mask.lerp(0.0f, 0.0f, 0.0f, 1f, ((float) (ticks - 110)) / 10f);
				}
				canvas.draw(introMask, mask, canvas.getWidth() / 2, canvas.getHeight() / 2, 0, 0, 0, 3, 3);
			}
		}
		canvas.end();
	}

	/**
	 * Draws win screen
	 *
	 */
	public void drawWin(){
		canvas.drawBackground(winBackground.getTexture(),0,0);
		displayFont.setColor(Color.WHITE);
		setScoreScale(30);

		// draw the next button; next should be
		long score = gameplayController.sb.getScore();
		canvas.draw(combo, Color.WHITE, combo.getWidth()/2, combo.getHeight()/2,
				centerX*0.45f, centerY*1.5f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		long maxCombo =gameplayController.sb.getMaxCombo();
		canvas.drawText(String.valueOf(maxCombo), blinkerRegular,centerX*0.45f+(scale*combo.getWidth()/2),centerY*1.53f,
				Color.WHITE);

		canvas.draw(perfect, Color.WHITE, perfect.getWidth()/2, perfect.getHeight()/2,
				centerX*0.45f, centerY*1.25f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		long nPerfect = gameplayController.getNPerfect();
		canvas.drawText(String.valueOf(nPerfect), blinkerRegular,centerX*0.45f+(scale*combo.getWidth()/2),centerY*1.28f,
				Color.WHITE);

		canvas.draw(good, Color.WHITE, good.getWidth()/2, good.getHeight()/2,
				centerX*0.45f, centerY, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		long nGood = gameplayController.getNGood();
		canvas.drawText(String.valueOf(nGood), blinkerRegular,centerX*0.45f+(scale*combo.getWidth()/2),centerY*1.03f,
				Color.WHITE);

		canvas.draw(ok, Color.WHITE, ok.getWidth()/2, ok.getHeight()/2,
				centerX*0.45f, centerY*0.75f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		long nOk =gameplayController.getNOk();
		canvas.drawText(String.valueOf(nOk), blinkerRegular,centerX*0.45f+(scale*combo.getWidth()/2),centerY*0.78f,
				Color.WHITE);

		canvas.draw(miss, Color.WHITE, miss.getWidth()/2, miss.getHeight()/2,
				centerX*0.45f, centerY*0.5f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		long nMiss = gameplayController.getNOk();
		canvas.drawText(String.valueOf(nMiss), blinkerRegular,centerX*0.45f+(scale*combo.getWidth()/2), centerY*0.53f,
				Color.WHITE);

		canvas.draw(line, Color.WHITE, line.getWidth()/2, line.getHeight()/2,
				centerX*0.6f, centerY*0.375f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);

		canvas.draw(scoreIcon, Color.WHITE, scoreIcon.getWidth()/2, scoreIcon.getHeight()/2,
				centerX*0.45f, centerY*0.25f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		canvas.drawText(String.valueOf(score), blinkerRegular,centerX*0.45f+(scale*combo.getWidth()/2),centerY*0.28f,
				Color.WHITE);

		drawNextRetryLevel();

		if (nextIdx<=LevelSelect.getnLevels()){
			canvas.draw(nextButtonWon, Color.WHITE, nextButtonWon.getWidth()/2, nextButtonWon.getHeight()/2,
					1.75f*centerX, centerY*0.3f, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		}

		Texture letterGrade = gameplayController.sb.getLetterGrade();
		canvas.draw(letterGrade, Color.WHITE, letterGrade.getWidth()/2, letterGrade.getHeight()/2,
				centerX*1.25f, centerY*1.45f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
	}


	/**
	 * Draws lose screen
	 *
	 * */
	public void drawLose(){
		canvas.drawBackground(loseBackground.getTexture(),0,0);
		canvas.draw(ruinShow, Color.WHITE,ruinShow.getWidth()/2,ruinShow.getHeight()/2,0.5f*centerX,
				centerY, 0,WON_BUTTON_SCALE*scale,WON_BUTTON_SCALE*scale);
		drawNextRetryLevel();
		canvas.draw(cross, Color.WHITE, cross.getWidth()/2, cross.getHeight()/2,
				centerX*1.25f, centerY*1.45f, 0, WON_BUTTON_SCALE*scale,WON_BUTTON_SCALE*scale);
	}


	/**
	 * Draws next, retry, and level on the right of the win/lose screen; things in common for win/lose screen.
	 *
	 */
	public void drawNextRetryLevel(){
		canvas.draw(resultIcon,Color.WHITE,resultIcon.getWidth()/2,resultIcon.getHeight()/2,centerX,
		1.8f*centerY,0,WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);

		canvas.draw(goBack, Color.WHITE, goBack.getWidth()/2, goBack.getHeight()/2,
				centerX*0.2f, 1.8f*centerY, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);

		canvas.draw(restartButtonWon, Color.WHITE, restartButtonWon.getWidth()/2, restartButtonWon.getHeight()/2,
				1.25f*centerX, centerY*0.3f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);
		canvas.draw(levelButtonWon, Color.WHITE, levelButtonWon.getWidth()/2, levelButtonWon.getHeight()/2,
				1.5f*centerX, centerY*0.3f, 0, WON_BUTTON_SCALE*scale, WON_BUTTON_SCALE*scale);

		canvas.draw(levelAlbumCover,Color.WHITE, levelAlbumCover.getWidth()/2,levelAlbumCover.getHeight()/2,
				1.5f*centerX,centerY,0,ALBUM_SCALE*scale,ALBUM_SCALE*scale);

		canvas.draw(difficultyIcon, Color.WHITE, difficultyIcon.getWidth()/2,difficultyIcon.getHeight()/2,
				centerX*1.78f,centerY*0.67f,0,WON_BUTTON_SCALE*scale,WON_BUTTON_SCALE*scale);
	}

	/**
	 * Returns true if all assets are loaded and the player presses on a button
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return pressState == ExitCode.TO_MENU || pressState == ExitCode.TO_LEVEL || pressState == ExitCode.TO_PLAYING;
	}


	/**
	 * Checks to see if the location clicked at `screenX`, `screenY` are within the bounds of the given button
	 * `buttonTexture` and `buttonCoords` should refer to the appropriate button parameters
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param buttonTexture the specified button texture
	 * @param x the x-coordinate of the button
	 * @param y the y-coordinate of the button
	 * @param scale the current scale
	 * @return whether the button specified was pressed
	 */
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
	 * Checks to see if the location clicked at `screenX`, `screenY` are within the bounds of the given button
	 * `buttonTexture` and `buttonCoords` should refer to the appropriate button parameters
	 *
	 * @param screenX the x-coordinate of the mouse on the screen
	 * @param screenY the y-coordinate of the mouse on the screen
	 * @param buttonTexture the specified button texture
	 * @param buttonCoords the specified button coordinates as a Vector2 object
	 * @return whether the button specified was pressed
	 */
	public boolean isButtonPressed(int screenX, int screenY, Texture buttonTexture, Vector2 buttonCoords, float scale) {
		// buttons are rectangles
		// buttonCoords hold the center of the rectangle, buttonTexture has the width and height
		// get half the x length of the button portrayed
		float xRadius = scale * buttonTexture.getWidth()/2.0f;
		boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

		// get half the y length of the button portrayed
		float yRadius = scale * buttonTexture.getHeight()/2.0f;
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
	@Override
	public void resize(int width, int height) {
		gameplayController.resize(Math.max(250,width), Math.max(200,height));
		// Compute the drawing scale
		float sx = ((float)width)/STANDARD_WIDTH;
		float sy = ((float)height)/STANDARD_HEIGHT;
		scale = Math.min(sx, sy);
		parameter.size = Math.round(30*(1+scale));
		blinkerRegular = generator.generateFont(parameter);

		centerY = height/2;
		centerX = width/2;
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
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
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