package edu.cornell.gdiac.optimize;

public class MusicController {

    /** 1/48- <offset> is the count of 1/48 quarter beat time to the timepoint */
    private float levelOffset;

    /** difference between the level offset and the time in the song */
    private float songOffset;

    /** drawing offset, in beats
     * how early do we draw the note onto the screen before it scrolls down? */
    private float drawOffset;


    /** based off of bbb level editor hardcoded numbers */
    public MusicController(){
        this.levelOffset = 48f;
        this.songOffset = 120.8f;
    }

    public MusicController(float levelOffset, float songOffset){
        this.levelOffset = levelOffset;
        this.songOffset = songOffset;
    }

    /** Given a Note object, determine if it should be put on the screen
     * based on the position in the song. */
    public boolean isDrawable(){

        return false;
    }

    /** Given a Note object and player input, determine if it was hit on beat. */
    public boolean isOnBeat(){

        return false;
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
