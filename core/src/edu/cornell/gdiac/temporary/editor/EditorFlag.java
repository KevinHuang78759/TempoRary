package edu.cornell.gdiac.temporary.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

/** Flag for a change in a band member's competency loss rate*/
public class EditorFlag {
    /** lane the flag corresponds to */
    private int lane;

    /** position in the song the flag appears */
    private int songPos;

    /** Rate at which the competency drains */
    private int lossRate;

    /** horizontal location of flag on screen */
    private float x;

    /** vertical location of flag on screen */
    private float y;

    /** True if the flag should be visible on screen*/
    private boolean onScreen;

    public EditorFlag(int lane, int songPos, int lossRate){
        this.lane = lane;
        this.songPos = songPos;
        this.lossRate = lossRate;
    }

    public void setLane(int lane){
        this.lane = lane;
    }

    public int getLane(){return lane;}

    public void setPos(int songPos){
        this.songPos = songPos;
    }

    public int getPos(){
        return songPos;
    }

    public void setLossRate(int lossRate){
        this.lossRate = lossRate;
    }

    public int getLossRate(){
        return lossRate;
    }

    /**
     * sets the horizotnal screen location of the flag
     * @param x horizontal screen location
     */
    public void setX(float x){
        this.x = x;
    }

    /**
     * sets the vertical screen location of the flag
     * @param y vertical screen location
     */
    public void setY(float y){
        this.y = y;
    }

    /**
     * returns the horizontal screen location of the flag
     * @return the horizontal screen location
     */
    public float getX(){
        return x;
    }

    /**
     * returns the vertical screen location of the flag
     * @return the vertical screen location
     */
    public float getY(){
        return y;
    }

    public void setOnScreen(boolean onScreen){
        this.onScreen = onScreen;
    }

    public boolean OnScreen(){
        return onScreen;
    }
    public void draw(GameCanvas canvas, float zoom, float laneEdge, float laneWidth){
        float sizeMultiple = 3f;
        if (zoom < 1 / 2) {
            sizeMultiple = 3 * (zoom + 1 / 2);
        }
        canvas.drawLine(laneEdge, y, laneEdge + laneWidth, y, 5*((int) sizeMultiple), Color.CORAL);
    }

}
