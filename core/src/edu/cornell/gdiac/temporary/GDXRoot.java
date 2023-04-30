/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * This time we shown how to use Game with player modes.  The player modes are 
 * implemented by screens.  Player modes handle their own rendering (instead of the
 * root class calling render for them).  When a player mode is ready to quit, it
 * notifies the root class through the ScreenListener interface.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.temporary.editor.*;

import com.badlogic.gdx.*;
import edu.cornell.gdiac.assets.*;

import java.io.IOException;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	// SCREEN MODES
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for viewing the menu (CONTROLLER CLASS) */
	private MenuMode menu;
	/** Player mode for the game proper (CONTROLLER CLASS) */
	private GameMode playing;
	/** Player mode for the level editor (CONTROLLER CLASS) */
	private EditorMode editing;
	/** Player mode for getting calibration (CONTROLLER CLASS) */
	private CalibrationMode calibration;

	private LevelSelect levelscreen;


	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {}

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode("assets.json", canvas,1);
		menu = new MenuMode(canvas);
		playing = new GameMode(canvas);
		try {
			editing = new EditorMode(canvas);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		calibration = new CalibrationMode(canvas);
		levelscreen = new LevelSelect(canvas);

		loading.setScreenListener(this);
		setScreen(loading);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		// dispose of each of the individual controller screens
		Screen screen = getScreen();
		setScreen(null);
		screen.dispose();
		canvas.dispose();
		canvas = null;
		playing.dispose();
		playing = null;
		calibration.dispose();
		calibration = null;
		editing.dispose();
		editing = null;
		menu.dispose();
		menu = null;

		// Unload all the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 * This is where most of the screen switching logic is done.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading && exitCode == ExitCode.TO_MENU) {
			directory = loading.getAssets();
			menu.setScreenListener(this);
			menu.populate(directory);
			setScreen(menu);
			menu.show();
			loading.dispose();
			loading = null;
		}
		else if (exitCode == ExitCode.TO_LEVEL) {
			levelscreen.reset();
			levelscreen.setScreenListener(this);
			levelscreen.populate(directory);
			setScreen(levelscreen);
			levelscreen.show();
		} else if (exitCode ==ExitCode.TO_PLAYING){
			playing.setScreenListener(this);
			String fileName = levelscreen.getSelectedJson();
			playing.readLevel(fileName, directory);
			playing.populate(directory);
			playing.initializeOffset(calibration.getOffset());
			setScreen(playing);
			playing.setSoundVolume(menu.getFXVolumeSetting(), menu.getMusicVolumeSetting());
			playing.reset();
			playing.show();
		} else if (exitCode == ExitCode.TO_EDITOR) {
			screen.hide();
			editing.setScreenListener(this);
			editing.populate(directory);
			setScreen(editing);
			editing.show();
		} else if (exitCode == ExitCode.TO_CALIBRATION) {
			screen.hide();
			calibration.setScreenListener(this);
			calibration.populate(directory);
			setScreen(calibration);
			calibration.show();
		} else if (exitCode == ExitCode.TO_MENU) {
			//System.out.println(calibration.getOffset());
			screen.hide();
			menu.setScreenListener(this);
			setScreen(menu);
			menu.reset();
			menu.show();
		} else if (exitCode == ExitCode.TO_EXIT) {
			// We quit the main application
			Gdx.app.exit();
		} else {
			Gdx.app.error("GDXRoot", "Exit with error code "+exitCode, new RuntimeException());
			Gdx.app.exit();
		}
	}

}
