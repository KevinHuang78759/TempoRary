package edu.cornell.gdiac.temporary.entity;

import edu.cornell.gdiac.temporary.*;
import edu.cornell.gdiac.util.*;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Model class for Notes.
 */
public class CompFlag {
    private int hitStatus;

    public int getHitStatus(){
        return hitStatus;
    }
    public void setHitStatus(int t){
        hitStatus = t;
    }

    private long startSample;
    public long getStartSample(){
        return startSample;
    }
    public void setStartSample(long t){
        startSample = t;
    }

    private long hitSample;
    public long getHitSample(){
        return hitSample;
    }
    public void setHitSample(long t){
        hitSample = t;
    }

    private int lossRate;
    public int getLossRate() {return lossRate;}
    public void setLossRate(int rate) {lossRate = rate;}

    private int noteGain;
    public int getNoteGain() {return noteGain;}
    public void setNoteGain(int gain) {noteGain = gain;}

    private boolean destroyed;
    public boolean isDestroyed(){
        return destroyed;
    }
    public void setDestroyed(boolean d){
        destroyed = d;
    }

    /**
     * Note constructor
     */
    public CompFlag(long startSample) {
        // Set minimum Y velocity for this shell
        hitStatus = 0;
        this.startSample = startSample;
        //Set the number of animation frames
    }

}