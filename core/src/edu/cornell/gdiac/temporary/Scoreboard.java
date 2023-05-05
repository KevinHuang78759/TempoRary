package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Scoreboard {
    private FreeTypeFontGenerator fontGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;
    private BitmapFont font;

    private GlyphLayout layout;
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
        sfx.addSound(0, "sound/comboLost.ogg");
        sfx.addSound(1, "sound/combo1.ogg");
        sfx.addSound(2, "sound/combo2.ogg");
        sfx.addSound(3, "sound/combo3.ogg");
        this.maxLevel = maxLevel;

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/TempoRaryFont.ttf"));
        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 128;
        font = fontGenerator.generateFont(fontParameter);
        layout = new GlyphLayout();
    }

    public void resetScoreboard() {
        totalScore = 0;
        level = 0;
        meter = 0;
    }

    public void resetCombo(){
        if(level > 0){
            sfx.playSound(0, 0.75f);
        }
        meter = 0;
        level = 0;
    }

    public void recieveHit(int hitVal){
        ++meter;
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
    }

    public void setVolume(float volume) {
        sfx.setVolumeAdjust(volume);
    }

    public long getScore(){
        return totalScore;
    }

    public long getMeter(){
        return meter;
    }

    public long getMaxCombo(){
        return maxLevel;
    }
    public void dispose(){
        sfx.dispose();
        fontGenerator.dispose();
        font.dispose();

    }

    private float fontScale = 1f;
    private float fontHeightOffset;
    public void setFontScale(float heightConfine){
        String disp = "S" +
                "\n" +
                "C" +
                "\n" +
                "M";
        layout.setText(font, disp);
        fontScale *= heightConfine/layout.height;
        fontHeightOffset = heightConfine;
        font.getData().setScale(fontScale);
    }

    public void displayScore(float xPos, float yPos, GameCanvas canvas){
        String disp = "Score " +
                totalScore +
                "\n" +
                "Combo " +
                meter +
                "\n" +
                "Multiplier x" +
                levelMultipliers[level];
        canvas.drawText(disp, font, xPos, yPos + fontHeightOffset);

    }
}
