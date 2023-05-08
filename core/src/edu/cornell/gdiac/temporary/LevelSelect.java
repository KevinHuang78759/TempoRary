package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelect implements Screen, InputProcessor, ControllerListener {

    private static String selectedJson;

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

    private FilmStrip levelBackground;

    /** button for a level */
    private Texture[] albumCovers;

    /** coord for level button */

    private Vector2[] albumCoverCoords;

    /** The background texture */
    private Texture background;
    private float scale=1;

    /** temp scale b/c album cover too big */
    private float scale2=0.15f;

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

    private Texture goLeft;

    private Vector2 goLeftCoords;

    private Texture goRight;

    private Vector2 goRightCoords;

    private AssetDirectory directory;

    private int EASY = 1;
    private int MEDIUM = 2;
    private  int HARD = 3;

    public int selectedLevel;

    public int selectedDifficulty;
    private boolean pressedEscape;

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

    public int getSelectedLevel(){
        return selectedLevel;
    }

    public int getSelectedDifficulty(){
        return selectedDifficulty;
    }
    public Texture[] getAlbumCovers(){
        return albumCovers;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public static void setSelectedJson(String json){
        selectedJson = json;
    }

    public static String getSelectedJson(){
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
//        System.out.println("num levels "+numLevels);
        gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
        albumCovers = new Texture[numLevels];

        playButton = directory.getEntry("ghost-play",Texture.class);
        easyButton = directory.getEntry("easy",Texture.class);
        mediumButton = directory.getEntry("medium",Texture.class);
        hardButton = directory.getEntry("hard",Texture.class);

        goLeft = directory.getEntry("level-select-left",Texture.class);
        goRight = directory.getEntry("level-select-right",Texture.class);

        goLeftCoords = new Vector2(goLeft.getWidth(),canvas.getHeight()/2);
        goRightCoords = new Vector2(canvas.getWidth()-goLeft.getWidth(),canvas.getHeight()/2);


        levelBackground = new FilmStrip(directory.getEntry("level-select-background", Texture.class), 1, 1);

        playButtonCoords = new Vector2(canvas.getWidth()/2, canvas.getHeight()/8);
        mediumButtonCoords = new Vector2(canvas.getWidth()/2 , canvas.getHeight()/4);
        easyButtonCoords = new Vector2(mediumButtonCoords.x-easyButton.getWidth(), canvas.getHeight()/4);
        hardButtonCoords = new Vector2(mediumButtonCoords.x+easyButton.getWidth(), canvas.getHeight()/4);

//        temp implementation for temp assets
        for (int i = 0; i < numLevels;i++){
            albumCovers[i] = directory.getEntry(Integer.toString(i+1),Texture.class);
        }

//        float w = albumCovers[1].getWidth()*scale2/2;
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
        pressedEscape = false;
    }

    public boolean getHasSelectedLevel(){
        return hasSelectedLevel;

    }


    public void draw(){
        canvas.begin();
        canvas.drawBackground(levelBackground.getTexture(),0,0);
//        canvas.drawBackground(background,0,0);

        canvas.draw(goLeft, Color.WHITE, goLeft.getWidth()/2, goLeft.getHeight()/2,
                goLeftCoords.x, goLeftCoords.y, 0, 0.9f*scale, 0.9f*scale);

        canvas.draw(goRight, Color.WHITE, goRight.getWidth()/2, goRight.getHeight()/2,
                goRightCoords.x, goRightCoords.y, 0, 0.9f*scale, 0.9f*scale);

        // draw easy, medium, and hard buttons
        Color easyButtonTint = (selectedDifficulty == EASY ? Color.GRAY: Color.WHITE);
        canvas.draw(easyButton, easyButtonTint, easyButton.getWidth()/2, easyButton.getHeight()/2,
                easyButtonCoords.x, easyButtonCoords.y, 0, 0.5f*scale, 0.5f*scale);

        Color medButtonTint = (selectedDifficulty == MEDIUM ? Color.GRAY: Color.WHITE);
        canvas.draw(mediumButton, medButtonTint, mediumButton.getWidth()/2, mediumButton.getHeight()/2,
                mediumButtonCoords.x, mediumButtonCoords.y, 0, 0.5f*scale, 0.5f*scale);

        Color hardButtonTint = (selectedDifficulty == HARD ? Color.GRAY: Color.WHITE);
        canvas.draw(hardButton, hardButtonTint, hardButton.getWidth()/2, hardButton.getHeight()/2,
                hardButtonCoords.x, hardButtonCoords.y, 0, 0.5f*scale, 0.5f*scale);

        // draw each song
        for (int i=0;i<numLevels;i++){
//            Level l = new Level (j,loadDirectory);
            Color albumCoverTint = (selectedLevel == i ? Color.GRAY: Color.WHITE);
            canvas.draw(albumCovers[i],albumCoverTint,albumCovers[i].getWidth()/2,albumCovers[i].getHeight()/2,albumCoverCoords[i].x,
                    albumCoverCoords[i].y,0, 0.5f*scale, 0.5f*scale);
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
        if (keycode == Input.Keys.ESCAPE) {
            pressedEscape = true;
        }
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

        if (isButtonPressed(screenX, screenY, goLeft, goLeftCoords, 0.9f*scale)) {
            System.out.println("go left pressed");

        }

        if (isButtonPressed(screenX, screenY, goRight, goRightCoords, 0.9f*scale)) {
            System.out.println("go right pressed");

        }

//        System.out.println("selected difficulty is: " + selectedDifficulty);

        for (int i=0;i<numLevels;i++){
//            System.out.println("numLevels is "+(numLevels));
            if (isButtonPressed(screenX, screenY, albumCovers[i], albumCoverCoords[i], scale)){
//                System.out.println("selected song is "+(i));
                selectedLevel=i;
            }
        }

        if (selectedDifficulty != -1 && selectedLevel!=-1){
            hasSelectedLevel=true;
        }

        if (isButtonPressed(screenX, screenY, playButton, playButtonCoords,scale)) {
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
//                System.out.println("loading level..");
//                System.out.println("selected level:" + selectedLevel);
//                System.out.println("selected difficulty:" + selectedDifficulty);
                int gameIdx = selectedDifficulty+(selectedLevel*3);
//                System.out.println("index of json is:" + gameIdx);
//                System.out.println("file name:"+allLevels[gameIdx-1]);
                System.out.println(gameIdx);
                selectedJson=allLevels[gameIdx-1];
                listener.exitScreen(this, ExitCode.TO_PLAYING);
            } else if (pressedEscape && listener != null) {
                listener.exitScreen(this, ExitCode.TO_MENU);
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
