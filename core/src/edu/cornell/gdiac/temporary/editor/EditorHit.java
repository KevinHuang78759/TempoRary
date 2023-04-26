package edu.cornell.gdiac.temporary.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

/** Flag for a random competency hit to occur*/
public class EditorHit implements Comparable<EditorHit>{

    /** amount of band members in the level */
    private static int laneNumber;

    /** position in the song the hit appears */
    private int songPos;

    /** probabilities for each band member that the hit will target them (sum must be less than 100)*/
    private int[] probabilities;

    /** horizontal location of hit on screen */
    private float x;

    /** vertical location of hit on screen */
    private float y;

    /** True if the hit should be visible on screen*/
    private boolean onScreen;

    public EditorHit(int songPos, int[] probabilities){
        this.songPos = songPos;
        this.probabilities = probabilities;
    }

    public static void setLaneNumber(int laneNumber){
        EditorHit.laneNumber = laneNumber;
    }

    public int getLaneNumber(){return laneNumber;}

    public void setPos(int songPos){
        this.songPos = songPos;
    }

    public int getPos(){
        return songPos;
    }

    public void setProbabilities(int[] probabilities){
        this.probabilities = probabilities;
    }

    public int[] getProbabilities(){
        return probabilities;
    }

    /**
     * sets the horizotnal screen location of the hit
     * @param x horizontal screen location
     */
    public void setX(float x){
        this.x = x;
    }

    /**
     * sets the vertical screen location of the hit
     * @param y vertical screen location
     */
    public void setY(float y){
        this.y = y;
    }

    /**
     * returns the horizontal screen location of the hit
     * @return the horizontal screen location
     */
    public float getX(){
        return x;
    }

    /**
     * returns the vertical screen location of the hit
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

    @Override
    public int compareTo(EditorHit h) {
        if (this.getPos() == h.getPos()) {
            return 0;
        } else if (this.getPos() < h.getPos()) {
            return -1;
        } else {
            return 1;
        }
    }

    public void draw(GameCanvas canvas, float zoom, float[] laneEdges, float laneWidth, BitmapFont font){
        float sizeMultiple = 3f;
        if (zoom < 1 / 2) {
            sizeMultiple = 3 * (zoom + 1 / 2);
        }
        for (int i = 0; i < laneEdges.length; i++){
            canvas.drawLine(laneEdges[i], y, laneEdges[i] + laneWidth, y, 8 * ((int) sizeMultiple), Color.BROWN);
            canvas.drawText(String.valueOf(probabilities[i]), font, laneEdges[i] + laneWidth/2, y+4*((int) sizeMultiple));
        }
    }

}