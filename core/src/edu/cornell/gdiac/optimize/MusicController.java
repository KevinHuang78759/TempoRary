package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.audio.Music;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.optimize.entity.BandMember;
import edu.cornell.gdiac.optimize.entity.Fish;
import edu.cornell.gdiac.optimize.entity.Level;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MusicController {
    /** Beats shown advance before it must be hit */
    private int beatsShownInAdvance = 10;

    /** 1/48- <offset> is the count of 1/48 quarter beat time to the timepoint */
    private float levelOffset;

    /** difference between the level offset and the time in the song */
    private float songOffset;

    /** drawing offset, in beats
     * how early do we draw the note onto the screen before it scrolls down? */
    private float drawOffset;

    /** Reference to level BPM */
    private int bpm;
    /** Reference to array of notes */
    private ArrayList<ArrayList<Fish>> notes;

    /** Reference to note data */
    private ArrayList<JsonValue> notesData;

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

    /** Song */
    private Music music;


    /** based off of bbb level editor hardcoded numbers */
    public MusicController(Music music, Level level, BandMember[] bandMembers){
        int bpm = level.getBpm();
        this.secondsPerBeat = 60f / bpm;
        this.timeStart = 0;
        this.music = music;
        this.notesData = new ArrayList<JsonValue>();

        for(BandMember bandMember : bandMembers){
            notesData.add(bandMember.getNotesData());
        }

        this.nextNotes = new int[Level.MAX_BANDMEMBERS];
        Arrays.fill(nextNotes, 0);

        this.music.play();
    }

    /** Update the song position and song position in beats.
     *  Initialize new notes as necessary.
     *
     * */
    public ArrayList<Fish> update(int currentBandMember, float delta){
        songPositionSec = delta - timeStart;
        songPositionBeat = songPositionSec / secondsPerBeat;

        ArrayList<Fish> newNotes = new ArrayList<Fish>();

        int i = 0; // this is the current band member

        // iterate through the band members and their note datas
        for(JsonValue noteData : notesData) {
            int currentNextNote = nextNotes[i];

            if(noteData == null) {return newNotes;}

            JsonValue currentNoteData = noteData.get(nextNotes[i]);

            int beat = currentNoteData.getInt("beat");

            if (nextNotes[i] < noteData.size() && beat < (songPositionBeat + beatsShownInAdvance)) {
                if (i == currentBandMember) {
                    // initialize note
                    Fish note = new Fish(currentNoteData);
                    newNotes.add(note);
                    // add new note to newNotes
                }
                nextNotes[i]++;
            }
            i++;
        }

        return newNotes;
    }

    public float getSongPositionBeat() {
        return songPositionBeat;
    }

    public float getSongPositionSec(){
        return songPositionSec;
    }

    /** "offset" in json / 48 = beat */
    public int getBeatFromLevelOffset(int levelOffset){
        return (int) (levelOffset / this.levelOffset); //should be / 48
    }

    /** (offset/48) / bpm = time in song */
    public float getTimeFromLevelOffset(int levelOffset, int bpm){
        int beat = getBeatFromLevelOffset(levelOffset);
        return getTimeFromBeat(beat, bpm);
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
