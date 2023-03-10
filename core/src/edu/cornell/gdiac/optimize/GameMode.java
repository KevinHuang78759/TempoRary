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

import com.badlogic.gdx.math.Vector2;
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
		gameplayController = new GameplayController(true,canvas.getWidth(),canvas.getHeight());
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
			if (inputController.didReset()) {
				ticks = 0;
				gameState = GameState.PLAY;
				gameplayController.reset();
				gameplayController.start(canvas.getWidth() / 2.0f, physicsController.getFloorLedge(),
						canvas.getWidth(), canvas.getHeight(), inputController.rKey);
			} else {
				play(delta);
			}
			ticks++;
			break;
		default:
			break;
		}
	}
	int currTick;

	/**
	 * This method processes a single step in the game loop.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	protected void play(float delta) {
		// create some kind of data structure for coordinates of notes
		// hm {frame : notes}
		currTick = ticks % 1800;
		gameplayController.addShellRandom(canvas.getHeight(), currTick);
		if(gameplayController.checkHealth(currTick%150==0)){
			gameState = GameState.OVER;
		}
//
//		if (ticks % 120 == 0){
//			for (int i = 0; i < gameplayController.lineAmount(); i++) {
//				gameplayController.setHealth(-1, i);
//			}
//		}


		// Update objects.
		gameplayController.resolvePhase(inputController, delta);
		gameplayController.resolveActions(inputController,delta, currTick);

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
		canvas.drawBackground(background,0,0);
		if (gameState == GameState.OVER) {
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Game Over!",displayFont, GAME_OVER_OFFSET+50);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Press T to Restart", displayFont, 0);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("(Hold H at the same time to change to random notes)", displayFont, -50);
		}
		else{

			// Draw the game objects
			for (GameObject o : gameplayController.getObjects()) {
				o.draw(canvas);
			}

			Color bkgC = new Color(237f/255f, 224f/255f, 1f, 1.0f);
			canvas.drawRect(gameplayController.LEFTBOUND, gameplayController.TOPBOUND, gameplayController.RIGHTBOUND, canvas.getHeight(), bkgC, true);
			canvas.drawRect(gameplayController.LEFTBOUND, 0, gameplayController.RIGHTBOUND, gameplayController.BOTTOMBOUND, bkgC, true);
			float[] curWidths = new float[4];
			float[] links = new float[16];
			float curHeight = gameplayController.TOPBOUND - gameplayController.BOTTOMBOUND;
			for(int i = 0; i < 4; ++i){
				if(gameplayController.curP == GameplayController.play_phase.NOTES){
					curWidths[i] = gameplayController.currentLane == i ? gameplayController.largewidth : gameplayController.smallwidth;
				}
				else{
					if(i == gameplayController.currentLane){
						curWidths[i] = gameplayController.largewidth + (float)(gameplayController.t_progress)*(gameplayController.smallwidth - gameplayController.largewidth)/(float)(gameplayController.T_SwitchPhases);
						curHeight = (gameplayController.TOPBOUND - gameplayController.BOTTOMBOUND) * (float)(gameplayController.T_SwitchPhases-gameplayController.t_progress)/(float)(gameplayController.T_SwitchPhases);
					}
					else if(i == gameplayController.goal){
						curWidths[i] = gameplayController.smallwidth + (float)(gameplayController.t_progress)*(gameplayController.largewidth - gameplayController.smallwidth)/(float)(gameplayController.T_SwitchPhases);
					}
					else{
						curWidths[i] = gameplayController.smallwidth;
					}
				}
			}
			Color cLanes = gameplayController.curP == GameplayController.play_phase.TRANSITION ? Color.RED : Color.MAROON;

			float Xcoor = gameplayController.LEFTBOUND;
			for(int i = 0; i < 4; ++i){

				Vector2 BL = new Vector2(Xcoor, gameplayController.BOTTOMBOUND);
				canvas.drawRect(BL, curWidths[i], gameplayController.TOPBOUND - gameplayController.BOTTOMBOUND, cLanes, false);
				if(gameplayController.currentLane == i || gameplayController.goal == i){
					for(int j = 0; j < gameplayController.triggers.length; ++j){
						float x2 = Xcoor + (j + 1) * curWidths[i] / 4f;
						Color hc = (gameplayController.triggers[j] && i == gameplayController.currentLane)? Color.CYAN : Color.NAVY;
						canvas.drawLine(Xcoor + j*curWidths[i]/4f, gameplayController.hitbarY, x2, gameplayController.hitbarY, 3, hc);


						if(j != gameplayController.triggers.length - 1){
							if(gameplayController.currentLane == i){
								canvas.drawLine(x2, gameplayController.TOPBOUND, x2, gameplayController.TOPBOUND - curHeight, 3, Color.BLACK);
							}
							else{
								canvas.drawLine(x2, gameplayController.TOPBOUND, x2, gameplayController.BOTTOMBOUND + curHeight, 3, Color.BLACK);

							}
						}

					}

				}
				else{
					canvas.drawLine(Xcoor, gameplayController.hitbarY, Xcoor + curWidths[i], gameplayController.hitbarY, 3, Color.NAVY);
				}
				links[2*i] = Xcoor + curWidths[i]/2;
				links[2*i+1] = gameplayController.BOTTOMBOUND;
				Xcoor += curWidths[i];
				Xcoor += gameplayController.inBetweenWidth;
			}
			Xcoor = gameplayController.LEFTBOUND;
			int[] hp = gameplayController.getHealth();
			for(int i =0; i < hp.length; ++i){
				canvas.drawRect(Xcoor+1, gameplayController.BOTTOMBOUND/5+1, Xcoor + (gameplayController.hpwidth *((float)hp[i]/(float)gameplayController.MAX_HEALTH))-1, gameplayController.BOTTOMBOUND*2f/5f-1,hp[i] < gameplayController.MAX_HEALTH/4? Color.RED : Color.GREEN,true);

				canvas.drawRect(Xcoor, gameplayController.BOTTOMBOUND/5, Xcoor + gameplayController.hpwidth, gameplayController.BOTTOMBOUND*2f/5f,Color.BLACK,false);
				links[8 + 2*i] = Xcoor + gameplayController.hpwidth/2;
				links[8 + 2*i + 1] = gameplayController.BOTTOMBOUND*2f/5f;
				Xcoor += gameplayController.hpwidth + gameplayController.hpbet;
			}
			for(int i = 0; i < 4; ++i){
				canvas.drawLine(links[2*i],links[2*i+1],links[8+2*i],links[8+2*i+1],2,Color.BLACK);
			}
//			for(int i = 0; i < health.length; ++i){
//				displayFont.setColor(Color.MAROON);
//				String hp = "Health: " + health[i];
//				canvas.drawText(hp, displayFont, i * canvas.getWidth()/(float)health.length, canvas.getHeight() - COUNTER_OFFSET - 30);
//			}
//
//
//			displayFont.setColor(Color.NAVY);
//			String Time = "Time: " + ticks;
//			canvas.drawText(Time, displayFont, COUNTER_OFFSET + 300, canvas.getHeight()-COUNTER_OFFSET);
			// Flush information to the graphic buffer.


//			displayFont.setColor(gameplayController.trigger ? Color.CYAN : Color.NAVY);
//			String indicator = "____________";
//			canvas.drawText(indicator, displayFont, gameplayController.lane * canvas.getWidth()/4f, canvas.getHeight()/3f);


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