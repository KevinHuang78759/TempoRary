package edu.cornell.gdiac.optimize.entity;

/*
* let's pretend a note is a fish
 */
public class Fish {
    public enum NoteType {
        SINGLE, HOLD, SWITCH
    }
    private int id;
    private NoteType noteType;
    private int beat;
    private int startBeat; // for held notes only
    private int endBeat; // for held notes only
    private int lane;
    private int timePoint;


}
