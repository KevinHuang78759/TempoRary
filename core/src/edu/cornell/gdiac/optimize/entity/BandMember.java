package edu.cornell.gdiac.optimize.entity;

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

    public BandMember(){

    }

    public void update(float delta){

    }

    public void draw(float delta){

    }

}