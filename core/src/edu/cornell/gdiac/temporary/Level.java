package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Note;

public class Level {
    public BandMember[] getBandMembers() {
        return BandMembers;
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

    public String getTitle() {
        return title;
    }

    public int getOrder() {
        return order;
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
        BandMembers = bandMembers;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOrder(int order) {
        this.order = order;
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

    private BandMember[] BandMembers;
    private Texture hitNoteTexture;
    private Texture switchNoteTexture;
    private Texture holdNoteTexture;
    private String title;
    private int order;
    private int maxCompetency;
    private int spawnOffset;
   private MusicQueue music;

    /**
     * The last sample that health was decremented due to continuous decay
     */
    private long lastDec;
    public Level(JsonValue data, AssetDirectory directory) {
        lastDec = 0;
        title = data.getString("title");
        order = data.getInt("number");
        maxCompetency = data.getInt("maxCompetency");
        music = directory.getEntry(data.getString("song"), MusicQueue.class);

        hitNoteTexture = directory.getEntry("hit", Texture.class);
        switchNoteTexture = directory.getEntry("switch", Texture.class);
        holdNoteTexture = directory.getEntry("hold", Texture.class);

        // preallocate band members
        BandMembers = new BandMember[data.get("bandMembers").size];
        spawnOffset = music.getSampleRate()/2;
        for(int i = 0; i < BandMembers.length; i++){
            BandMembers[i] = new BandMember();
            JsonValue bmData = data.get("bandMembers").get(i);
            Queue<Note> notes = new Queue<>();
            JsonValue noteData = bmData.get("notes");
            for(int j = 0; j < noteData.size; ++j){
                JsonValue thisNote = noteData.get(j);
                Note n;
                if (thisNote.getString("type").equals("single")){
                    n = new Note(thisNote.getInt("lane"), Note.NoteType.BEAT, thisNote.getLong("sample") - spawnOffset, hitNoteTexture);
                    n.setHitSample(thisNote.getInt("sample"));
                }
                else if(thisNote.getString("type").equals("switch")){
                    n = new Note(0, Note.NoteType.SWITCH, thisNote.getLong("sample") - spawnOffset, switchNoteTexture);
                    n.setHitSample(thisNote.getInt("sample"));

                }
                else{
                    n = new Note(thisNote.getInt("lane"), Note.NoteType.HELD, thisNote.get("connections").get(0).getLong("sample") - spawnOffset, holdNoteTexture);
                    n.setHoldSamples(thisNote.get("connections").get(1).getLong("sample") - thisNote.get("connections").get(0).getLong("sample"));
                    n.setHitSample(thisNote.get("connections").get(0).getLong("sample"));
                }
                notes.addLast(n);
            }
            BandMembers[i].setAllNotes(notes);
            BandMembers[i].setCurComp(maxCompetency);
            BandMembers[i].setMaxComp(maxCompetency);
            BandMembers[i].setLossRate(bmData.getInt("competencyLossRate"));
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
        for (BandMember bandMember : BandMembers) {
            bandMember.setBottomLeft(new Vector2(xCoord, yCoord));
            xCoord += inBetweenWidth + bandMember.getWidth();
        }
    }

    /**
     * Sets width and height of BandMembers outside of transition. Call once after transitioning is finished
     * @param activeBM - index of active BandMember
     * @param large_width - width of active BandMember
     * @param short_width - width of inactive BandMember
     */
    public void setActiveProperties(int activeBM, float large_width, float short_width, float maxLineHeight){
        for (BandMember bandMember : BandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
        }
        BandMembers[activeBM].setWidth(large_width);
        BandMembers[activeBM].setLineHeight(maxLineHeight);
    }

    /**
     * Sets the width and line height of BandMembers during transition call at the start and during transition
     * @param previousBM - BandMember we are transitioning away from
     * @param nextBM - BandMember we are transitioning into
     * @param large_width - width of fully active BandMember
     * @param short_width - with of fully in active Band Member
     * @param t_progress - transition progress, should be between 0 and 1. 1 means completed.
     * */
    public void setTransitionProperties(int previousBM, int nextBM, float large_width, float short_width, float maxLineHeight, float t_progress){
        for (BandMember bandMember : BandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
        }
        BandMembers[previousBM].setWidth(large_width - (large_width - short_width)*t_progress);
        BandMembers[nextBM].setWidth(short_width + (large_width - short_width)*t_progress);

        BandMembers[previousBM].setLineHeight(maxLineHeight*(1f-t_progress));
        BandMembers[nextBM].setLineHeight(maxLineHeight*t_progress);
    }


    public void updateBandMemberNotes(){
        long currentSample = music.getCurrent().getStream().getSampleOffset();
        for(BandMember bm : BandMembers){
            bm.spawnNotes(currentSample);
            if(currentSample - lastDec >= 40000){
                bm.compUpdate(-1);
                lastDec = currentSample;
            }
        }
    }

    public void startmusic(){
        music.play();
    }

    public void drawEverything(GameCanvas canvas, int active, int goal, boolean[] triggers, boolean[] switches){
        long sample = music.getCurrent().getStream().getSampleOffset();
        for(int i = 0; i < BandMembers.length; ++i){
            //Draw the border of each band member
            BandMembers[i].drawBorder(canvas);
            //If we are the goal of the active lane we need to draw separation lines and held/beat notes
            //We also need to draw a separate hit bar for each line
            if(active == i || goal == i){
                BandMembers[i].drawHitNotes(canvas, sample);
                BandMembers[i].drawLineSeps(canvas);
                BandMembers[i].drawHitBar(canvas, Color.WHITE, triggers);
            }
            //Otherwise just draw the switch notes, and we only have 1 hit bar to draw
            else{
                BandMembers[i].drawSwitchNotes(canvas, sample);
                BandMembers[i].drawHitBar(canvas, Color.WHITE, switches[i]);
            }
        }
    }
}
