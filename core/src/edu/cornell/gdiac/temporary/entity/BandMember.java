package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.temporary.*;
import edu.cornell.gdiac.util.FilmStrip;


public class BandMember {

    FilmStrip hpbar;
    /**
     * Number of frames in HP bar animation
     */
    int hpbarFrames;

    /**
     * Bottom left corner
     */
    private Vector2 BL;
    private BitmapFont displayFont;
    private Texture noteIndicator;
    private Texture noteIndicatorHit;

    public void setBottomLeft(Vector2 V){
        BL = V;
    }

    public Vector2 getBottomLeft(){
        return BL;
    }

    /**
     * The height of separator lines from the top of the lane
     */
    private float lineHeight;

    public void setLineHeight(float l){
        lineHeight = l;
    }

    public float getLineHeight(){
        return lineHeight;
    }
    /**
     * Width of the lane
     */
    private float width;
    public void setWidth(float l){
        width = l;
    }

    public float getWidth(){
        return width;
    }
    /**
     * Total height
     */
    private float height;
    public void setHeight(float l){
        height = l;
        setHitY(BL.y + l/4);
    }

    public float getHeight(){
        return height;
    }

    private float hitY;
    public void setHitY(float t){
        hitY = t;
    }
    public float getHitY(){
        return hitY;
    }


    /**
     * Number of lines this band member has
     */
    private final int numLines = 4;


    public int getNumLines(){
        return numLines;
    }

    /**
     * The color of the border
     */
    private Color borderColor = Color.BLACK;
    public void setBColor(Color l){
        borderColor = l;
    }

    public Color getBorderColor(){
        return borderColor;
    }
    /**
     * Active array of beat and held notes
     */
    private Array<Note> hitNotes;
    public void setHitNotes(Array<Note> l){
        hitNotes = l;
    }

    public Array<Note> getHitNotes(){
        return hitNotes;
    }
    /**
     * Active array of switch notes
     */
    private Array<Note> switchNotes;
    public void setSwitchNotes(Array<Note> l){
        switchNotes = l;
    }

    public Array<Note> getSwitchNotes(){
        return switchNotes;
    }
    /**
     * Queue to hold all the notes for this band member across the entire level
     */
    private Queue<Note> allNotes;

    public void setAllNotes(Queue<Note> l){
        allNotes = l;
    }

    public Queue<Note> getAllNotes(){
        return allNotes;
    }

    /**
     * backing array used for garbage collection
     */
    private Array<Note> backing;


    private int lossRate;
    public void setLossRate(int t){
        lossRate = t;
    }
    public int getLossRate(){
        return lossRate;
    }
    /**
     * Maximum competency
     */

    private int maxComp;

    public void setMaxComp(int p){
        maxComp = p;
    }
    public int getMaxComp(){
        return maxComp;
    }

    /**
     * Current competency
     */
    private int curComp;
    public void setCurComp(int p){
        curComp = p;
    }
    public int getCurComp(){
        return curComp;
    }
    /**
     * Constructor
     */
    public BandMember(){
        BL = new Vector2();
        hitNotes = new Array<>();
        switchNotes = new Array<>();
        allNotes = new Queue<>();
        backing = new Array<>();
    }

    public void setHpBarFilmStrip(Texture t, int numFrames){
        hpbarFrames = numFrames;
        hpbar = new FilmStrip(t,1,hpbarFrames,hpbarFrames);
        hpbar.setFrame(0);
    }

    public void setStartingState(int comp, Queue<Note> notes){
        curComp = comp;
        maxComp = comp;
        allNotes = notes;
    }

    /**
     * Update animations
     */
    public void updateNotes(){
        //Update both switchNotes and hit notes no matter what
        for(Note n : switchNotes){
            n.update();
        }
        for(Note n : hitNotes){
            n.update();
        }
    }

    /**
     * Add notes from the queue to the correct active array
     * @param currentSample
     */
    public void spawnNotes(long currentSample){
        //add everything at the front of the queue that's supposed to start on this frame
        while(!allNotes.isEmpty() && allNotes.first().getStartSample() <= currentSample){
            Note n = allNotes.removeFirst();
            if(n.getNoteType() == Note.NoteType.SWITCH){
                switchNotes.add(n);
            }
            else{
                hitNotes.add(n);
            }
        }
    }

    public void garbageCollect(){
        //Stop and copy both the switch and hit notes
        for(Note n : switchNotes){
            if(!n.isDestroyed()){
                backing.add(n);
            }
        }
        Array<Note> temp = backing;
        backing = switchNotes;
        switchNotes = temp;
        backing.clear();

        for(Note n : hitNotes){
            if(!n.isDestroyed()){
                backing.add(n);
            }
        }
        temp = backing;
        backing = hitNotes;
        hitNotes = temp;
        backing.clear();
    }

    /**
     * Update competency by the specified amount but will not go below 0 or exceed the max
     */
    public void compUpdate(int amount){
        curComp = Math.min(Math.max(0, curComp + amount), maxComp);
        hpbar.setFrame(Math.min((int)((1 - (float)curComp/maxComp)*(hpbarFrames)), hpbarFrames - 1));
    }

    // DRAWING METHODS

    /**
     * Draw the hit bar in a certain color according to if we triggered the line. Pass in an array for the active
     * lane
     * also draw the keyBind
     */
    public void drawHitBar(GameCanvas canvas, Color hitColor, boolean[] hits){
        //If we get passed an array we must draw 4 hit bars
        for(int i = 0; i < numLines; ++i){
            canvas.draw(hits[i] ? noteIndicatorHit : noteIndicator, Color.WHITE, noteIndicatorHit.getWidth() / 2, noteIndicatorHit.getHeight() / 2,
                    ((BL.x + i * width/numLines) + (BL.x +(i+1) * width/numLines)) / 2 - 5, hitY,
                    0.0f,0.45f, 0.45f);
            canvas.drawText(InputController.triggerKeyBinds()[i], displayFont, (BL.x + i * width/numLines + BL.x +(i+1) * width/numLines) / 2, hitY - 80);
        }
    }

    /**
     * Draw the hit bar in a certain color according to if we triggered the line. Pass in a value for a switchable lane
     * also draw the keyBind
     */
    public void drawHitBar(GameCanvas canvas, Color hitColor, boolean hit, int i){
        //If we get passed a single value then we're in a switch lane
        // commenting out now because I'm not too sure about it
//        canvas.drawLine(BL.x, hitY, BL.x + width, hitY, 3, hit ? hitColor : Color.BLACK);
        canvas.draw(hit ? noteIndicatorHit : noteIndicator, Color.WHITE, noteIndicatorHit.getWidth() / 2, noteIndicatorHit.getHeight() / 2,
                (BL.x + BL.x + width) / 2 - 2, hitY,
                0.0f,0.20f, 0.20f);
        canvas.drawText(InputController.switchKeyBinds()[i], displayFont, ( BL.x + BL.x + width) / 2f, hitY - 80);
    }

    /**
     * Draw the switch notes
     */
    public void drawSwitchNotes(GameCanvas canvas, long currentSample, float spawnY){
        for(Note n : switchNotes){
            if(!n.isDestroyed()){
                //Switch notes should just appear in the middle of the lane
                n.setX(BL.x + width/2);
                //Set the Y coordinate according to sampleProgression
                //Calculate the spawning y coordinate to be high enough such that none of the note is
                //visible
                n.setY(spawnY + (float)(currentSample - n.getStartSample())/(n.getHitSample() - n.getStartSample()) *(hitY - spawnY));
                n.draw(canvas, 3*width/4, 3*width/4);
            }
        }
    }

    /**
     * Draw the held and beat notes
     */
    public void drawHitNotes(GameCanvas canvas, long currentSample, float spawnY){
        for(Note n : hitNotes){
            if(!n.isDestroyed()){
                //Hitnotes will be based on what line we are on
                n.setX(BL.x + width/(2*numLines) + n.getLine()*(width/numLines));
                if(n.getNoteType() == Note.NoteType.HELD){
                    //Y coordinates based on formula mentioned in discord.
                    n.setBottomY(spawnY + (float)(currentSample - n.getStartSample())/(n.getHitSample() - n.getStartSample()) *(hitY - spawnY));
                    n.setY(spawnY + Math.max(0, (float)(currentSample - n.getStartSample() - n.getHoldSamples())/(n.getHitSample() - n.getStartSample()))*(hitY - spawnY));
                }
                else{
                    n.setY(spawnY + (float)(currentSample - n.getStartSample())/(n.getHitSample() - n.getStartSample())*(hitY - spawnY));
                }

                n.draw(canvas, 3*width/(4*numLines), 3*width/(4*numLines));
            }

        }
    }

    /**
     * Draw the border
     */
    public void drawBorder(GameCanvas canvas){
        canvas.drawRect(BL, width, height, borderColor, false);
    }

    public void drawHPBar(GameCanvas canvas){
        float scale = (BL.y*4/5)/hpbar.getRegionHeight();
        float trueHeight = scale*hpbar.getRegionHeight();
        canvas.draw(hpbar, Color.WHITE, 0, 0,BL.x + width/10, (BL.y - trueHeight)/2,
                0.0f, scale, scale);
    }

    /**
     * Draw separation lines to divide each line within this lane
     */
    public void drawLineSeps(GameCanvas canvas){
        Color lColor = Color.BLACK;
        for(int i = 1; i < numLines; ++i){
            canvas.drawLine(BL.x + i * (width/numLines), BL.y + height, BL.x + i * (width/numLines), BL.y + height - lineHeight, 3, lColor);
        }
    }

    public void setFont(BitmapFont displayFont) {
        this.displayFont = displayFont;
    }

    public void setIndicatorTextures(Texture texture, Texture textureHit) {
        noteIndicator = texture;
        noteIndicatorHit = textureHit;
    }
}
