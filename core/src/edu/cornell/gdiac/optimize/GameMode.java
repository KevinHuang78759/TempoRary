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
package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;

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
	/** The background image for the game */
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
	/** Handle collision and physics (CONTROLLER CLASS) */
	private CollisionController physicsController;
	/** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;

	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;
	/** Variable to track total time played in milliseconds (SIMPLE FIELDS) */
	private float totalTime = 0;
	/** Whether or not this player mode is still active */
	private boolean active;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** Amount of game ticks which have gone by */
	private int ticks;

	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas) {
		this.canvas = canvas;
		active = false;
		ticks = 0;
		// Null out all pointers, 0 out all ints, etc.
		gameState = GameState.INTRO;

		// Create the controllers.
		inputController = new InputController();
		gameplayController = new GameplayController(true);
		// YOU WILL NEED TO MODIFY THIS NEXT LINE

		/*
		 * Deciding a cell size:
		 * Per each object, we must hedge the expected number of cells we check (which will decrease
		 * as we increase the size of the cell) with the expected number of objects in each cell (which
		 * will increase with cell size. The rigorous formula for this yielded no meaningful optimal result,
		 * since the absolute minima expected value occurs for at a negative cell size
		 *
		 * Thus, we choose a cell size that is slightly larger than the largest object (the ship, which has radius 17.5)
		 * to get rid of any possibility we need to check higher numbers of cells.
		 */
		physicsController = new CollisionController(canvas.getWidth(), canvas.getHeight(), 40.0f);

	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		gameplayController = null;
		physicsController  = null;
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
			gameState = GameState.PLAY;
			gameplayController.start(canvas.getWidth() / 2.0f, physicsController.getFloorLedge(),
					canvas.getWidth(), canvas.getHeight(), inputController.rKey);
			break;
		case OVER:
			if (inputController.didReset()) {
				ticks = 0;
				gameState = GameState.PLAY;
				gameplayController.reset();
				gameplayController.start(canvas.getWidth() / 2.0f, physicsController.getFloorLedge(),
						canvas.getWidth(), canvas.getHeight(), inputController.rKey);
			} else {
				play(delta);
			}
			break;
		case PLAY:
			play(delta);
			ticks++;
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
		// create some kind of data structure for coordinates of notes
		// hm {frame : notes}
		int currTick = ticks % 1800;
		gameplayController.addShell(canvas.getWidth(), canvas.getHeight(), currTick);

		if (ticks % 120 == 0){
			for (int i = 0; i < gameplayController.lineAmount(); i++) {
				gameplayController.setHealth(-1, i);
			}
		}

		for (int health : gameplayController.getHealth()) {
			if (health == 0) {
				gameState = GameState.OVER;
				break;
			}
		}
		// Update objects.
		gameplayController.resolveActions(inputController,delta, canvas.getWidth(), canvas.getHeight());

		// Check for collisions
		totalTime += (delta*1000); // Seconds to milliseconds

		physicsController.processCollisions(gameplayController.getObjects(),0);

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
	private void draw(float delta) {
		float offset = -((totalTime * TIME_MODIFIER) % canvas.getWidth());
		canvas.begin();
		canvas.drawBackground(background,offset,-100);
		if (gameState == GameState.OVER) {
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Game Over!",displayFont, GAME_OVER_OFFSET);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Press R to Restart", displayFont, 0);
		}
		else{
			// Draw the game objects
			for (GameObject o : gameplayController.getObjects()) {
				o.draw(canvas);
			}


			int[] health = gameplayController.getHealth();
			for(int i = 0; i < health.length; ++i){
				displayFont.setColor(Color.MAROON);
				String hp = "Health: " + health[i];
				canvas.drawText(hp, displayFont, i * canvas.getWidth()/(float)health.length, canvas.getHeight() - COUNTER_OFFSET - 30);
			}


			displayFont.setColor(Color.NAVY);
			String Time = "Time: " + ticks;
			canvas.drawText(Time, displayFont, COUNTER_OFFSET + 300, canvas.getHeight()-COUNTER_OFFSET);
			displayFont.setColor(gameplayController.trigger ? Color.CYAN : Color.NAVY);
			String indicator = "____________";
			canvas.drawText(indicator, displayFont, gameplayController.lane * canvas.getWidth()/4f, canvas.getHeight()/3f);




		}
		// Flush information to the graphic buffer.
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
			draw(delta);
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