package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class CalibrationController implements Screen {

    /** Whether or not this player mode is still active */
    private boolean active;

    /** The font for giving messages to the player */
    private BitmapFont displayFont;

    // View
    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    // Controllers
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /**
     * Constructs new CalibrationController
     * @param canvas
     */
    public CalibrationController(GameCanvas canvas) {
        inputController = new InputController();
        this.canvas = canvas;
    }

    // TODO: finish method
    public int getOffset() {
        return 0;
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
        displayFont = directory.getEntry("times",BitmapFont.class);
    }

    @Override
    public void render(float delta) {
        if (active) {
            draw();
            if (inputController.didExit() && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void resize(int width, int height) {
        // Auto-generated method stub
    }

    @Override
    public void pause() {
        // Auto-generated method stub
    }

    @Override
    public void resume() {
        // Auto-generated method stub
    }

    @Override
    public void dispose() {
        inputController = null;
        canvas = null;
    }

    private void draw() {
        canvas.begin();
        displayFont.setColor(Color.NAVY);
        canvas.drawTextCentered("Game Over!",displayFont, 50);
        canvas.end();
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
