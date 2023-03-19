package edu.cornell.gdiac.optimize.entity;

import com.badlogic.gdx.utils.JsonValue;

public class BandMember {
    /** Denote player's band member possession **/
    public enum BandMemberState {
        ACTIVE, TRANSITIONING, INACTIVE
    }

    /** IDentifier for band member **/
    private int id;
    /** Current competency bar **/
    private int competency;
    /** Rate competency loses health **/
    private int competencyLossRate;
    /** Current State **/
    private BandMemberState state;
    /** Notes for this band member **/
    private Note[] notes;

    public BandMember(int id){
        this.id = id;
        this.competency = -1;
        this.competencyLossRate = 0;
        this.state = BandMemberState.INACTIVE;
        this.notes = new Note[] {};
    }

    public BandMember(int id, JsonValue data){
        this.id = id;
        this.competency = data.getInt("competency");
        this.competencyLossRate = data.getInt("competencyLossRate");
        this.state = BandMemberState.INACTIVE;
        if (data.getString("state")=="Active"){
            this.state = BandMemberState.ACTIVE;
        }

        this.notes = new Note[] {};

    }

    public void update(float delta){

    }

    public void draw(float delta){

    }

}