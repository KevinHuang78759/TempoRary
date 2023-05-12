package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelect implements Screen, InputProcessor, ControllerListener {

    private static String selectedJson;

    float WON_BUTTON_SCALE = 0.7f;
    /** Whether this player mode is still active */
    private boolean active;

    /** Whether the play button is pressed or not */
    private boolean playPressed;
    private int numSongs;

    /** Pressed down button state for the play button */
    private static final int PLAY_PRESSED = 1;

    private Texture goBack;
    private Vector2 goBackCoords;

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

    /** coordinates for each album; will always have 3 elements;
     * albumCoverCoords[0] left, [1] center, [2] right, */

    private Vector2[] albumCoverCoords;

    private float albumCoverLeftX;
    private float albumCoverMiddleX;
    private float albumCoverRightX;

    private float albumCoverY;


    /** The background texture */
    private Texture background;
    private float scale=1;

    /** temp scale b/c album cover too big */
    private float scale2=0.15f;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;

    /** true if we want to draw the left album */
    private boolean drawLeft;

    private static float BUTTON_SCALE  = 0.75f;

    /** Play button x and y coordinates represented as a vector */
    private Vector2 playButtonCoords;

    /** easyButton x and y coordinates represented as a vector */
    private Vector2 easyButtonCoords;

    /** mediumButton x and y coordinates represented as a vector */
    private Vector2 mediumButtonCoords;


    /** hardButton x and y coordinates represented as a vector */
    private Vector2 hardButtonCoords;


    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** A string array of levels in order;
     * invariant 1: in order; allLevels[0] is song 1 easy level.
     * invariant 2: size is a multiple of 3, because we have 3 difficulties */
    private String[] allLevels;

    private Texture goLeft;

    private Vector2 goLeftCoords;

    private Texture goRight;

    private Vector2 goRightCoords;

    private AssetDirectory directory;

    private int EASY = 1;
    private int MEDIUM = 2;
    private  int HARD = 3;

    /** the scale for the song in the middle in level select */
    float centerScale = 0.65f*scale;

    /** the scale for the songs on the sides in level select */
    float cornerScale = 0.45f*scale;

    /** Selected song; index from 0. */
    public int selectedLevel;

    /** Selected song; 1 is easy, 2 is medium, 3 is hard. */
    public int selectedDifficulty;
    private boolean pressedEscape;

    int prevLevel=selectedLevel;

    float albumScales[];


    /**
     * Enum to determine whether or not we have selected a song, or we are transitioning between songs
     */
    public enum transitionPhase {
        SELECTED,
        TRANSITION
    }

    public LevelSelect(GameCanvas canvas) {
        this.canvas  = canvas;
        selectedDifficulty= 2;
        selectedLevel = 0;
        playButton = null;
        easyButton=null;
        mediumButton=null;
        hardButton=null;
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
        goBack = directory.getEntry("go-back", Texture.class);
        goBackCoords=new Vector2 (goBack.getWidth(), canvas.getHeight()-goBack.getWidth());

        JsonReader jr = new JsonReader();
        JsonValue levelData = jr.parse(Gdx.files.internal("assets.json"));
        allLevels = levelData.get("levels").asStringArray();
        numSongs = allLevels.length/3;
        assert allLevels.length%3 == 0;
//        allLevels= new String[numSongs *3];
        albumCoverCoords = new Vector2[allLevels.length];
        albumScales = new float[allLevels.length];
//        System.out.println("num levels "+numLevels);
        gameplayController = new GameplayController(canvas.getWidth(),canvas.getHeight());
        albumCovers = new Texture[numSongs];

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
        drawLeft=false;

//      album covers are called 1, 2, 3 and so on in assets.json
        for (int i = 0; i < numSongs; i++){
            albumCovers[i] = directory.getEntry(Integer.toString(i+1),Texture.class);
        }

        albumCoverLeftX = canvas.getWidth()/4-10;
        albumCoverMiddleX = canvas.getWidth()/2;
        albumCoverRightX = (canvas.getWidth()/2 )+ (canvas.getWidth()/4)+10;
        albumCoverY = (canvas.getHeight()*2/3)-25;

        if (selectedLevel>=1){
            albumCoverCoords[selectedLevel-1] = new Vector2(albumCoverLeftX,albumCoverY);
            albumScales[selectedLevel-1] = cornerScale;
        }
        albumCoverCoords[selectedLevel] =new Vector2 (albumCoverMiddleX,albumCoverY);// the first song is at the middle at first
        albumScales[selectedLevel] = centerScale;

        if (selectedLevel+1< numSongs){
            albumCoverCoords[selectedLevel+1] = new Vector2(albumCoverRightX,albumCoverY);
            albumScales[selectedLevel+1] = cornerScale;
        }


    }

    /**
     * Resets  LevelSelect
     */
    public void reset() {
        Gdx.input.setInputProcessor( this );
        playButton = null;
        easyButton=null;
        mediumButton=null;
        hardButton=null;
        playPressed=false;
        pressedEscape = false;
    }




    public void draw(){
        canvas.begin();

        canvas.drawBackground(levelBackground.getTexture(),0,0);
        canvas.draw(goBack, Color.WHITE, goBack.getWidth()/2, goBack.getHeight()/2,
                goBackCoords.x, goBackCoords.y, 0, WON_BUTTON_SCALE, WON_BUTTON_SCALE);



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
        if (selectedLevel>=1){ //check that if there are a song to the left; if so, draw.
            canvas.draw(albumCovers[selectedLevel-1], Color.WHITE, albumCovers[selectedLevel-1].getWidth()/2,
                    albumCovers[selectedLevel-1].getHeight()/2, albumCoverCoords[selectedLevel-1].x,
                    albumCoverCoords[selectedLevel-1].y, 0, albumScales[selectedLevel-1], albumScales[selectedLevel-1]);
        }

        // It an invariant that selectedLevel is a valid index, so we can simply draw.
        canvas.draw(albumCovers[selectedLevel],Color.WHITE,albumCovers[selectedLevel].getWidth()/2,
                albumCovers[selectedLevel].getHeight()/2,albumCoverCoords[selectedLevel].x,
                albumCoverCoords[selectedLevel].y,0, albumScales[selectedLevel], albumScales[selectedLevel]);

        if (selectedLevel+1< numSongs) {//check that if there are a song to the right; if so, draw.
            canvas.draw(albumCovers[selectedLevel+1],Color.WHITE,albumCovers[selectedLevel+1].getWidth()/2,
                    albumCovers[selectedLevel+1].getHeight()/2,albumCoverCoords[selectedLevel+1].x,
                    albumCoverCoords[selectedLevel+1].y,0, albumScales[selectedLevel+1], albumScales[selectedLevel+1]);
        }

        // draw the goleft and go right buttons
        if (selectedLevel-1>=0) {
            canvas.draw(goLeft, Color.WHITE, goLeft.getWidth() / 2, goLeft.getHeight() / 2,
                    goLeftCoords.x, goLeftCoords.y, 0, 0.9f * scale, 0.9f * scale);
        }
        if (selectedLevel+1< numSongs) {
            canvas.draw(goRight, Color.WHITE, goRight.getWidth() / 2, goRight.getHeight() / 2,
                    goRightCoords.x, goRightCoords.y, 0, 0.9f * scale, 0.9f * scale);
        }


        // draw play button
        Color playButtonTint = (playPressed ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, playButtonTint, playButton.getWidth()/2, playButton.getHeight()/2,
                    playButtonCoords.x, playButtonCoords.y, 0, BUTTON_SCALE*scale, BUTTON_SCALE*scale);

        canvas.end();
    }

    private void drawSongs(){

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

        // if there are a previous level, we allow decrement.
        if (isButtonPressed(screenX, screenY, goLeft, goLeftCoords, 0.9f*scale)) {
            if (selectedLevel-1>=0){
                prevLevel = selectedLevel;
                selectedLevel--;
            }
        }

        // if there are a next level, we allow increment.
        if (isButtonPressed(screenX, screenY, goRight, goRightCoords, 0.9f*scale)) {
            System.out.println("go right pressed");
            if (selectedLevel+1< numSongs){
                prevLevel = selectedLevel;
                selectedLevel++;
            }
        }

//         check if the albums on the sides are touched; if so, update selected level

        if (selectedLevel-1>=0){
            if (isButtonPressed(screenX, screenY, albumCovers[selectedLevel-1], albumCoverCoords[selectedLevel-1], cornerScale)) {
                prevLevel = selectedLevel;
                selectedLevel--;
            }
        }


        if (selectedLevel+1< numSongs){
            if (isButtonPressed(screenX, screenY, albumCovers[selectedLevel+1], albumCoverCoords[selectedLevel+1], cornerScale)) {
                prevLevel = selectedLevel;
                selectedLevel++;
            }
        }



        if (isButtonPressed(screenX, screenY, playButton, playButtonCoords,scale)) {
            playPressed=true;
        }


        if (isButtonPressed(screenX, screenY, goBack, goBackCoords,WON_BUTTON_SCALE)){
            listener.exitScreen(this,  ExitCode.TO_MENU);
        }

        return false;
    }


    boolean set1 = false;
    /**
     * Update coordinates; called per frame
     *
     */
    private void update(){
        System.out.println("prev level:"+prevLevel);
        System.out.println("curr level"+selectedLevel);
        float steps = 100f;
        float scaleChange = centerScale - cornerScale;
        float rightLenX = Math.abs(albumCoverMiddleX - albumCoverLeftX);
        float rightStep = rightLenX / steps;
        float scaleStep = scaleChange / steps;



        if (prevLevel!=selectedLevel){ // need to do transition animation
            if (prevLevel < selectedLevel){// we need to transition to right
                if (selectedLevel+1< numSongs && !set1 ) {
                    albumCoverCoords[selectedLevel + 1] = new Vector2(canvas.getWidth(), albumCoverY);
                    albumScales[selectedLevel + 1]=cornerScale;
                    set1=true;
                }
                System.out.println("prev level coords"+albumCoverCoords[prevLevel].x);
                if (albumCoverCoords[prevLevel].x > albumCoverLeftX) { // move previous level from center to left
                    System.out.println("here");
                    albumCoverCoords[prevLevel].x -= rightStep;
                    albumCoverCoords[selectedLevel].x-=rightStep;
                    if (selectedLevel+1< numSongs){
                        albumCoverCoords[selectedLevel+1].x -=rightStep;
                    }
                    albumScales[prevLevel] -=scaleStep;
                    albumScales[selectedLevel] +=scaleStep;
                } else{ // reset
                    albumCoverCoords[prevLevel]= new Vector2(albumCoverLeftX,albumCoverY);
                    albumCoverCoords[selectedLevel] = new Vector2(albumCoverMiddleX,albumCoverY);
                    if (selectedLevel+1< numSongs) {
                        albumCoverCoords[selectedLevel + 1] = new Vector2(albumCoverRightX, albumCoverY);
                        albumScales[selectedLevel+1] =cornerScale;
                    }
                    albumScales[prevLevel]=cornerScale;
                    albumScales[selectedLevel] = centerScale;
                    prevLevel = selectedLevel;
                    set1=false;
                }

            } else{// we need to transition to left (move to the right)
                if (selectedLevel>=1 && !set1) {
                    albumCoverCoords[selectedLevel - 1] = new Vector2(0, albumCoverY);
                    albumScales[selectedLevel - 1]=cornerScale;
                    set1=true;
                }
                if (albumCoverCoords[prevLevel].x < albumCoverRightX) { // move previous level from center to left
                    albumCoverCoords[prevLevel].x += rightStep;
                    albumCoverCoords[selectedLevel].x+=rightStep;
                    if (selectedLevel>=1){
                        albumCoverCoords[selectedLevel-1].x +=rightStep;
                    }
                    albumScales[prevLevel] -=scaleStep;
                    albumScales[selectedLevel] +=scaleStep;
                } else{ // reset
                    albumCoverCoords[prevLevel]= new Vector2(albumCoverRightX,albumCoverY);
                    albumCoverCoords[selectedLevel] = new Vector2(albumCoverMiddleX,albumCoverY);
                    if (selectedLevel>=1) {
                        albumCoverCoords[selectedLevel - 1] = new Vector2(albumCoverLeftX, albumCoverY);
                        albumScales[selectedLevel-1] =cornerScale;
                    }
                    albumScales[prevLevel]=cornerScale;
                    albumScales[selectedLevel] = centerScale;
                    prevLevel = selectedLevel;
                    set1=false;
                }

            }

        }



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
            update();
            draw();
            if (playPressed && listener != null) {
                // go to game
//                System.out.println("selected level:" + selectedLevel);
//                System.out.println("selected difficulty:" + selectedDifficulty);
                int gameIdx = selectedDifficulty+(selectedLevel*3);
                System.out.println("game index: "+gameIdx);
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
