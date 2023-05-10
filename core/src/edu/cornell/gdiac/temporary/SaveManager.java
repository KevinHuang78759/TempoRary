package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** This class utilizes LibGDX Preferences to store saved games */
public class SaveManager {
    /** Singleton save manager */
    private static SaveManager saveManager;
    /** LibGDX Preferences for all the game's levels */
    private Preferences[] levels;
    /** LibGDX Preferences for user volume settings */
    private Preferences volumeSettings;
    /** LibGDX preferences for hit bindings */
    private Preferences hitBindingSettings;
    /** LibGDX preferences for switch bindings */
    private Preferences[] switchBindingSettings;


    private SaveManager() {
        // TODO: UPDATE THIS WITH A LOOP OF ALL OF OUR LEVELS
        levels = new Preferences[]{Gdx.app.getPreferences("edu.cornell.gdiac.temporary.levels")};
        volumeSettings = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.volume");
        hitBindingSettings = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.hitBindingSettings");
        switchBindingSettings = new Preferences[3];
        for (int i = 2; i <= 4; i++) {
            switchBindingSettings[i-2] = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.switchBindingSettings." + i);
        }
    }

    /**
     * Return the singleton instance of the save manager
     * @return the singleton save manager
     */
    public static SaveManager getInstance() {
        if (saveManager == null) {
            saveManager = new SaveManager();
        }
        return saveManager;
    }

    // TODO: SAVE LEVEL AFTER BEATING IT
    public void saveGame() {

    }

    // TODO: RETRIEVE LEVEL DATA AND GET SIGNATURE
    public void getLevelData() {

    }

    // TODO: SAVE SETTINGS UPON EXIT
    public void saveSettings(float musicVol, float fxVol) {
        volumeSettings.putFloat("music", musicVol);
        volumeSettings.putFloat("fx", fxVol);
    }

    // TODO: RETRIEVE THE KEYBINDINGS THAT USER SAVED
    public int[] getHitKeybindingSettings() {
        int[] temp = new int[4];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = hitBindingSettings.getInteger("i");
        }
        return temp;
    }

    /**
     * Retrieves save data for music volume
     */
    public float getMusicVolume() {
        return volumeSettings.getFloat("music");
    }

    /**
     * Retrieves save data for soundFX volume
     */
    public float getFXVolume() {
        return volumeSettings.getFloat("fx");
    }

    // TODO: CLEAR SAVED DATA
    public void clearSavedData() {

    }

}
