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

	private Vector2 ruinShowCoords;

	private Texture resultIcon;

	private Vector2 resultIconCoords;
	private Texture difficultyIcon;

	private Vector2 difficultyIconCoords;

	private Vector2 levelAlbumCoverCoords;
	private Texture levelButton;
	private Texture menuButton;
	private Texture pauseBackground;
	private Texture whiteBackground;

	private Texture nextButtonWon;
	private Texture restartButtonWon;
	private Texture levelButtonWon;

	/* BUTTON LOCATIONS */

	/** Resume button for win/lose screen x and y coordinates represented as a vector */
	private Vector2 nextWonCoords;
	/** Restart button or win/lose screen x and y coordinates represented as a vector */
	private Vector2 restartWonCoords;
	/** Level select or win/lose screen button x and y coordinates represented as a vector */
	private Vector2 levelWonCoords;

	/** Play button to display when done */
	float WON_BUTTON_SCALE = 0.7f;

	private Texture goBack;
	private Vector2 goBackCoords;

	private static float BUTTON_SCALE = 1.0f;

	private static float ALBUM_SCALE  = 0.75f;

	/** The current state of each button */
	private int pressState;

	/* PRESS STATES **/
	/** Initial button state */
	private static final int NO_BUTTON_PRESSED = 0;

	/** Reference to drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

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


	/** the current level */
	private int currLevel;

	private int currDifficulty;

	/** the index of the next game */

	private int nextIdx;

	// TODO: REMOVE ALL INSTANCES OF VECTOR2
	private Texture cross;
	private Vector2 crossCoords;

	private Texture combo;
	private Vector2 comboCoords;
	private Texture perfect;
	private Vector2 perfectCoords;

	private Texture good;
	private Vector2 goodCoords;

	private Texture ok;
	private Vector2 okCoords;

	private Texture miss;
	private Vector2 missCoords;

	private Texture line;
	private Vector2 lineCoords;

	private Texture scoreIcon;
	private Vector2 scoreIconCoords;
	AssetDirectory directory;

	private Texture scoreA;
	private Vector2 scoreACoords;

	private Texture scoreB;
	private Vector2 scoreBCoords;

	private Texture scoreC;
	private Vector2 scoreCCoords;

	private Texture scoreS;
	private Vector2 scoreSCoords;

	String levelString;

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
	}

	/**
	 * reset the GameMode state
	 */
	public void reset(){
		gameState = GameState.INTRO;
		pressState = NO_BUTTON_PRESSED;
		gameplayController.garbageCollectNoteIndicators();
	}

	/**
	 * Initializes the offset to use for the gameplayController
	 * @param offset the offset from CalibrationMode
	 */
	public void initializeOffset(int offset) {
		gameplayController.setOffset(offset);
	}

	public void readLevel(String level, AssetDirectory assetDirectory, int selectedLevel, int difficulty) {
		levelString = level;

		currLevel = selectedLevel;
		currDifficulty = difficulty;

		directory = assetDirectory;

		int start = level.indexOf("/");
		int end = level.indexOf(".");

		JsonReader jr = new JsonReader();
//		nextIdx = Integer.parseInt(level.substring(start + 1, end))+3;
		nextIdx = currLevel++;

		JsonValue levelData = jr.parse(Gdx.files.internal(level));

//		System.out.println("level read");
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
		resultIconCoords=new Vector2(canvas.getWidth()/2,canvas.getHeight()-resultIcon.getHeight()/2);
		nextButtonWon = directory.getEntry("win-lose-next", Texture.class);
		restartButtonWon = directory.getEntry("win-lose-restart", Texture.class);
		levelButtonWon = directory.getEntry("win-lose-select", Texture.class);
		cross = directory.getEntry("x", Texture.class);
		ruinShow = directory.getEntry("ruin-show",Texture.class);


		menuButton = directory.getEntry("menu-button", Texture.class);
		pauseBackground = directory.getEntry("pause-background", Texture.class);
		whiteBackground = directory.getEntry("white-background", Texture.class);

		nextWonCoords = new Vector2(canvas.getWidth()*3/4+ nextButtonWon.getWidth()*2, canvas.getHeight()/7);
		restartWonCoords = new Vector2(canvas.getWidth()*3/4 - nextButtonWon.getWidth()*2, canvas.getHeight()/7);
		levelWonCoords = new Vector2(canvas.getWidth()*3/4, canvas.getHeight()/7);

		System.out.println("currSong:"+currLevel);
		levelAlbumCover = directory.getEntry(String.valueOf(currLevel), Texture.class);
		levelAlbumCoverCoords = new Vector2(canvas.getWidth()*3/4, canvas.getHeight()/2);

		difficultyIcon = directory.getEntry(matchDifficulty(currDifficulty), Texture.class);
		difficultyIconCoords = new Vector2(levelAlbumCoverCoords.x+(difficultyIcon.getWidth()*1.3f),
				levelAlbumCoverCoords.y-(difficultyIcon.getWidth()*1.2f));

		crossCoords = new Vector2(levelAlbumCoverCoords.x-cross.getWidth()+10, levelAlbumCoverCoords.y+(cross.getHeight())-20);

		ruinShowCoords= new Vector2(canvas.getWidth()/3,canvas.getHeight()/2);

		goBack = directory.getEntry("go-back", Texture.class);
		goBackCoords=new Vector2 (goBack.getWidth(), canvas.getHeight()-goBack.getWidth());

		combo = directory.getEntry("combo", Texture.class);
		comboCoords = new Vector2((canvas.getWidth()/7f)+(combo.getWidth()/2)-2,levelAlbumCoverCoords.y+cross.getHeight());

		perfect = directory.getEntry("perfect", Texture.class);
		perfectCoords = new Vector2((canvas.getWidth()/7f)+(perfect.getWidth()/2)-2,comboCoords.y-perfect.getHeight()*2f);

		good=directory.getEntry("good", Texture.class);
		goodCoords = new Vector2((canvas.getWidth()/7f)+(good.getWidth()/2)+3,perfectCoords.y-perfect.getHeight()*2f);

		ok=directory.getEntry("ok", Texture.class);
		okCoords =new Vector2((canvas.getWidth()/7f)+(ok.getWidth()/2)+10,goodCoords.y-perfect.getHeight()*2f);

		miss=directory.getEntry("miss", Texture.class);
		missCoords=new Vector2((canvas.getWidth()/7f)+(miss.getWidth()/2)+5,okCoords.y-perfect.getHeight()*2f);

		line=directory.getEntry("line", Texture.class);
		lineCoords=new Vector2(canvas.getWidth()/3f-30,missCoords.y-perfect.getHeight()*2f);

		scoreIcon = directory.getEntry("score", Texture.class);
		scoreIconCoords=new Vector2((canvas.getWidth()/7f)+(scoreIcon.getWidth()/2)-6,lineCoords.y-perfect.getHeight()*2f);

		scoreA = directory.getEntry("score-a", Texture.class);
		scoreB = directory.getEntry("score-b", Texture.class);
		scoreC = directory.getEntry("score-c", Texture.class);
		scoreS = directory.getEntry("score-s", Texture.class);

		scoreACoords=new Vector2(levelAlbumCoverCoords.x-scoreA.getWidth()+10, levelAlbumCoverCoords.y+(scoreA.getHeight()));
		scoreBCoords=new Vector2(levelAlbumCoverCoords.x-scoreA.getWidth()+10, levelAlbumCoverCoords.y+(scoreA.getHeight()));
		scoreCCoords=new Vector2(levelAlbumCoverCoords.x-scoreA.getWidth()+10, levelAlbumCoverCoords.y+(scoreA.getHeight()));
		scoreSCoords=new Vector2(levelAlbumCoverCoords.x-scoreA.getWidth()+10, levelAlbumCoverCoords.y+(scoreA.getHeight()));
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
			case OVER:
				if (ticks >= 120) {
					if (didInput) {
						int screenX = (int) inputController.getMouseX();
						int screenY = (int) inputController.getMouseY();
						screenY = canvas.getHeight() - screenY;
						boolean didGoBack = (isButtonPressed(screenX, screenY, goBack, goBackCoords, WON_BUTTON_SCALE));
						boolean didRestartWon = (isButtonPressed(screenX, screenY, restartButtonWon, restartWonCoords, WON_BUTTON_SCALE));
						boolean didLevel = (isButtonPressed(screenX, screenY, levelButtonWon, levelWonCoords, WON_BUTTON_SCALE));
						boolean didNext = (isButtonPressed(screenX, screenY, nextButtonWon, nextWonCoords, WON_BUTTON_SCALE));

						if (didGoBack) {
							s.playSound(0, 0.3f);
							System.out.println("pressed back");
							pressState = ExitCode.TO_MENU;
						}

						if (didRestartWon) {
							s.playSound(0, 0.3f);
							System.out.println("pressed restart");
							resetLevel();
						}

						if (didLevel) {
							s.playSound(0, 0.3f);
							System.out.println("pressed level");
							pressState = ExitCode.TO_LEVEL;
						}

						if (didNext) {
							s.playSound(0, 0.3f);
							goNextLevel();
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
			case WON:
					if (didInput) {
						int screenX = (int) inputController.getMouseX();
						int screenY = (int) inputController.getMouseY();
						screenY = canvas.getHeight() - screenY;
						boolean didGoBack = (isButtonPressed(screenX, screenY, goBack, goBackCoords, WON_BUTTON_SCALE));
						boolean didRestartWon = (isButtonPressed(screenX, screenY, restartButtonWon, restartWonCoords, WON_BUTTON_SCALE));
						boolean didLevel = (isButtonPressed(screenX, screenY, levelButtonWon, levelWonCoords, WON_BUTTON_SCALE));
						boolean didNext = (isButtonPressed(screenX, screenY, nextButtonWon, nextWonCoords, WON_BUTTON_SCALE));
						if (didGoBack) {
							s.playSound(0, 0.3f);
							pressState = ExitCode.TO_MENU;
						}
						if (didRestartWon) {
							s.playSound(0, 0.3f);
							System.out.println("pressed restart");
							pressState = ExitCode.TO_PLAYING;
							resetLevel();
						}
						if (didLevel) {
							s.playSound(0, 0.3f);
							System.out.println("pressed level");
							pressState = ExitCode.TO_LEVEL;
						}
						if (didNext) {
							s.playSound(0, 0.3f);
							goNextLevel();
						}
					}
					break;
			default:
				break;
		}
	}

	// TODO: FIX THIS
	private void goNextLevel(){
		System.out.println("next pressed:" + nextIdx);
		LevelSelect.setSelectedJson("levels/"+nextIdx+".json");
		pressState = ExitCode.TO_PLAYING;
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

			if (endTime >= 60) {
				gameplayController.level.stopMusic();
			}
			if (endTime >= 120) {
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
		// TODO: SWITCH BACKGROUND BASED ON LEVEL JSON (may need to move this to a different location)
		if (gameState == GameState.OVER) {
			if (ticks >= 120) {
				drawLose();
			}
		}
		if (gameState == GameState.WON) {
			drawWin();
		}
		if (gameState == GameState.PLAY || gameState == GameState.INTRO || gameState == GameState.PAUSE || (gameState == GameState.OVER && ticks < 120)){
//			drawLose();
//			gameState = GameState.OVER;
//		}
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
			gameplayController.sb.displayCombo((gameplayController.LEFTBOUND + gameplayController.RIGHTBOUND)/2f,  (gameplayController.totalHeight + gameplayController.TOPBOUND)/2f, canvas);
			gameplayController.sb.displayScore(gameplayController.LEFTBOUND,  (gameplayController.totalHeight - gameplayController.TOPBOUND)/4f + gameplayController.TOPBOUND, canvas);
			gameplayController.sb.displayMultiplier(gameplayController.LEFTBOUND,  3*(gameplayController.totalHeight - gameplayController.TOPBOUND)/5f + gameplayController.TOPBOUND, canvas);

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
			if (gameState == GameState.PLAY && endTime > 0) {
				if (endTime <= 60) {
					mask.set(0f, 0f, 0f, (((float)(endTime))/60f));
					canvas.draw(introMask, mask, introMask.getWidth()/2, introMask.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, 3, 3);
				}
				else {
					canvas.draw(introMask, Color.BLACK, introMask.getWidth()/2, introMask.getHeight()/2, canvas.getWidth()/2, canvas.getHeight()/2, 0, 3, 3);
				}
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

		// draw the next button; next should be

		long score = gameplayController.sb.getScore();
		canvas.draw(combo, Color.WHITE, combo.getWidth()/2, combo.getHeight()/2,
				comboCoords.x, comboCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		long maxCombo =gameplayController.sb.getMaxCombo();
		canvas.drawText(String.valueOf(maxCombo), lucidaFont,comboCoords.x+combo.getWidth()/2,comboCoords.y,
				Color.WHITE);

		canvas.draw(perfect, Color.WHITE, perfect.getWidth()/2, perfect.getHeight()/2,
				perfectCoords.x, perfectCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		long nPerfect = gameplayController.getNPerfect();
		canvas.drawText(String.valueOf(nPerfect), lucidaFont,perfectCoords.x+combo.getWidth()/2,perfectCoords.y,
				Color.WHITE);

		canvas.draw(good, Color.WHITE, good.getWidth()/2, good.getHeight()/2,
				goodCoords.x, goodCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		long nGood = gameplayController.getNGood();
		canvas.drawText(String.valueOf(nGood), lucidaFont,goodCoords.x+good.getWidth()/2,goodCoords.y,
				Color.WHITE);

		canvas.draw(ok, Color.WHITE, ok.getWidth()/2, ok.getHeight()/2,
				okCoords.x, okCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		long nOk =gameplayController.getNOk();
		canvas.drawText(String.valueOf(nOk), lucidaFont,okCoords.x+good.getWidth()/2,okCoords.y,
				Color.WHITE);

		canvas.draw(miss, Color.WHITE, miss.getWidth()/2, miss.getHeight()/2,
				missCoords.x, missCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		long nMiss = gameplayController.getNOk();
		canvas.drawText(String.valueOf(nMiss), lucidaFont,missCoords.x+good.getWidth()/2,missCoords.y,
				Color.WHITE);

		canvas.draw(line, Color.WHITE, line.getWidth()/2, line.getHeight()/2,
				lineCoords.x, lineCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);

		canvas.draw(scoreIcon, Color.WHITE, scoreIcon.getWidth()/2, scoreIcon.getHeight()/2,
				scoreIconCoords.x, scoreIconCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);

		canvas.drawText(String.valueOf(score), lucidaFont,scoreIconCoords.x+good.getWidth(),scoreIconCoords.y,
				Color.WHITE);

		drawNextRetryLevel(true);

		//draw score from JSON thresholds

		long aThreshold = gameplayController.level.getaThreshold();
		long bThreshold = gameplayController.level.getbThreshold();
		long cThreshold = gameplayController.level.getcThreshold();
		long sThreshold = gameplayController.level.getsThreshold();

		if (score>=sThreshold){
			canvas.draw(scoreS, Color.WHITE, scoreS.getWidth()/2, scoreS.getHeight()/2,
					scoreSCoords.x, scoreSCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		} else if (score>=aThreshold) {
			canvas.draw(scoreA, Color.WHITE, scoreA.getWidth()/2, scoreA.getHeight()/2,
					scoreACoords.x, scoreACoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		}else if (score>=bThreshold) {
			canvas.draw(scoreB, Color.WHITE, scoreB.getWidth()/2, scoreB.getHeight()/2,
					scoreBCoords.x, scoreBCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		} else if (score>=cThreshold) {
			canvas.draw(scoreC, Color.WHITE, scoreC.getWidth()/2, scoreC.getHeight()/2,
					scoreCCoords.x, scoreCCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		}

	}


	/**
	 * Draws lose screen
	 *
	 * */
	public void drawLose(){
		canvas.drawBackground(loseBackground.getTexture(),0,0);
		canvas.draw(ruinShow, Color.WHITE,ruinShow.getWidth()/2,ruinShow.getHeight()/2,ruinShowCoords.x,ruinShowCoords.y,
				0,WON_BUTTON_SCALE,WON_BUTTON_SCALE);
		drawNextRetryLevel(false);
		canvas.draw(cross, Color.WHITE, cross.getWidth()/2, cross.getHeight()/2,
				crossCoords.x, crossCoords.y, 0, WON_BUTTON_SCALE,WON_BUTTON_SCALE);
	}


	/**
	 * Draws next, retry, and level on the right of the win/lose screen; things in common for win/lose screen.
	 *
	 */
	public void drawNextRetryLevel(boolean drawNextLevel){
		canvas.draw(resultIcon,Color.WHITE,resultIcon.getWidth()/2,resultIcon.getHeight()/2,resultIconCoords.x,
		resultIconCoords.y,0,WON_BUTTON_SCALE, WON_BUTTON_SCALE);

		canvas.draw(goBack, Color.WHITE, goBack.getWidth()/2, goBack.getHeight()/2,
				goBackCoords.x, goBackCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);

		// TODO: FIX THIS
//		if (nextIdx<=9 && drawNextLevel){
//			canvas.draw(nextButtonWon, Color.WHITE, nextButtonWon.getWidth()/2, nextButtonWon.getHeight()/2,
//					nextWonCoords.x, nextWonCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
//		}

		canvas.draw(restartButtonWon, Color.WHITE, restartButtonWon.getWidth()/2, restartButtonWon.getHeight()/2,
				restartWonCoords.x, restartWonCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);
		canvas.draw(levelButtonWon, Color.WHITE, levelButtonWon.getWidth()/2, levelButtonWon.getHeight()/2,
				levelWonCoords.x, levelWonCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);

		canvas.draw(levelAlbumCover,Color.WHITE, levelAlbumCover.getWidth()/2,levelAlbumCover.getHeight()/2,
				levelAlbumCoverCoords.x,levelAlbumCoverCoords.y,0,ALBUM_SCALE,ALBUM_SCALE );

		canvas.draw(difficultyIcon, Color.WHITE, difficultyIcon.getWidth()/2,difficultyIcon.getHeight()/2,
				difficultyIconCoords.x,difficultyIconCoords.y,0,WON_BUTTON_SCALE,WON_BUTTON_SCALE);
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