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

    private static float volumeAdjust;

    private Sound curSound;

    public SoundController(){
        effectList = new HashMap<>();
        volumeAdjust = SaveManager.getInstance().getFXVolume();
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
        curSound.play(vol * volumeAdjust);
    }

    public void dispose(){
        for (Sound sound : effectList.values()) {
            sound.dispose();
        }
        effectList = new HashMap<>();
    }

    /**
     * Adjusts the volume for all sounds effects in SoundController
     * @param vol volume
     */
    public static void setVolumeAdjust(float vol) {
        volumeAdjust = vol;
    }
}
