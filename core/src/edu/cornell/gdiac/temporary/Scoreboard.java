package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;

import static java.lang.Math.max;

public class Scoreboard {
    private Texture[] letterGrades;

    private long[] scoreThreholds;
    private int curTH;
    private Texture background;

    private Vector2 BL;
    private Vector2 TR;

    private Color tint = new Color(1.0f,1.0f,1.0f,0.3f);
    private FreeTypeFontGenerator fontGenerator;

    private FreeTypeFontGenerator boldFont;
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

    private long maxCombo;
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
        curTH = 0;
        letterGrades = new Texture[5];
        scoreThreholds = new long[4];
        totalScore = 0;
        level = 0;
        meter = 0;
        levelMultipliers = multiplers;
        levelMeters = meters;
        background = new Texture(Gdx.files.internal("images/scoreboard_background.png"));
        this.maxLevel = maxLevel;

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-SemiBold.ttf"));

        fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        boldFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-Bold.ttf"));
        fontParameter.size = 96;
        scoreFont = fontGenerator.generateFont(fontParameter);
        multiplierFont = fontGenerator.generateFont(fontParameter);
        comboFont = boldFont.generateFont(fontParameter);
        scoreFont.setColor(Color.WHITE);
        multiplierFont.setColor(Color.WHITE);
        comboFont.setColor(new Color(1f, 0f, 254f/255f,1f));
        scoreLayout = new GlyphLayout();
        multiplierLayout = new GlyphLayout();
        comboLayout = new GlyphLayout();
        letterGrades[0] = new Texture(Gdx.files.internal("images/win_lose/D.png"));
        letterGrades[1] = new Texture(Gdx.files.internal("images/win_lose/C.png"));
        letterGrades[2] = new Texture(Gdx.files.internal("images/win_lose/B.png"));
        letterGrades[3] = new Texture(Gdx.files.internal("images/win_lose/A.png"));
        letterGrades[4] = new Texture(Gdx.files.internal("images/win_lose/S.png"));
    }

    public void setletterTH(long[] th){
        scoreThreholds = th;
    }

    public void setBounds(Vector2 tr, Vector2 bl){
        TR = tr;
        BL = bl;
    }

    public void resetScoreboard() {
        curTH = 0;
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
        maxCombo = max(maxCombo,meter);
        if(curTH < 4 && totalScore >= scoreThreholds[curTH]){
            ++curTH;
        }
    }

    public long getScore(){
        return totalScore;
    }

    public long getMaxCombo(){
        return maxCombo;
    }
    public void dispose(){
        background.dispose();
        fontGenerator.dispose();
        scoreFont.dispose();
        comboFont.dispose();
        multiplierFont.dispose();
        for(int i = 0; i < letterGrades.length; ++i){
            letterGrades[i].dispose();
        }
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
        String disp = Long.toString(totalScore);
        scoreLayout.setText(scoreFont, disp);
        canvas.drawTextSetColor(disp, scoreFont, xPos - scoreLayout.width/2f, yPos + scoreLayout.height/2f);
    }

    public void displayCombo(float xPos, float yPos, GameCanvas canvas){
        String disp = "Combo " + meter;
        comboLayout.setText(comboFont, disp);
        canvas.drawTextSetColor(disp, comboFont, xPos - comboLayout.width/2f, yPos + comboLayout.height/2f);
    }

    public void displayMultiplier(float xPos, float yPos, GameCanvas canvas){
        String disp = "Multiplier x" + levelMultipliers[level];
        multiplierLayout.setText(multiplierFont, disp);
        canvas.drawTextSetColor(disp, multiplierFont, xPos - multiplierLayout.width/2f, yPos + multiplierLayout.height/2f);
    }

    public void displayLetterGrade(GameCanvas canvas){
        float sc = (2f*(TR.y - BL.y)/3f)/letterGrades[curTH].getHeight();
        canvas.draw(letterGrades[curTH], Color.WHITE,
                 letterGrades[curTH].getWidth()/2f, letterGrades[curTH].getHeight()/2f,
                    BL.x*5f/6f + TR.x/6f, (BL.y + TR.y)/2f,
                    0f,
                    sc, sc);
    }

    public void drawBackground(GameCanvas canvas){
        canvas.draw(background,
                    tint,
                background.getWidth()/2f, background.getHeight()/2f,
                (TR.x + BL.x)/2f, (TR.y + BL.y)/2f,
                0f,
                (TR.x - BL.x)/(background.getWidth()),(TR.y - BL.y)/(background.getHeight()));
    }

    public void displayScoreBoard(GameCanvas canvas){
        drawBackground(canvas);
        displayLetterGrade(canvas);
        setScoreScale((TR.y - BL.y)/5f);
        displayScore((TR.x + BL.x)/2f, BL.y + (TR.y - BL.y)/4f, canvas);
        setComboScale(7f*(TR.y - BL.y)/20f);
        displayCombo((TR.x + BL.x)/2f, BL.y + 13f*(TR.y - BL.y)/20f, canvas);
        setMultiplierScale((TR.y - BL.y)/5f);
        displayMultiplier((3f*TR.x/4f + BL.x/4f), BL.y + (TR.y - BL.y)/4f, canvas);
    }
}
