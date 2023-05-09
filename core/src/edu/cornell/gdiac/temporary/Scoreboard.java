package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Scoreboard {
    private FreeTypeFontGenerator fontGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;

    //due to different sizes, we use one font for every score piece
    private BitmapFont scoreFont;
    private BitmapFont multiplierFont;
    private BitmapFont comboFont;

    //likewise three different glyphlayouts to track them all
    private GlyphLayout scoreLayout;
    private GlyphLayout multiplierLayout;
    private GlyphLayout comboLayout;
    /**
     * SFX for hitting combos
     */
    //private SoundController<Integer> sfx;
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

        this.maxLevel = maxLevel;

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/TempoRaryFont.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 128;
        scoreFont = fontGenerator.generateFont(fontParameter);
        multiplierFont = fontGenerator.generateFont(fontParameter);
        comboFont = fontGenerator.generateFont(fontParameter);
        scoreLayout = new GlyphLayout();
        multiplierLayout = new GlyphLayout();
        comboLayout = new GlyphLayout();
    }

    public void resetScoreboard() {
        totalScore = 0;
        level = 0;
        meter = 0;
    }

    public void resetCombo(){

        meter = 0;
        level = 0;
    }

    public void receiveHit(int hitVal){
        ++meter;
        if(level < maxLevel-1 && meter > levelMeters[level]){
            ++level;

        }
        totalScore += (long) hitVal* (long)levelMultipliers[level];
    }

    public long getScore(){
        return totalScore;
    }
    public void dispose(){

        fontGenerator.dispose();
        scoreFont.dispose();
        comboFont.dispose();
        multiplierFont.dispose();
    }

    private float scoreScale = 1f;
    private float multiplierScale = 1f;
    private float comboScale = 1f;



    public void setScoreScale(float heightConfine){
        String disp = "Score 0000000";
        scoreLayout.setText(scoreFont, disp);
        scoreScale *= heightConfine/scoreLayout.height;
        scoreFont.getData().setScale(scoreScale);
    }
    public void setComboScale(float heightConfine){
        String disp = "Combo " + meter;
        comboLayout.setText(comboFont, disp);
        comboScale *= heightConfine/comboLayout.height;
        comboFont.getData().setScale(comboScale);
    }

    public void setMultiplierScale(float heightConfine){
        String disp = "Multiplier x" + levelMultipliers[level];
        multiplierLayout.setText(multiplierFont, disp);
        multiplierScale *= heightConfine/multiplierLayout.height;
        multiplierFont.getData().setScale(multiplierScale);
    }

    public void displayScore(float xPos, float yPos, GameCanvas canvas){
        String disp = "Score " + totalScore;
        canvas.drawText(disp, scoreFont, xPos - scoreLayout.width/4f, yPos + scoreLayout.height/2f);
    }

    public void displayCombo(float xPos, float yPos, GameCanvas canvas){
        String disp = "Combo " + meter;
        canvas.drawText(disp, comboFont, xPos - comboLayout.width/4f, yPos + comboLayout.height/2f);
    }

    public void displayMultiplier(float xPos, float yPos, GameCanvas canvas){
        String disp = "Multiplier x" + levelMultipliers[level];
        canvas.drawText(disp, scoreFont, xPos - multiplierLayout.width/4f, yPos + multiplierLayout.height/2f);
    }
}
