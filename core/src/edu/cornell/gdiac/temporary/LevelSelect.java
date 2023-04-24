package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelect implements Screen, InputProcessor, ControllerListener {
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    private String selectedJson;


    /** Whether this player mode is still active */
    private boolean active;

    /** Whether the play button is pressed or not */
    private boolean playPressed;
    private int numLevels;

    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 1;

    /** Player mode for the game proper (CONTROLLER CLASS) */
    private GameMode playing;

    /** Play button to display when done */
    private Texture playButton;

    /** Play button to display easy level*/
    private Texture easyButton;

    /** Play button to display medium level */
    private Texture mediumButton;

    /** Play button to display hard level */
    private Texture hardButton;

    /** button for a level */
    private Texture[] albumCovers;

    /** coord for level button */

    private Vector2[] albumCoverCoords;

    /** The background texture */
    private Texture background;
    private float scale=1;

    /** temp scale b/c album cover too big */
    private float scale2=0.15f;

    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;


    /** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;

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

    private String[] allLevels;


    private AssetDirectory directory;

    private int EASY = 1;
    private int MEDIUM = 2;
    private  int HARD = 3;

    public int selectedLevel;

    public int selectedDifficulty;

    public LevelSelect(GameCanvas canvas) {
        this.canvas  = canvas;
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

    public String getSelectedJson(){
        return selectedJson;
    }

    /**
     * Parse information about the levels .
     *
     */
    public void populate(AssetDirectory assetDirectory) {
        directory  = assetDirectory;
        background  = directory.getEntry("background",Texture.class); //menu background

        JsonReader jr = new JsonReader();
        JsonValue levelData = jr.parse(Gdx.files.internal("assets.json"));
        numLevels = levelData.get("jsons").size;
        allLevels= new String[numLevels*3];
        albumCoverCoords = new Vector2[numLevels];
        System.out.println("num levels "+numLevels);
        gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
        albumCovers = new Texture[numLevels];


        if (playButton == null || easyButton == null || mediumButton ==null || hardButton==null) {
            Gdx.input.setInputProcessor( this );

            internal = new AssetDirectory( "loading.json" );
            internal.loadAssets();
            internal.finishLoading();

            playButton = internal.getEntry("play",Texture.class);
            //we don't currently have these in the json yet.
            easyButton = internal.getEntry("easy",Texture.class);
            mediumButton = internal.getEntry("medium",Texture.class);
            hardButton = internal.getEntry("hard",Texture.class);

            playButtonCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/8);
            easyButtonCoords = new Vector2(canvas.getWidth()/4 , canvas.getHeight()/3);
            mediumButtonCoords = new Vector2(canvas.getWidth()/2 , canvas.getHeight()/3);
            hardButtonCoords = new Vector2(canvas.getWidth()/2+ canvas.getWidth()/4, canvas.getHeight()/3);
        }



//        temp implementation for temp assets
        for (int i = 0; i <numLevels;i++){
            albumCovers[i] = internal.getEntry(Integer.toString(i+1),Texture.class);
        }

        float w = albumCovers[1].getWidth()*scale2/2;
        albumCoverCoords[0]=new Vector2((canvas.getWidth()/4 ),canvas.getHeight()-canvas.getHeight()/3);
        albumCoverCoords[1]=new Vector2((canvas.getWidth()/2 ),canvas.getHeight()-canvas.getHeight()/3);
        albumCoverCoords[2]=new Vector2((canvas.getWidth()/2 )+ canvas.getWidth()/4,canvas.getHeight()-canvas.getHeight()/3);

        for (int i = 0; i <numLevels*3;i++){
            String levelString = "levels/"+(i+1)+".json";
            allLevels[i]=levelString;
        }


    }

    /**
     * Resets the LevelSelect
     */
    public void reset() {
        Gdx.input.setInputProcessor( this );
        selectedDifficulty= -1;
        selectedLevel = -1;
        playButton = null;
        easyButton=null;
        mediumButton=null;
        hardButton=null;
        playPressed=false;
        hasSelectedLevel = false;
    }

    public boolean getHasSelectedLevel(){
        return hasSelectedLevel;

    }


    public void draw(){
//        System.out.println("start draw");

        canvas.begin();
        canvas.drawBackground(background,0,0);

        // draw easy, medium, and hard buttons
        Color easyButtonTint = (selectedDifficulty == EASY ? Color.GRAY: Color.WHITE);
        canvas.draw(easyButton, easyButtonTint, easyButton.getWidth()/2, easyButton.getHeight()/2,
                easyButtonCoords.x, easyButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color medButtonTint = (selectedDifficulty == MEDIUM ? Color.GRAY: Color.WHITE);
        canvas.draw(mediumButton, medButtonTint, mediumButton.getWidth()/2, mediumButton.getHeight()/2,
                mediumButtonCoords.x, mediumButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        Color hardButtonTint = (selectedDifficulty == HARD ? Color.GRAY: Color.WHITE);
        canvas.draw(hardButton, hardButtonTint, hardButton.getWidth()/2, hardButton.getHeight()/2,
                hardButtonCoords.x, hardButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        // draw each song
        for (int i=0;i<numLevels;i++){
//            Level l = new Level (j,loadDirectory);
            Color albumCoverTint = (selectedLevel == i ? Color.GRAY: Color.WHITE);
            canvas.draw(albumCovers[i],albumCoverTint,albumCovers[i].getWidth()/2,albumCovers[i].getHeight()/2,albumCoverCoords[i].x,
                    albumCoverCoords[i].y,0, BUTTON_SCALE*scale2, BUTTON_SCALE*scale2);
        }

        // draw play button
        Color playButtonTint = (playPressed ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);


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
    public boolean isButtonPressed(int screenX, int screenY, Texture buttonTexture, Vector2 buttonCoords, float scale) {
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



    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
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
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//        System.out.println("touch down");
//        System.out.println(screenX);
//        System.out.println(screenY);

        screenY = canvas.getHeight()-screenY;

        if (playButton == null ) {
            return true;
        }

        if (isButtonPressed(screenX, screenY, easyButton, easyButtonCoords, scale)) {
            selectedDifficulty = 1;
        }

        if (isButtonPressed(screenX, screenY, mediumButton, mediumButtonCoords, scale)) {
            selectedDifficulty = 2;
        }

        if (isButtonPressed(screenX, screenY, hardButton, hardButtonCoords, scale)) {
            selectedDifficulty = 3;
        }

        System.out.println("selected difficulty is: " + selectedDifficulty);

        for (int i=0;i<numLevels;i++){
            System.out.println("numLevels is "+(numLevels));
            if (isButtonPressed(screenX, screenY, albumCovers[i], albumCoverCoords[i], scale2)){
                System.out.println("selected song is "+(i));
                selectedLevel=i;
            }
        }

        if (selectedDifficulty != -1 && selectedLevel!=-1){
            hasSelectedLevel=true;
        }

        if (isButtonPressed(screenX, screenY, playButton, playButtonCoords,scale)) {
            System.out.println("play pressed");
            playPressed=true;

        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
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
            if (getHasSelectedLevel() && playPressed && listener != null) {
                // go to game
                System.out.println("loading level..");
                System.out.println("selected level:" + selectedLevel);
                System.out.println("selected difficulty:" + selectedDifficulty);
                int gameIdx = selectedDifficulty+(selectedLevel*3);
                System.out.println("index of json is:" + gameIdx);
                System.out.println("file name:"+allLevels[gameIdx-1]);

                selectedJson=allLevels[gameIdx-1];
                listener.exitScreen(playing, ExitCode.TO_PLAYING);
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
        directory.unloadAssets();
        directory.dispose();
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
    }
}
