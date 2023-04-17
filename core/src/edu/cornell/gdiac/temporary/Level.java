package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Note;

public class Level {
    public BandMember[] getBandMembers() {
        return bandMembers;
    }

    public Texture getHitNoteTexture() {
        return hitNoteTexture;
    }

    public Texture getSwitchNoteTexture() {
        return switchNoteTexture;
    }

    public Texture getHoldNoteTexture() {
        return holdNoteTexture;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getMaxCompetency() {
        return maxCompetency;
    }

    public int getSpawnOffset() {
        return spawnOffset;
    }

    public MusicQueue getMusic() {
        return music;
    }

    public void setBandMembers(BandMember[] bandMembers) {
        this.bandMembers = bandMembers;
    }

    public void setHitNoteTexture(Texture hitNoteTexture) {
        this.hitNoteTexture = hitNoteTexture;
    }

    public void setSwitchNoteTexture(Texture switchNoteTexture) {
        this.switchNoteTexture = switchNoteTexture;
    }

    public void setHoldNoteTexture(Texture holdNoteTexture) {
        this.holdNoteTexture = holdNoteTexture;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public void setMaxCompetency(int maxCompetency) {
        this.maxCompetency = maxCompetency;
    }

    public void setSpawnOffset(int spawnOffset) {
        this.spawnOffset = spawnOffset;
    }

    public void setMusic(MusicQueue music) {
        this.music = music;
    }

    private BandMember[] bandMembers;

    // TEXTURES
    private Texture hitNoteTexture;
    private Texture switchNoteTexture;
    private Texture holdNoteTexture;
    private Texture holdEndTexture;
    private Texture holdTrailTexture;
    private BitmapFont displayFont;
    private Texture hpbar;
    private Texture noteIndicator;
    private Texture noteIndicatorHit;

    // PROPERTIES
    private String levelName;
    private int levelNumber;
    private int maxCompetency;
    private int spawnOffset;
    private MusicQueue music;

    /**
     * The last sample that health was decremented due to continuous decay
     */
    private long lastDec;
    public Level(JsonValue data, AssetDirectory directory) {
        //Read in Json  Value and populate asset textures
        lastDec = 0;
        levelName = data.getString("levelName");
        levelNumber = data.getInt("levelNumber");
        maxCompetency = data.getInt("maxCompetency");

        // need to take from directory because this is the only way to load it into the music queue
        music = directory.getEntry("challenger", MusicQueue.class);

        hitNoteTexture = directory.getEntry("hit", Texture.class);
        switchNoteTexture = directory.getEntry("switch", Texture.class);
        holdNoteTexture = directory.getEntry("hold-start", Texture.class);
        holdTrailTexture = directory.getEntry("hold-trail", Texture.class);
        displayFont = directory.getEntry("times", BitmapFont.class);
        hpbar = directory.getEntry("hp-bar", Texture.class);
        holdTrailTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.Repeat);
        holdEndTexture = directory.getEntry("hold-end", Texture.class);
        noteIndicator = directory.getEntry("note-indicator", Texture.class);
        noteIndicatorHit = directory.getEntry("note-indicator-hit", Texture.class);

        // preallocate band members
        bandMembers = new BandMember[data.get("bandMembers").size];
        spawnOffset = music.getSampleRate();
        for(int i = 0; i < bandMembers.length; i++){
            bandMembers[i] = new BandMember();
            JsonValue bandMemberData = data.get("bandMembers").get(i);
            Queue<Note> notes = new Queue<>();
            JsonValue noteData = bandMemberData.get("notes");
            for(int j = 0; j < noteData.size; ++j){
                JsonValue thisNote = noteData.get(j);
                Note n;
                if (thisNote.getString("type").equals("beat")){
                    n = new Note(thisNote.getInt("line"), Note.NoteType.BEAT, thisNote.getLong("position") - spawnOffset, hitNoteTexture);
                }
                else if (thisNote.getString("type").equals("switch")){
                    n = new Note(thisNote.getInt("line"), Note.NoteType.SWITCH, thisNote.getLong("position") - spawnOffset, switchNoteTexture);
                }
                else {
                    n = new Note(thisNote.getInt("line"), Note.NoteType.HELD, thisNote.getLong("position") - spawnOffset, holdNoteTexture);
                    n.setHoldTextures(holdTrailTexture,1,holdEndTexture,1);
                    n.setHoldSamples(thisNote.getLong("duration"));
                }
                n.setHitSample(thisNote.getInt("position"));
                notes.addLast(n);
            }
            bandMembers[i].setAllNotes(notes);
            bandMembers[i].setCurComp(maxCompetency);
            bandMembers[i].setMaxComp(maxCompetency);
            bandMembers[i].setLossRate(bandMemberData.getInt("competencyLossRate"));
            bandMembers[i].setHpBarFilmStrip(hpbar, 47);
            bandMembers[i].setFont(displayFont);
            bandMembers[i].setIndicatorTextures(noteIndicator, noteIndicatorHit);
//            System.out.println(System.nanoTime() - t);
        }
    }

    /**
     * Sets all the bottom left corners of the BandMembers. Call this after setting width and height properties
     * @param windowBL - Bottom Left of the play area
     * @param inBetweenWidth - Width between each band member
     */
    public void setBandMemberBl(Vector2 windowBL, float inBetweenWidth){
        float xCoord = windowBL.x;
        float yCoord = windowBL.y;
        for (BandMember bandMember : bandMembers) {
            bandMember.setBottomLeft(new Vector2(xCoord, yCoord));
            xCoord += inBetweenWidth + bandMember.getWidth();
        }
    }

    /**
     * Sets width and height of BandMembers outside of transition. Call once after transitioning is finished
     * @param activeBandMember - index of active BandMember
     * @param large_width - width of active BandMember
     * @param short_width - width of inactive BandMember
     */
    public void setActiveProperties(int activeBandMember, float large_width, float short_width, float maxLineHeight){
        for (BandMember bandMember : bandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
            bandMember.setHeight(maxLineHeight);
        }
        bandMembers[activeBandMember].setWidth(large_width);
        bandMembers[activeBandMember].setLineHeight(maxLineHeight);
    }

    /**
     * Sets the width and line height of BandMembers during transition call at the start and during transition
     * @param previousBM - BandMember we are transitioning away from
     * @param nextBM - BandMember we are transitioning into
     * @param large_width - width of fully active BandMember
     * @param short_width - with of fully in active Band Member
     * @param t_progress - transition progress, should be between 0 and 1. 1 means transition completed.
     * */
    public void setTransitionProperties(int previousBM, int nextBM, float large_width, float short_width, float maxLineHeight, float t_progress){
        //set default widths and heights, which means everyone with no lines and everyone has no lines
        for (BandMember bandMember : bandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
            bandMember.setHeight(maxLineHeight);
        }

        //set the width and line heights of the active and goal bandmembers accordingly
        bandMembers[previousBM].setWidth(large_width - (large_width - short_width)*t_progress);
        bandMembers[nextBM].setWidth(short_width + (large_width - short_width)*t_progress);

        bandMembers[previousBM].setLineHeight(maxLineHeight*(1f-t_progress));
        bandMembers[nextBM].setLineHeight(maxLineHeight*t_progress);
    }

    /**
     * Spawns new notes according to what sample we are at. Also decrements bandmembers' competency
     * for some amount about once a second
     */
    public void updateBandMemberNotes(){
        //First get the sample we at
        long sample = getCurrentSample();
        boolean decTog = false;
        for(BandMember bandMember : bandMembers){
            //update the note frames
            bandMember.updateNotes();
            //spawn new notes accordingly
            bandMember.spawnNotes(sample);
            //check if enough samples have passed since the last decrement

            if(sample - lastDec >= music.getSampleRate()){
                //if so, decrement competency
                bandMember.compUpdate(-bandMember.getLossRate());
//                System.out.println(bm.getCurComp() + " " + bm.getLossRate());
                decTog = true;
            }
        }
        if (decTog){
            lastDec = sample;
        }
    }

    /**
     * Starts the music
     */
    public void startmusic(){
        music.play();
    }

    public void stopMusic() {
        music.stop();
    }

    public void pauseMusic() {
        music.pause();
    }

    /**
     * Gets the current sample of the song
     * @return
     */
    public long getCurrentSample(){
        return (long)(music.getPosition()*music.getSampleRate());
    }

    /**
     * this draws everything the level needs to display on the given canvas
     * @param canvas - what are we drawing on
     * @param active - which band member is active
     * @param goal - which band member are we/have we transitioned to?
     * @param triggers - which triggers are pressed?
     * @param switches - which switches are pressed?
     */
    public void drawEverything(GameCanvas canvas, int active, int goal, boolean[] triggers, boolean[] switches){
        //first we get the sample, since this determines where the notes will be drawn
        long sample = getCurrentSample();
        for(int i = 0; i < bandMembers.length; ++i){
            //Draw the border of each band member
            bandMembers[i].drawBorder(canvas);

            //If we are the goal of the active lane we need to draw separation lines and held/beat notes
            //We also need to draw a separate hit bar for each line
            if(active == i || goal == i){
                bandMembers[i].drawHitNotes(canvas, sample, canvas.getHeight());
                bandMembers[i].drawLineSeps(canvas);
                bandMembers[i].drawHitBar(canvas, triggers);
            }
            //Otherwise just draw the switch notes, and we only have 1 hit bar to draw
            else{
                bandMembers[i].drawSwitchNotes(canvas, sample, canvas.getHeight());
                bandMembers[i].drawHitBar(canvas, switches[i], i);
            }
        }

    }
}
