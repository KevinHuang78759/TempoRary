package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Screen;

public class CalibrationController implements Screen {

    /** Whether or not this player mode is still active */
    private boolean active;

    // View
    /** Reference to drawing context to display graphics (VIEW CLASS) */
    private GameCanvas canvas;

    // Controllers
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;

    public CalibrationController() {
        inputController = new InputController();
        canvas = new GameCanvas();
    }

    // TODO: finish method
    public int getOffset() {
        return 0;
    }

    @Override
    public void render(float delta) {

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
}
