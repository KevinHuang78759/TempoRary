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
    public SoundController(){
        effectList = new HashMap<>();
    }
    public void addSound(String id, String fileName){
        Sound nextSound = Gdx.audio.newSound(Gdx.files.internal(fileName));
        effectList.put(id, nextSound);
    }
    public void playSound(String id){
        effectList.get(id).play(0.25f);
    }
    public void dispose(){
        for (Sound sound : effectList.values()) {
            sound.dispose();
        }
        effectList = new HashMap<>();
    }
}
