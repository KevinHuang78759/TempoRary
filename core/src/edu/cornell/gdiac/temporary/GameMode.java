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
	/** Variable to track total time played in milliseconds (SIMPLE FIELDS) */
	private float totalTime = 0;
	/** Whether or not this player mode is still active */
	private boolean active;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** Amount of game ticks which have gone by */
	private int ticks;

	/**
	 * Number of band member lanes
	 */
	int lanes;

	/**
	 * Number of lines per lane
	 */
	int lpl;

	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas,int lanes, int linesPerLane) {
		lpl = linesPerLane;
		this.lanes = lanes;
		this.canvas = canvas;
		active = false;
		ticks = 0;
		// Null out all pointers, 0 out all ints, etc.
		gameState = GameState.INTRO;

		// Create the controllers.
		inputController = new InputController(lanes, lpl);
		gameplayController = new GameplayController(true,lanes,lpl, canvas.getWidth(),canvas.getHeight());
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
			gameState = GameState.PLAY;
			gameplayController.start(inputController.rKey);
			break;
		case OVER:
			if (inputController.didReset()) {
				ticks = 0;
				gameState = GameState.PLAY;
				gameplayController.reset();
				gameplayController.start(inputController.rKey);
			} else {
				play(delta);
			}
			break;
		case PLAY:
			if (inputController.didReset()) {
				ticks = 0;
				gameState = GameState.PLAY;
				gameplayController.reset();
				gameplayController.start(inputController.rKey);
			} else {
				play(delta);
			}
			ticks++;
			break;
		default:
			break;
		}
	}

	/**
	 * current tick we are on
	 */
	int currTick;

	/**
	 * This method processes a single step in the game loop.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	protected void play(float delta) {
		// create some kind of data structure for coordinates of notes
		// hm {frame : notes}
		//make sure currTick doesn't get too big
		currTick = ticks % 1800;
		//Add a random shell for now
		gameplayController.addNote(canvas.getWidth(), canvas.getHeight(), currTick);
		//Every so often check our HP
		if(gameplayController.checkHealth(currTick%150==0)){
			gameState = GameState.OVER;
		}

		// Update objects.
		gameplayController.resolvePhase(inputController);
		gameplayController.resolveActions(inputController, delta, currTick);

		// Check for collisions
		totalTime += (delta*1000); // Seconds to milliseconds

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
		canvas.begin();
		//First draw the background
		canvas.drawBackground(background,0,0);
		if (gameState == GameState.OVER) {
			//Draw game over text
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Game Over!",displayFont, GAME_OVER_OFFSET+50);
			displayFont.setColor(Color.NAVY);
			canvas.drawTextCentered("Press Enter to Restart", displayFont, 0);
		}
		else{
			// Draw the game objects
			for (GameObject o : gameplayController.getObjects()) {
				o.draw(canvas);
			}

			//obtain background color
			Color bkgC = new Color(237f/255f, 224f/255f, 1f, 1.0f);
			//draw two rectangles to cover up spawning/disappearing areas of notes and switches
			canvas.drawRect(0, gameplayController.TOPBOUND, canvas.getWidth(), canvas.getHeight(), bkgC, true);
			canvas.drawRect(0, 0, canvas.getWidth(), gameplayController.BOTTOMBOUND, bkgC, true);

			//keep track of the current widths of each lane
			float[] curWidths = new float[lanes];
			//link each hp bar to a lane, even when it is small
			float[] links = new float[4 * lanes];
			//The current height of the line bars within an active lane
			float curHeight = gameplayController.TOPBOUND - gameplayController.BOTTOMBOUND;
			for(int i = 0; i < lanes; ++i){
				if(gameplayController.curP == GameplayController.play_phase.NOTES){
					//If we are in a NOTES phase, all widths are small except for the active lane, which is large
					curWidths[i] = gameplayController.currentLane == i ? gameplayController.largewidth : gameplayController.smallwidth;
				}
				else{
					//Otherwise we must be transitioning.
					if(i == gameplayController.currentLane){
						//If this is the current active lane, make sure it shrinks, and decrease the height
						curWidths[i] = gameplayController.largewidth + (float)(gameplayController.t_progress)*(gameplayController.smallwidth - gameplayController.largewidth)/(float)(gameplayController.T_SwitchPhases);
						curHeight = (gameplayController.TOPBOUND - gameplayController.BOTTOMBOUND) * (float)(gameplayController.T_SwitchPhases-gameplayController.t_progress)/(float)(gameplayController.T_SwitchPhases);
					}
					else if(i == gameplayController.goal){
						//If this is the goal lane we are trying to transition to, make sure it grows
						curWidths[i] = gameplayController.smallwidth + (float)(gameplayController.t_progress)*(gameplayController.largewidth - gameplayController.smallwidth)/(float)(gameplayController.T_SwitchPhases);
					}
					else{
						//Otherwise this lane should stay a small width
						curWidths[i] = gameplayController.smallwidth;
					}
				}
			}
			//Change the color of the lanes' outline if we are transitioning
			Color cLanes = gameplayController.curP == GameplayController.play_phase.TRANSITION ? Color.RED : Color.MAROON;
			//Lanes and lines will be drawn sequentially from the left. This is our starting XCoordinate
			float Xcoor = gameplayController.LEFTBOUND;
			for(int i = 0; i < lanes; ++i){
				//Find the bottom left of the lane
				Vector2 BL = new Vector2(Xcoor, gameplayController.BOTTOMBOUND);
				//Draw a rectangle from the bottom left using the total available height and the current width as a border
				//for this lane
				canvas.drawRect(BL, curWidths[i], gameplayController.TOPBOUND - gameplayController.BOTTOMBOUND, cLanes, false);
				if(gameplayController.currentLane == i || gameplayController.goal == i){
					//If we are in the active or the goal lane, we need to draw the lines
					for(int j = 0; j < lpl; ++j){
						//Calculate the x coordinate of this line using the bottom left x coordinate of this lane
						float x2 = Xcoor + (j + 1) * curWidths[i] / lpl;

						//We might as well also draw the hit bars here as well. Change their color if they are triggered
						Color hc = (gameplayController.triggers[j] && i == gameplayController.currentLane)? Color.CYAN : Color.NAVY;
						canvas.drawLine(Xcoor + j*curWidths[i]/lpl, gameplayController.hitbarY, x2, gameplayController.hitbarY, 3, hc);

						//if we are not at the last line, draw a line to divide them from the other lines in this lane
						if(j != lpl-1){
							if(gameplayController.currentLane == i){
								//If this is the current lane, draw from the top all the way to the current height
								canvas.drawLine(x2, gameplayController.TOPBOUND, x2, gameplayController.TOPBOUND - curHeight, 3, Color.BLACK);
							}
							else{
								//If it is not the current lane, it must be the goal lane, so draw from the bottom up to
								//current height
								canvas.drawLine(x2, gameplayController.TOPBOUND, x2, gameplayController.BOTTOMBOUND + curHeight, 3, Color.BLACK);
							}
						}
					}
				}
				else{
					//If this is not the current or goal lane, just draw the hitbar
					canvas.drawLine(Xcoor, gameplayController.hitbarY, Xcoor + curWidths[i], gameplayController.hitbarY, 3, Color.NAVY);
				}
				//calculate the link line coordinates for later
				links[2 * i] = Xcoor + curWidths[i]/2;
				links[2 * i + 1] = gameplayController.BOTTOMBOUND;
				//increment our X coordinate with the width of this lane, and the distance between each lane
				Xcoor += curWidths[i];
				Xcoor += gameplayController.inBetweenWidth;
			}
			//Now we need to draw the HP bars, which is done sequentially as well
			//Start our X coordinate at the minimum x margin
			Xcoor = gameplayController.LEFTBOUND;
			//Get the current HP values
			int[] hp = gameplayController.getHealth();
			for(int i = 0; i < lanes; ++i){
				//Draw the filled in fraction of each HP bar with respect to current health. Change the color from green
				//to red if it is low enough.
				canvas.drawRect(Xcoor+1, gameplayController.BOTTOMBOUND/5+1, Xcoor + (gameplayController.hpwidth *((float)hp[i]/(float)gameplayController.MAX_HEALTH))-1, gameplayController.BOTTOMBOUND*2f/5f-1,hp[i] < gameplayController.MAX_HEALTH/4? Color.RED : Color.GREEN,true);
				//Draw the outline over the actual filled in portion so we cover the edges
				canvas.drawRect(Xcoor, gameplayController.BOTTOMBOUND/5, Xcoor + gameplayController.hpwidth, gameplayController.BOTTOMBOUND*2f/5f,Color.BLACK,false);
				//Determine the other endpoints of the link lines
				links[2*lanes + 2*i] = Xcoor + gameplayController.hpwidth/2;
				links[2*lanes + 2*i + 1] = gameplayController.BOTTOMBOUND*2f/5f;
				//increment out X coordinate
				Xcoor += gameplayController.hpwidth + gameplayController.hpbet;
			}
			for(int i = 0; i < lanes; ++i){
				//Draw the link lines
				canvas.drawLine(links[2*i],links[2*i+1],links[2*lanes+2*i],links[2*lanes+2*i+1],2,Color.BLACK);
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