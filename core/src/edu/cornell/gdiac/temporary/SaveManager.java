package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.IntMap;

import java.util.Arrays;

/** This class utilizes LibGDX Preferences to store saved games */
// TODO: ADD CALIBRATION VALUE
public class SaveManager {
    /** Singleton save manager */
    private static SaveManager saveManager;
    /** LibGDX Preferences for all the game's levels */
    // TODO: UPDATE THIS TYPE WITH A COLLECTION
    private Preferences levels;
    /** LibGDX Preferences for user volume settings */
    private Preferences volumeSettings;
    /** LibGDX preferences for hit bindings */
    private Preferences hitBindingSettings;
    /** LibGDX preferences for switch bindings */
    private Preferences[] switchBindingSettings;

    private SaveManager() {
        // TODO: UPDATE THIS WITH A LOOP OF ALL OF OUR LEVELS
        levels = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.levels");
        volumeSettings = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.volume");
        hitBindingSettings = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.hitBindingSettings");
        switchBindingSettings = new Preferences[InputController.MAX_BAND_MEMBERS];
        for (int i = 0; i < InputController.MAX_BAND_MEMBERS; i++) {
            switchBindingSettings[i] = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings.switchBindingSettings." + i);
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
    public void saveGame(String levelName, int score) {

    }

    // TODO: RETRIEVE LEVEL DATA AND GET SIGNATURE
    public void getLevelData() {

    }

    // TODO: SAVE SETTINGS UPON EXIT
    public void saveSettings(int[] hitBindings, IntMap<int[]> switchBindings, float musicVol, float fxVol) {
        for (int i = 0; i < hitBindings.length; i++) {
            hitBindingSettings.putInteger("" + i, hitBindings[i]);
        }
        for (int i = 0; i < InputController.MAX_BAND_MEMBERS; i++) {
            for (int j = 0; j < i + 1; j++) {
                switchBindingSettings[i].putInteger("" + j, switchBindings.get(i)[j]);
            }
            switchBindingSettings[i].flush();
        }
        volumeSettings.putFloat("music", musicVol);
        volumeSettings.putFloat("fx", fxVol);
        hitBindingSettings.flush();
        volumeSettings.flush();
    }

    // MEANT TO BE CALLED ONCE WHEN INITIALIZING SETTINGS
    public int[] getHitKeybindingSettings(int[] def) {
        int[] temp = new int[InputController.MAX_LINES_PER_LANE];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = hitBindingSettings.getInteger("" + i, -2);
            if (temp[i] == -2) {
                return def;
            }
        }
        return temp;
    }

    public IntMap<int[]> getSwitchKeybindingSettings(IntMap<int[]> def) {
        IntMap<int[]> temp = new IntMap<>();
        for (int i = 0; i < InputController.MAX_BAND_MEMBERS; i++) {
            int[] curr = new int[i + 1];
            for (int j = 0; j < curr.length; j++) {
                curr[j] = switchBindingSettings[i].getInteger("" + j, -2);
                if (curr[j] == -2) {
                    return def;
                }
            }
            temp.put(i, curr);
        }
        return temp;
    }

    /**
     * Retrieves save data for music volume
     */
    public float getMusicVolume() {
        return volumeSettings.getFloat("music", 1);
    }

    /**
     * Retrieves save data for soundFX volume
     */
    public float getFXVolume() {
        return volumeSettings.getFloat("fx", 1);
    }

    // TODO: CLEAR SAVED DATA
    public void clearSavedData() {

    }

}
