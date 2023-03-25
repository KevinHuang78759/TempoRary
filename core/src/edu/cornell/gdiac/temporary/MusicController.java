package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.audio.Music;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Fish;
import edu.cornell.gdiac.temporary.entity.Level;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MusicController {
    /** Beats shown advance before it must be hit */
    public static int beatsShownInAdvance = 10;

    /** Reference to level BPM */
    private int bpm;

    /** Array of the indices of next note to be played */
    private int[] nextNotes;

    /** Current song position in SECONDS */
    private float songPositionSec;
    /** Current song position in BEATS */
    private float songPositionBeat;
    /** Seconds per beat */
    private float secondsPerBeat;
    /** Seconds before song started */
    private float timeStart;

    /** This is the moving reference point */
    public int lastBeat;

    public Music music;

    private Texture catNoteTexture;

    private ArrayList<JsonValue> notesData;

    public MusicController(Level level, BandMember[] bandMembers, Music music){
        int bpm = level.getBpm();
        this.secondsPerBeat = 60f / bpm;
        this.timeStart = 0;
        this.notesData = new ArrayList<JsonValue>();

        for(BandMember bandMember : bandMembers){
            notesData.add(bandMember.getNotesData());
        }

        this.music = music;
        music.play();
    }

    /** Update the song position in both beats and seconds.
     * SHOULD BE DONE BY FRAME */
    public void update(){
        // (AudioSettings.dspTime – dsptimesong) * song.pitch – offset;
        // this is not right at all
        songPositionSec = music.getPosition();
        //songPositionSec = currentPosition - timeStart;
        songPositionBeat = songPositionSec / secondsPerBeat;
    }



    /** Based on current location in song, check if new notes should be spawned for
     * a given band member.
     * If so, spawn note.
     * */
    /*public ArrayList<Fish> getNewNotes(int currentBandMember){
        ArrayList<Fish> newNotes = new ArrayList<Fish>();

        // this is the current band member
        int i = 0;
        // iterate through the band members and get their noteData
        for(JsonValue noteData : notesData) {

            // if there are no new notes, then stop
            if(noteData == null) {return newNotes;}

            // (BeatsShownInAdvance - (beatOfThisNote - songPosInBeats)) / BeatsShownInAdvance
            // get new note data
            JsonValue currentNoteData = noteData.get(nextNotes[i]);
            if(currentNoteData == null){
                return newNotes;
            }
            int beat = currentNoteData.getInt("beat");

            // if song position + beatsShownInAdvance > note beat, then spawn.
            if (nextNotes[i] <= noteData.size() && beat < (songPositionBeat + beatsShownInAdvance)) {
                if (i == currentBandMember) {
                    // initialize note
                    Fish note = new Fish(currentNoteData, catNoteTexture);
                    newNotes.add(note);
                    // add new note to newNotes
                }
                nextNotes[i] = nextNotes[i]+1; //is this the same as nextNotes[i]++;?
            }
            i++;
        }

        return newNotes; // TODO THIS GOTTA BE DELETED SOMEWHERE TOO
    }*/

    public float getSongPositionBeat() {
        return songPositionBeat;
    }

    public float getSongPositionSec(){
        return songPositionSec;
    }


    /** beat / bpm = minute */
    public float getTimeFromBeat(int beat, int bpm){
        return beat / bpm;
    }

    /** bpm * minute = beat */
    public int getBeatFromTime(float time, int bpm){
        return (int) (bpm * time);
    }
}
