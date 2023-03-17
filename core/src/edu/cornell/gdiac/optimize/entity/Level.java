/*
Level.java

This is a passive model that stores all level information that
other classes reference. */

package edu.cornell.gdiac.optimize.entity;
import com.badlogic.gdx.audio.Music;

public class Level {
    /** Title of a Level (may not equal song title) **/
    private String name;
    /** Order in level progression **/
    private int order;
    /** Song asset associated with level **/
    private Music song;
    /** BPM of song determines speed of all the notes **/
    private int bpm;
    /** Determines how often random audience attacks occur **/
    private int randomness;
    /** Defines maximum and starting competency bars of all instruments **/
    private int maxCompetency;
    /** Array of all band members **/
    private BandMember[] bandMembers;

    /**
     * Initialize a level with trivial information.
     * */
    public Level() {

    }

    public void update(float delta){

    }

    public void draw(float delta){

    }

}
