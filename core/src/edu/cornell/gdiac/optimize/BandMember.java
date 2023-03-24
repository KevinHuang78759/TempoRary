package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.optimize.entity.Note;



public class BandMember {


    Vector2 BL;

    float lineHeight;
    float width;

    float height;

    int numLines;
    Color borderColor;
    Array<Note> hitNotes;
    Array<Note> switchNotes;

    Queue<Note> allNotes;
    Array<Note> backing;

    int maxComp;
    int curComp;

    public BandMember(){
        BL = new Vector2();
        hitNotes = new Array<>();
        switchNotes = new Array<>();
        allNotes = new Queue<>();
        backing = new Array<>();
    }

    public void setNoteXCords(){
        for(Note n : switchNotes){
            //Switch notes should just appear in the middle of the lane
            n.x = BL.x + width/2;

        }
        for(Note n : hitNotes){
            //Hitnotes will be based on what line we are on
            n.x = BL.x + width/(2*numLines) + n.line*(width/numLines);
        }
    }

    public void updateNotes(int frame){
        for(Note n : switchNotes){
            //Switch notes should just appear in the middle of the lane
            n.update(frame);
        }
        for(Note n : hitNotes){
            //Hitnotes will be based on what line we are on
            n.update(frame);
        }
    }
    public void drawHitBar(GameCanvas canvas, float yval, Color hitColor, boolean[] hits){
        //If we get passed an array we must draw 4 hit bars
        for(int i = 0; i < numLines; ++i){
            if(hits[i]){
                System.out.println("hit");
            }
            canvas.drawLine(BL.x + i * width/numLines, yval, BL.x +(i+1) * width/numLines, yval, 3, hits[i] ? hitColor : Color.BLACK);
        }
    }

    public void drawHitBar(GameCanvas canvas, float yval, Color hitColor, boolean hit){
        //If we get passed a single value then we're in a switch lane
        canvas.drawLine(BL.x, yval, BL.x + width, yval, 3, hit ? hitColor : Color.BLACK);
    }

    public void spawnNotes(int frame){
        while(!allNotes.isEmpty() && allNotes.first().startFrame == frame){
            System.out.println(frame + " " + "spawn");
            Note n = allNotes.removeFirst();
            if(n.nt == Note.NType.SWITCH){
                switchNotes.add(n);
            }
            else{
                hitNotes.add(n);
            }
        }
    }
    public void garbageCollect(){
        //Stop and copy both the switch and hit notes
        for(Note n : switchNotes){
            if(!n.destroyed){
                backing.add(n);
            }
        }
        Array<Note> temp = backing;
        backing = switchNotes;
        switchNotes = temp;
        backing.clear();

        for(Note n : hitNotes){
            if(!n.destroyed){
                backing.add(n);
            }
        }
        temp = backing;
        backing = hitNotes;
        hitNotes = temp;
        backing.clear();
    }

    public void compUpdate(int amount){
        curComp = Math.min(Math.max(0, curComp + amount), maxComp);
    }

    public void drawSwitchNotes(GameCanvas canvas){
        setNoteXCords();
        for(Note n : switchNotes){
            if(!n.destroyed){
                n.draw(canvas, 3*width/4, 3*width/4);
            }
        }
    }

    public void drawHitNotes(GameCanvas canvas){
        setNoteXCords();
        for(Note n : hitNotes){
            if(!n.destroyed){
                System.out.println(n.x);
                n.draw(canvas, 3*width/(4*numLines), 3*width/(4*numLines));
            }

        }
    }

    public void drawBorder(GameCanvas canvas){
        canvas.drawRect(BL, width, height, borderColor, false);
    }

    public void drawLineSeps(GameCanvas canvas){
        Color lColor = Color.BLACK;
        for(int i = 1; i < numLines; ++i){
            canvas.drawLine(BL.x + i * (width/numLines), BL.y + height, BL.x + i * (width/numLines), BL.y + height - lineHeight, 3, lColor);
        }
    }


}
