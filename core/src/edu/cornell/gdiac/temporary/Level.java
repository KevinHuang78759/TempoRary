package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.graphics.Texture;
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
    public Level(JsonValue data, Texture noteTexture, AssetDirectory directory) {
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
        spawnOffset = 20000;
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
}
