package edu.cornell.gdiac.temporary;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Controller for sound effects
 * @param <T> key type
 */
public class SoundController<T>{

    private HashMap<T, Sound> effectList;

    private Sound curSound;

    public SoundController(){
        effectList = new HashMap<>();
    }

    public void addSound(T id, String fileName){
        Sound nextSound = Gdx.audio.newSound(Gdx.files.internal(fileName));
        effectList.put(id, nextSound);
    }

    public void playSound(T id, float vol){
        if(curSound != null){
            curSound.stop();
        }
        curSound = effectList.get(id);
        curSound.play(vol * MenuMode.getFXVolumeSetting());
    }

    public void dispose(){
        for (Sound sound : effectList.values()) {
            sound.dispose();
        }
        effectList = new HashMap<>();
    }
}
