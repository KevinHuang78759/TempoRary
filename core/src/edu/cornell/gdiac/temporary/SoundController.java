package edu.cornell.gdiac.temporary;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller for sound effects
 */
public class SoundController {
    private HashMap<String, Sound> effectList;

    private Sound curSound;
    public SoundController(){
        effectList = new HashMap<>();
    }
    public void addSound(String id, String fileName){
        Sound nextSound = Gdx.audio.newSound(Gdx.files.internal(fileName));
        effectList.put(id, nextSound);
    }
    public void playSound(String id){
        if(curSound != null){
            curSound.stop();
        }
        curSound = effectList.get(id);
        curSound.play(0.2f);
    }
    public void dispose(){
        for (Sound sound : effectList.values()) {
            sound.dispose();
        }
        effectList = new HashMap<>();
    }
}
