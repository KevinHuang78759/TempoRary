package edu.cornell.gdiac.optimize.entity;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;

public class BandMember {
    /** Denote player's band member possession **/
    public enum BandMemberState {
        ACTIVE, TRANSITIONING, INACTIVE
    }

    /** uhhhhhh max number of notes go brr */
    private static final int MAX_NOTES = 10000;

    /** IDentifier for band member **/
    private int id;
    /** Current competency bar **/
    private int competency;
    /** Rate competency loses health **/
    private int competencyLossRate;
    /** Rate competency gains health per correct note */
    // TODO: DIFFERENT ACCURACY GAINS DIFFERENT HEALTH. THIS IS SOMETHING LEVEL EDITOR NEEDS TO ACCOMODATE, NO?
    private int competencyGainRate = 4;

    /** Original competency - acts as a boundary */
    private int maxCompetency = 30; // TODO: CHANGE - THIS IS JUST A PLACEHOLDER BC I AM WRITING QUICK CODE

    /** Current State **/
    private BandMemberState state;
    /** Notes for this band member **/
    private ArrayList<Fish> notes = new ArrayList<Fish>();

    /** Note Data for this band member */
    private JsonValue notesData;


    /**get notes */
    public ArrayList<Fish> getNotes() { return notes; }
    public JsonValue getNotesData() { return notesData; }

    public BandMember(int id){
        this.id = id;
        this.competency = 1;
        this.competencyLossRate = 0;
        this.state = BandMemberState.INACTIVE;
        //this.notes = new Fish[MAX_NOTES];
    }

    public BandMember(int id, int maxCompetency, JsonValue data){
        // init BandMember params
        this.id = id;
        this.competency = maxCompetency;//data.getInt("maxCompetency");
        this.competencyLossRate = data.getInt("competencyLossRate");
        this.state = BandMemberState.INACTIVE;

        if (data.getString("state")=="Active"){
            this.state = BandMemberState.ACTIVE;
        } else {
            this.state = BandMemberState.INACTIVE;
        }

        // init Notes
        JsonValue noteData = data.get("notes");
        this.notesData = noteData;

    }
    /**
     * Update competency such that it is continuously decreasing
     * TODO: boolean dec = ????
     * TODO: RANDOM HITS????
     * @param decreasing = if yes we are decreasing from the competency. doesn't decreasae every tick!
     * @return boolean true if die
     * */
    public boolean updateHealth(boolean decreasing) {
        int temp  = Math.min(competency, maxCompetency);
        if(decreasing){
            competency -= competencyLossRate;
        }

        if(competency <= 0){
            return true;
        }
        return false;
    }

    /** Increase competency by gain rate
     * */
    public boolean addHealth(){
        competency += competencyGainRate;
        competency = Math.min(competency, maxCompetency);
        competency = Math.max(0, competency);
        return true;
    }

    /** Get competency */
    public int getCompetency(){return competency;}

    public void update(float delta){

    }

    public void draw(float delta){

    }

}