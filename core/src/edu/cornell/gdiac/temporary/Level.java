package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Note;

public class Level {
    private BandMember[] BandMembers;
    private Texture hitNoteTexture;
    private Texture switchNoteTexture;
    private Texture holdNoteTexture;
    private String title;
    private int order;
    private int bpm;
    private float randomness;
    private int maxCompetency;

    private int spawnOffset;
   private MusicQueue music;
    public Level(JsonValue data, AssetDirectory directory) {
        title = data.getString("title");
        order = data.getInt("number");
        bpm = data.getInt("bpm");
        randomness = data.getFloat("randomness");
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
                    n = new Note(thisNote.getInt("lane"), Note.NoteType.BEAT, thisNote.getInt("sample") - spawnOffset, hitNoteTexture);
                    n.setHitSample(thisNote.getInt("sample"));
                }
                else if(thisNote.getString("type").equals("switch")){
                    n = new Note(0, Note.NoteType.SWITCH, thisNote.getInt("sample") - spawnOffset, switchNoteTexture);
                    n.setHitSample(thisNote.getInt("sample"));

                }
                else{
                    n = new Note(thisNote.getInt("lane"), Note.NoteType.HELD, thisNote.get("connections").get(0).getInt("sample") - spawnOffset, holdNoteTexture);
                    n.setHoldSamples(thisNote.get("connections").get(1).getInt("sample") - thisNote.get("connections").get(0).getInt("sample"));
                    n.setHitSample(thisNote.get("connections").get(0).getInt("sample"));
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
     * Sets all the bottom left corners of the BandMembers. Call this after setting properties
     * @param windowBL - Bottom Left of the play area
     * @param inBetweenWidth - Width between each band member
     */
    public void setBandMemberBl(Vector2 windowBL, float inBetweenWidth){
        float xCoord = windowBL.x;
        float yCoord = windowBL.y;
        for(int i = 0; i < BandMembers.length; ++i){
            BandMembers[i].setBottomLeft(new Vector2(xCoord, yCoord));
            xCoord += inBetweenWidth + BandMembers[i].getWidth();
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
}
