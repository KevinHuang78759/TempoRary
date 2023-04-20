package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelect implements Screen {

    /** Whether this player mode is still active */
    private boolean active;
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

    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;



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

    private JsonValue[] allLevels;


    private AssetDirectory loadDirectory;
    private AssetDirectory assets;

    private Texture levelButton;

    public int selectedLevel;

    public int selectedDifficulty;

    public LevelSelect(String file, GameCanvas canvas) {
        this.canvas  = canvas;
//        loadDirectory = new AssetDirectory("assets/loading.json" );
        selectedDifficulty= -1;
        selectedLevel = -1;
        playButton = null;
        easyButton=null;
        mediumButton=null;
        hardButton=null;


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
        allLevels= new JsonValue[numLevels];
        System.out.println("num levels "+numLevels);


        if (playButton == null || easyButton == null || mediumButton ==null || hardButton==null) {
            playButton = directory.getEntry("play",Texture.class);
//          we don't currently have these in the json yet.
            easyButton = directory.getEntry("play",Texture.class);
            mediumButton = directory.getEntry("play",Texture.class);
            hardButton = directory.getEntry("play",Texture.class);

            playButtonCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/4);
            easyButtonCoords = new Vector2(canvas.getWidth()/3 , canvas.getHeight()/3);
            mediumButtonCoords = new Vector2(canvas.getWidth()/2 , canvas.getHeight()/3);
            hardButtonCoords = new Vector2(canvas.getWidth()/4 , canvas.getHeight()/3);
        }

//      load each level
        for (int i=0; i<numLevels;i++ ){
//            todo: level name is null
//            String levelName = levelData.get(i).name();
//            System.out.println(levelName);
            String levelName = "test2";
            String levelDir = "levels/"+levelName+".json";
            allLevels[i]= jr.parse(Gdx.files.internal(levelDir));
        }

    }

    public boolean getHasSelectedLevel(){
        return hasSelectedLevel;

    }


    public void draw(){
//        System.out.println("start draw");

        canvas.begin();
        canvas.drawBackground(background,0,0);

        // draw easy, medium, and hard buttons
        canvas.draw(easyButton,easyButtonCoords.x,easyButtonCoords.y);
        canvas.draw(mediumButton,mediumButtonCoords.x,mediumButtonCoords.y);
        canvas.draw(hardButton,hardButtonCoords.x,hardButtonCoords.y);

        // draw each song
        for (JsonValue j: allLevels){
//            Level l = new Level (j,loadDirectory);
            // get the album art from json, right now just rectangle

            canvas.drawRect(canvas.getWidth()/2, canvas.getHeight()/3, canvas.getWidth()/2+100, canvas.getHeight()/3+100, Color.GRAY, true);
            //l.hasUnlocked()
            Color levelButtonTint = (true ? Color.GRAY: Color.WHITE);
            canvas.draw(playButton, levelButtonTint, 20, 20,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);
        }

        // draw play button
        Color playButtonTint = (getHasSelectedLevel() ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        if (selectedDifficulty != -1 && selectedLevel!=-1){
            hasSelectedLevel=true;
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

        if (isButtonPressed(screenX, screenY, easyButton, easyButtonCoords)) {
            pressState = 1;
        }

        if (isButtonPressed(screenX, screenY, mediumButton, mediumButtonCoords)) {
            pressState = 2;
        }

        if (isButtonPressed(screenX, screenY, hardButton, hardButtonCoords)) {
            pressState = 3;
        }

//        process which level is selected



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
        active = true;
    }

    @Override
    public void render(float delta) {
        if (active) {
            draw();
            if (getHasSelectedLevel() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
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
        active = false;

    }

    @Override
    public void dispose() {
        loadDirectory.unloadAssets();
        loadDirectory.dispose();
    }
    
}
