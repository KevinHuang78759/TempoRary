package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelect implements Screen {
    private int numLevels;

    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 1;

    /** The current state of the play button */
    private int  pressState;

    /** Play button to display when done */
    private Texture playButton;

    /** Play button to display easy level*/
    private Texture easyButton;

    /** Play button to display medium level */
    private Texture mediumButton;

    /** Play button to display hard level */
    private Texture hardButton;

    /** The background texture */
    private Texture background;
    private float scale;

    private static float BUTTON_SCALE  = 0.75f;

    /** Play button x and y coordinates represented as a vector */
    private Vector2 playButtonCoords;

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
    private AssetDirectory loadDirectory;
    private AssetDirectory assets;

    private Texture levelButton;

    public LevelSelect(String file, GameCanvas canvas) {
        this.canvas  = canvas;
//        loadDirectory = new AssetDirectory("assets/loading.json" );
//        playButton = null;

        hasSelectedLevel = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }




    /**
     * Parse information about the levels .
     *
     */
    public void populate(AssetDirectory directory) {
        background  = directory.getEntry("background",Texture.class); //menu background

        JsonReader jr = new JsonReader();
        JsonValue levelData = jr.parse(Gdx.files.internal("assets.json"));
        numLevels = levelData.get("jsons").size;
        allLevels= new Level[numLevels];
        System.out.println("num levels "+numLevels);


        if (playButton == null || easyButton == null || mediumButton ==null || hardButton==null) {
            playButton = directory.getEntry("play",Texture.class);
//          we don't currently have these in the json yet.
            easyButton = directory.getEntry("play",Texture.class);
            mediumButton = directory.getEntry("play",Texture.class);
            hardButton = directory.getEntry("play",Texture.class);

            playButtonCoords = new Vector2(20, 20 + 200);
            easyButtonCoords = new Vector2(20 , 20);
            mediumButtonCoords = new Vector2(20 , 20);
            hardButtonCoords = new Vector2(20 , 20);
        }

//      load each level
        for (int i=0; i<numLevels;i++ ){
            String levelName = directory.getEntry("jsons", String.class);
//            Level(JsonValue data, AssetDirectory directory)
//            level =
//                    loadLevel(levelData, directory);
//            allLevels[i]=;
        }

    }

    public void draw(){
        canvas.begin();
        canvas.drawBackground(background,0,0);
        System.out.println("start draw");

        for (Level l: allLevels){

//            get the album art from json, right now just rectangle
            canvas.drawRect(0, 2, 3, 4, Color.RED, true);
//l.hasUnlocked()
            Color levelButtonTint = (true ? Color.GRAY: Color.WHITE);
            canvas.draw(playButton, levelButtonTint, 20, 20,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
        }

        if (hasSelectedLevel){
            Color playButtonTint = (pressState == PLAY_PRESSED ? Color.GRAY: Color.WHITE);
            canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        }

        canvas.end();


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
        float xRadius = BUTTON_SCALE * scale * buttonTexture.getWidth()/2.0f;
        boolean xInBounds = buttonCoords.x - xRadius <= screenX && buttonCoords.x + xRadius >= screenX;

        // get half the y length of the button portrayed
        float yRadius = BUTTON_SCALE * scale * buttonTexture.getHeight()/2.0f;
        boolean yInBounds = buttonCoords.y - yRadius <= screenY && buttonCoords.y + yRadius >= screenY;
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
        if (playButton == null ) {
            return true;
        }

        // Flip to match graphics coordinates
//        screenY = heightY-screenY;

        // TODO: Fix scaling (?)
        // check if buttons get pressed appropriately
        if (isButtonPressed(screenX, screenY, playButton, playButtonCoords)) {
            pressState = PLAY_PRESSED;
        }
//        if (isButtonPressed(screenX, screenY, levelEditorButton, levelEditorButtonCoords)) {
//            pressState = LEVEL_EDITOR_PRESSED;
//        }
//
//        float radius = BUTTON_SCALE*scale*calibrationButton.getWidth()/2.0f;
//        float dist = (screenX-calibrationButtonCoords.x)*(screenX-calibrationButtonCoords.x)
//                +(screenY-calibrationButtonCoords.y)*(screenY-calibrationButtonCoords.y);
//        if (dist < radius*radius) {
//            pressState = TO_CALIBRATION;
//        }
//        return false;
        return false;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        this.draw();
        listener.exitScreen(this, 0);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    public void dispose() {
        loadDirectory.unloadAssets();
        loadDirectory.dispose();
    }
    
}
