package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelect {
    private int numLevels;

    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 1;

    /** The current state of the play button */
    private int   pressState;
    private boolean hasPlayed;

    /** Play button to display when done */
    private Texture playButton;

    /** Play button to display easy level*/
    private Texture easyButton;

    /** Play button to display medium level */
    private Texture mediumButton;

    /** Play button to display hard level */
    private Texture hardButton;

    private float scale;


    /** Play button x and y coordinates represented as a vector */
    private Vector2 playButtonCoords;

    private static float BUTTON_SCALE  = 0.75f;


    /** easyButton x and y coordinates represented as a vector */
    private Vector2 easyButtonCoords;

    /** mediumButton x and y coordinates represented as a vector */
    private Vector2 mediumButtonCoords;


    /** hardButton x and y coordinates represented as a vector */
    private Vector2 hardButtonCoords;

    private boolean hasSelectedLevel;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private Level[] allLevels;
    private AssetDirectory internal;

    private Texture levelButton;

    public LevelSelect(String file, GameCanvas canvas, int millis) {
        this.canvas  = canvas;
        internal = new AssetDirectory( "loading.json" );
        playButton = null;
        hasSelectedLevel = false;

    }


    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     */
    private void load() {

//        will later load from json
        numLevels = 3;
        if (playButton == null || easyButton == null || mediumButton ==null || hardButton==null) {
            playButton = internal.getEntry("play",Texture.class);
//           we don't currently have these in the json yet.
            easyButton = internal.getEntry("easyButton",Texture.class);
            mediumButton = internal.getEntry("mediumButton",Texture.class);
            hardButton = internal.getEntry("hardButton",Texture.class);

            playButtonCoords = new Vector2(canvas.getWidth()/2 + playButton.getWidth(), canvas.getHeight()/2 + 200);
            easyButtonCoords = new Vector2(canvas.getWidth()/2 + easyButton.getWidth(), canvas.getHeight()/2);
            mediumButtonCoords = new Vector2(canvas.getWidth()/2 + mediumButton.getWidth(), canvas.getHeight()/2);
            hardButtonCoords = new Vector2(canvas.getWidth()/2 + hardButton.getWidth(), canvas.getHeight()/2);
        }
    }

    public void draw(GameCanvas canvas, int numLevels, Level[] allLevels){

        for (Level l: allLevels){
            int currNum = l.getLevelNumber();
            Color levelButtonTint = (l.hasWon() ? Color.GRAY: Color.WHITE);
            canvas.draw(levelButton, levelButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
        }

        Color playButtonTint = (pressState == PLAY_PRESSED ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        canvas.end();


    }
    
}
