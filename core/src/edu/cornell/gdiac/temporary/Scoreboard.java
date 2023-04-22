package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.utils.JsonValue;

public class Scoreboard {
    /**
     * SFX for hitting combos
     */
    private SoundController<Integer> sfx;
    /**
     * Our current meter - starts at 0
     */
    private long meter;
    /**
     * Our current level - starts at 0
     */
    private int level;
    /**
     * Score multiplier for each combo-level <br>
     * level i has multiplier levelMultipliers[i]
     */
    private int[] levelMultipliers;
    /**
     * Meter requirement for each combo-level <br>
     * Reached levelMeters[i] to advance from level i to level i + 1
     */
    private long[] levelMeters;
    /**
     * Keep track of total score
     */
    private long totalScore;

    /**
     * Maximum combo level
     */
    private int maxLevel;

    public Scoreboard(int maxLevel, int[] multiplers, long[] meters){
        totalScore = 0;
        level = 0;
        meter = 0;
        levelMultipliers = multiplers;
        levelMeters = meters;
        sfx = new SoundController<>();
        sfx.addSound(0, "sound/combolost.ogg");
        sfx.addSound(1, "sound/combo1.ogg");
        sfx.addSound(2, "sound/combo2.ogg");
        sfx.addSound(3, "sound/combo3.ogg");
        this.maxLevel = maxLevel;
    }
    public void resetCombo(){
        if(level > 0){
            sfx.playSound(0, 0.75f);
        }
        meter = 0;
        level = 0;
    }
    public void recieveHit(int hitVal){
        meter += hitVal;
        if(level < maxLevel-1 && meter > levelMeters[level]){
            ++level;
            if(level == maxLevel){
                sfx.playSound(3, 1f);
            }
            else{
                sfx.playSound(level,0.8f);
            }
        }
        totalScore += (long) hitVal* (long)levelMultipliers[level];
        System.out.println("Current Combo:" + level + "Current Score:" + totalScore);
    }
}
