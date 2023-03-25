package edu.cornell.gdiac.temporary.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.temporary.GameCanvas;
import edu.cornell.gdiac.temporary.MusicController;

import java.util.ArrayList;

public class BandMember {
    /** Denote player's band member possession **/
    public enum BandMemberState {
        ACTIVE, TRANSITIONING, INACTIVE
    }

    /** Bottom left corner */
    public Vector2 bottomLeftCorner;
    /** The height of separator lines from the top of the lane*/
    public float lineHeight;
    /** Width of the lane */
    public float width;
    /** Total height (of the lane?) */
    public float height;
    /** Number of lines this band member has */
    public int numLines;
    /** Active array of beat and held notes */
    public Array<Fish> hitNotes;
    /** Active array of switch notes */
    public Array<Fish> switchNotes;
    /** Queue to hold all the notes for this band member across the entire level */
    Queue<Fish> allNotes;
    /** backing array used for garbage collection */
    Array<Fish> backing;
    /** IDentifier for band member **/
    private final int id;
    /** Current competency bar **/
    public int competency;
    /** Rate competency loses health **/
    private final int competencyLossRate;
    /** Rate competency gains health per correct note */
    // TODO: DIFFERENT ACCURACY GAINS DIFFERENT HEALTH. THIS IS SOMETHING LEVEL EDITOR NEEDS TO ACCOMODATE, NO?
    private int competencyGainRate = 4;

    /** Original competency - acts as a boundary */
    public int maxCompetency = 30; // TODO: CHANGE - THIS IS JUST A PLACEHOLDER BC I AM WRITING QUICK CODE

    /** Current State **/
    private BandMemberState state;
    /** Notes for this band member **/
    private ArrayList<Fish> notes = new ArrayList<Fish>();

    /** Note Data for this band member */
    private JsonValue notesData;

    public Texture noteTexture;

    float smallwidth;
    float largewidth;
    float inBetweenWidth;
    float LEFTBOUND;

    public void initBoundaries(float smallwidth, float largewidth, float inBetweenWidth, float LEFTBOUND){
        this.smallwidth = smallwidth;
        this.largewidth = largewidth;
        this.inBetweenWidth = inBetweenWidth;
        this.LEFTBOUND = LEFTBOUND;
    }


    /**get notes */
    public ArrayList<Fish> getNotes() { return notes; }
    public JsonValue getNotesData() { return notesData; }
    public int getCompetencyLossRate(){return competencyLossRate;}

    /** Constructor that instantiates all class fields*/
    public BandMember(){
        this.bottomLeftCorner = new Vector2();
        this.id = -1;
        this.competency = 1;
        this.competencyLossRate = 0;
        this.state = BandMemberState.INACTIVE;

        this.hitNotes = new Array<>();
        this.switchNotes = new Array<>();
        this.allNotes = new Queue<>();
    }

    public BandMember(int id, int maxCompetency, JsonValue data, Texture noteTexture){
        // init BandMember params
        this.bottomLeftCorner = new Vector2();
        this.id = id;
        this.competency = maxCompetency;//data.getInt("maxCompetency");
        this.competencyLossRate = data.getInt("competencyLossRate");
        this.state = BandMemberState.INACTIVE;

        if (data.getString("state")=="Active"){
            this.state = BandMemberState.ACTIVE;
        } else {
            this.state = BandMemberState.INACTIVE;
        }

        this.noteTexture = noteTexture;

        // init Notes
        this.hitNotes = new Array<Fish>();
        this.switchNotes = new Array<Fish>();
        this.allNotes = new Queue<Fish>();
        initNotes(data.get("notes"));

    }

    /** Initialize all the notes and add to allNotes queue. */
    public void initNotes(JsonValue notesData){
        for(JsonValue noteData : notesData){
            Fish note = new Fish(noteData, noteTexture);
            allNotes.addLast(note);
        }
        System.out.println(allNotes.last().getBeat());
        System.out.println(allNotes.first().getBeat());
    }

    /**
     * Update competency by band member's specific competency loss or gain rate.
     * This update function is *not* called every frame!
     * TODO: RANDOM HITS????
     * @param decreasing = true : competency -= lossRate. otherwise, competency += gainRate.
     * */
    public void updateCompetency(boolean decreasing) {
        if(decreasing){
            competency -= competencyLossRate;
        } else {
            competency += competencyGainRate;
        }
    }

    /** Update competency by a specified amount (+-)
     * Do not go below 0 or exceed maximum competency */
    public void updateCompetency(int amount){
        competency = Math.min(Math.max(0, competency + amount), maxCompetency);
    }

    /** Return true if competency <= 0, or the competency is below zero. */
    public boolean isCaught(){
        return (competency <= 0);
    }

    /** Get competency */
    public int getCompetency(){return competency;}

    /** Update the notes for this BandMember.
     *
     * @param delta
     * @param beat of the song currently */
    public void update(float delta, float beat, int order){
        //System.out.println("band update");
        // spawn new notes if necessary
        //System.out.println("!allNotes.empty() is " + !allNotes.isEmpty());
        //System.out.println("beats" + ((beat + MusicController.beatsShownInAdvance) > allNotes.last().getBeat()));

        while(!allNotes.isEmpty() && (beat + MusicController.beatsShownInAdvance) > allNotes.last().getBeat() ){
            //System.out.println("new note added");
            Fish note = allNotes.removeLast();
            note.setPosition(height, order, noteTexture, smallwidth, largewidth, inBetweenWidth, LEFTBOUND);
            if(note.getNoteType() == Fish.NoteType.SINGLE){
                hitNotes.add(note);
            }
            else {
                switchNotes.add(note);
            }
        }

        // Update current notes
        for(Fish note : hitNotes){
            note.update(beat, delta);
        }
        /*for(Fish note : switchNotes) {
        *   note.update(beat, delta);
        * }*/

    }

    /** Draw switch notes */
    public void drawSwitchNotes(GameCanvas canvas){
        for(Fish n : switchNotes){
            if(!n.isDestroyed()){
                //Switch notes should just appear in the middle of the lane
                //n.setX(bottomLeftCorner.x + width/2);
                //n,tail_thickness = width/(4f*numLines);
                n.draw(canvas, width*(3/4), width*(3/4));
            }
        }
    }

    /** Draw held and beat notes. */
    public void drawHitNotes(GameCanvas canvas){
        for(Fish n : hitNotes){
            System.out.println("hitnotes is not empty");
            n.draw(canvas, (width/numLines)*(3/4), (width/numLines)*(3/4));
            if(!n.isDestroyed()){
                System.out.println("!nisdestroyed");
                // hit notes are based on what line we are
                //n.setX(bottomLeftCorner.x + width/(2*numLines) + n.getLane() * (width/numLines));
                n.draw(canvas, (width/numLines)*(3/4), (width/numLines)*(3/4));
            }
        }
    }

    /** Draw border */
    public void drawBorder(GameCanvas canvas){
        // TODO CHANGE???
        canvas.drawRect(bottomLeftCorner, width, height, Color.WHITE, false);
    }

    /**
     * Draw separation lines to divide each line within this lane
     */
    public void drawLineSeps(GameCanvas canvas){
        Color lColor = Color.BLACK;
        for(int i = 1; i < numLines; ++i){
            canvas.drawLine(bottomLeftCorner.x + i * (width/numLines), bottomLeftCorner.y + height, bottomLeftCorner.x + i * (width/numLines), bottomLeftCorner.y + height - lineHeight, 3, lColor);
        }
    }

    /** Garbage collection for both switch and hit notes */
    public void garbageCollect(){
        // copy switch and hit notes (that haven't been destroyed) to backing
        /*for(Fish n : switchNotes){
            if(!n.destroyed){backing.add(n);}
        }*/

        //Array<Fish> temp = backing;
        //backing = switchNotes;
        //switchNotes = temp;
        //backing.clear();

        for(Fish n : hitNotes){
            if(!n.destroyed){backing.add(n);}
        }

        Array<Fish> temp = backing;
        backing = hitNotes;
        hitNotes = temp;
        backing.clear();
    }

}