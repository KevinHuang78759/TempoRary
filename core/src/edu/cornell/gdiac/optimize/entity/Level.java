/*
Level.java

This is a passive model that stores all level information that
other classes reference. */

package edu.cornell.gdiac.optimize.entity;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.optimize.GameCanvas;

public class Level {
    /** Title of a Level (may not equal song title) **/
    private String title;
    /** Order in level progression **/
    private int order;
    /** fixed maximum number of instruments */
    private static final int MAX_BANDMEMBERS = 4;

    /** Song asset associated with level **/
    private String songPath;
    /** BPM of song determines speed of all the notes **/
    private int bpm;
    /** Determines how often random audience attacks occur **/
    private int randomness;
    /** Defines maximum and starting competency bars of all instruments **/
    private int maxCompetency;
    /** Array of all band members **/
    private BandMember[] bandMembers;


    /**  */

    /**
     * Initialize a level with trivial information.
     * */
    public Level(JsonValue data) {

        this.title = data.getString("title");
        this.order = data.getInt("number");
        this.bpm = data.getInt("bpm");
        this.randomness = data.getInt("randomness");
        this.maxCompetency = data.getInt("maxCompetency");
        this.songPath = data.getString("song");

        // preallocate band members
        this.bandMembers = new BandMember[MAX_BANDMEMBERS];

        for(int i = 0; i < MAX_BANDMEMBERS; i++){
            bandMembers[i] = new BandMember(i);
        }

        // add band member for level
        for(int i = 0; i < data.get("bandMember").size(); i++){
            JsonValue bandMemberData = data.get("bandMember").get(i);
            bandMembers[i] = new BandMember(i, bandMemberData);
        }
    }

    public void update(float delta){

    }

    public void draw(GameCanvas canvas){

    }

}
