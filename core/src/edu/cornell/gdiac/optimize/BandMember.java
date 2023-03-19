package edu.cornell.gdiac.optimize;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.optimize.entity.Note;

import java.util.ArrayList;

public class BandMember {
    float width;
    float minWidth;
    float maxWidth;
    float hpy;
    float hpwidth;
    float hpHeight;
    float separation;
    float[] linkCoords;

    float  hitY;

    boolean active;
    boolean goal;
    float comp;
    float maxComp;
    float height;

    float lineHeight;
    int lines;
    Vector2 BL;
    Array<Note> notes;

    Color borderColor;
    Color lineSepColor;

    Color triggerColor;
    Color triggerHitColor;

    boolean[] triggers;
    public void setNoteXCoord(){
        for(Note n : notes){
            n.setX(BL.x + (width/lines)*n.line + width/(2*lines));
        }
    }

    public void draw(GameCanvas canvas){

        //draw outer rectangle
        canvas.drawRect(BL, width, height, borderColor, false);
        //draw our notes
        drawNotes(canvas);
        if(active || goal){

            //If we are in the active or the goal lane, we need to draw the lines
            for(int j = 0; j < lines; ++j){
                //Calculate the x coordinate of this line using the bottom left x coordinate of this lane
                float x2 = BL.x + (j + 1) * width / lines;

                //We might as well also draw the hit bars here as well. Change their color if they are triggered
                Color hc = (triggers[j] && active)? triggerHitColor: triggerColor;
                canvas.drawLine(BL.x + j*width/lines, hitY, x2, hitY, 3, hc);

                //if we are not at the last line, draw a line to divide them from the other lines in this lane
                if(j != lines-1){
                    if(active){
                        //If this is the current lane, draw from the top all the way to the current height
                        canvas.drawLine(x2, BL.y + height, x2, BL.y + height - lineHeight, 3, lineSepColor);
                    }
                    else{
                        //If it is not the current lane, it must be the goal lane, so draw from the bottom up to
                        //current height
                        canvas.drawLine(x2, BL.y + height, x2, BL.y + lineHeight, 3, lineSepColor);

                    }
                }

            }

        }else{
            //If this is not the current or goal lane, just draw the hitbar
            canvas.drawLine(BL.x,hitY, BL.x + width, hitY, 3, Color.NAVY);

        }
        linkCoords[0] = BL.x + width/2;
        linkCoords[1] = BL.y;

        //Draw the filled in fraction of each HP bar with respect to current health. Change the color from green
        //to red if it is low enough.
        canvas.drawRect(BL.x+1, hpy, BL.x + (hpwidth *(comp/maxComp))-1, hpy + hpHeight,comp< maxComp/4? Color.RED : Color.GREEN,true);
        //Draw the outline over the actual filled in portion so we cover the edges
        canvas.drawRect(BL.x, hpy, BL.x + hpwidth, hpy + hpHeight,Color.BLACK,false);
        //Determine the other endpoints of the link lines
        linkCoords[2] = BL.x + hpwidth/2;
        linkCoords[3] = hpy + hpHeight;
        //Draw the link lines
        canvas.drawLine(linkCoords[0],linkCoords[1],linkCoords[2],linkCoords[3],3,Color.BLACK);




    }
    public void drawNotes(GameCanvas canvas){
        for(Note n: notes){
            if(((n.nt == Note.NType.BEAT || n.nt == Note.NType.HELD) && (active || goal)) || (n.nt == Note.NType.SWITCH && (!active && !goal))){
                n.draw(canvas, (width-minWidth)/(maxWidth-minWidth));
            }
        }
    }


}
