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
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Particle;
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
		OVER
	}

	// Loaded assets
	private Texture background;
	/** The font for giving messages to the player */
	private BitmapFont displayFont;

	/// CONSTANTS
	/** Factor used to compute where we are in scrolling process */
	private static final float TIME_MODIFIER    = 0.00f;
	/** Offset for the shell counter message on the screen */
	private static final float COUNTER_OFFSET   = 5.0f;
	/** Offset for the game over message on the screen */
	private static final float GAME_OVER_OFFSET = 40.0f;

	/** Reference to drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	/** Reads input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;
	/** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;

	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;

	/** Whether or not this player mode is still active */
	private boolean active;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** used for "counting down" before game starts */
	private float waiting = 4f;


	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas)  {
		this.canvas = canvas;
		active = false;

		// Null out all pointers, 0 out all ints, etc.
		gameState = GameState.INTRO;

		// Create the controllers.
		gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
	}

	/**
	 * Initializes the offset to use for the gameplayController
	 * @param offset the offset from CalibrationMode
	 */
	public void initializeOffset(int offset) {
		gameplayController.setOffset(offset);
	}

	public void readLevel(AssetDirectory directory){
		JsonReader jr = new JsonReader();
		JsonValue levelData = jr.parse(Gdx.files.internal("levels/test2.json"));
		gameplayController.loadLevel(levelData, directory);
		if (gameplayController.NUM_LANES == 2) {
			inputController = new InputController(new int[]{1, 2},  new int[gameplayController.lpl]);
		}
		else {
			inputController = new InputController(gameplayController.NUM_LANES, gameplayController.lpl);
		}
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
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
	 * @param directory 	Reference to the asset directory.
	 */
	public void populate(AssetDirectory directory) {
		background  = directory.getEntry("background",Texture.class);
		displayFont = directory.getEntry("times",BitmapFont.class);
		gameplayController.populate(directory);
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

		// Test whether to reset the game.
		switch (gameState) {
			case INTRO:
				// wait a few frames before starting
				if (waiting == 4f) {
					gameplayController.reset();
					gameplayController.update();
					gameplayController.start();
				}
				waiting -= delta;
				if (waiting < 1f) {
					gameState = GameState.PLAY;
					gameplayController.level.startmusic();
				}
				break;
			case OVER:
			case PLAY:
				if (inputController.didReset()) {
					gameplayController.reset();
					gameplayController.start();
				} else if (inputController.didPause()){
					listener.exitScreen(this, 0);
				}
				else {
					play(delta);
				}
				break;
			default:
				break;
		}
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
	private void draw(long sample) {
		canvas.begin();
		//First draw the background
		canvas.drawBackground(background,0,0);
		if (gameState == GameState.OVER) {
			//Draw game over text
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Game Over!",displayFont, GAME_OVER_OFFSET+50);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Press ENTER to Restart", displayFont, 0);
		}
		else{
			//Draw everything in the current level
			gameplayController.level.drawEverything(canvas, gameplayController.activeBandMember, gameplayController.goalBandMember, inputController.triggerPress, inputController.switches());

			// Draw the rest of the game objects on top
			for (Particle o : gameplayController.getParticles()) {
				o.draw(canvas);
			}

			//obtain background color
			Color bkgC = new Color(237f/255f, 224f/255f, 1f, 1.0f);
			//draw two rectangles to cover up spawning/disappearing areas of notes and switches
			canvas.drawRect(0, gameplayController.TOPBOUND, canvas.getWidth(), canvas.getHeight(), bkgC, true);
			canvas.drawRect(0, 0, canvas.getWidth(), gameplayController.BOTTOMBOUND, bkgC, true);
			for(BandMember bandMember : gameplayController.level.getBandMembers()){
				//Draw the band member sprite and competency bar
				bandMember.drawCharacterSprite(canvas);
				bandMember.drawHPBar(canvas);
			}
			// draw the countdown
			if (gameState == GameState.INTRO) {
				canvas.drawTextCentered("" + (int) waiting, displayFont, 0);
			}
		}
		canvas.end();
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
		// IGNORE FOR NOW
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
			draw(0);
			if (inputController.didExit() && listener != null) {
				listener.exitScreen(this, 0);
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
		waiting = 4f;
		gameState = GameState.INTRO;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
		gameplayController.level.stopMusic();
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