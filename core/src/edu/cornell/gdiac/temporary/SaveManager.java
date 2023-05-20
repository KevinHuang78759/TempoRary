package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Arrays;

/** This class utilizes LibGDX Preferences to store saved games */
// TODO: ADD CALIBRATION VALUE
public class SaveManager {
    /** Singleton save manager */
    private static SaveManager saveManager;
    /** LibGDX Preferences for all the game's levels */
    private Preferences levels;
    /** LibGDX Preferences for user volume settings (and calibration) */
    private Preferences volumeSettings;
    /** LibGDX preferences for hit bindings */
    private Preferences hitBindingSettings;
    /** LibGDX preferences for switch bindings */
    private Preferences[] switchBindingSettings;

    private SaveManager() {
        levels = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.levels");
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

    public void saveGame(String levelName, long score, String grade, long maxCombo) {
        if (score >= levels.getLong(levelName+".highScore", 0)) {
            levels.putLong(levelName+".highScore", score);
            levels.putString(levelName+".grade", grade);
            levels.putLong(levelName+".maxCombo", maxCombo);
            levels.flush();
        }
    }

    // currently just returns the high score for now
    public long getHighScore(String levelName) {
        return levels.getLong(levelName+".highScore", 0);
    }

    public String getGrade(String levelName) {
        return levels.getString(levelName+".grade", "");
    }

    public long getHighestCombo(String levelName) {
        return levels.getLong(levelName+".maxCombo", 0);
    }

    public void saveSettings(int[] hitBindings, IntMap<int[]> switchBindings, float musicVol, float fxVol) {
        for (int i = 0; i < hitBindings.length; i++) {
            hitBindingSettings.putInteger("main." + i, hitBindings[i]);
        }
        for (int i = 0; i < InputController.MAX_BAND_MEMBERS; i++) {
            for (int j = 0; j < i + 1; j++) {
                switchBindingSettings[i].putInteger("main." + j, switchBindings.get(i)[j]);
            }
            switchBindingSettings[i].flush();
        }
        volumeSettings.putFloat("music", musicVol);
        volumeSettings.putFloat("fx", fxVol);
        hitBindingSettings.flush();
        volumeSettings.flush();
    }

    public void saveCalibration(int offset) {
        volumeSettings.putInteger("calibration", offset);
        volumeSettings.flush();
    }

    // MEANT TO BE CALLED ONCE WHEN INITIALIZING SETTINGS
    public int[] getHitKeybindingSettings(int[] def) {
        int[] temp = new int[InputController.MAX_LINES_PER_LANE];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = hitBindingSettings.getInteger("main." + i, -2);
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
                curr[j] = switchBindingSettings[i].getInteger("main." + j, -2);
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

    /**
     * Retrieves saved calibration offset data
     */
    public int getCalibrationOffset() {
        return volumeSettings.getInteger("calibration", 0);
    }


    public void resetSettings() {
        volumeSettings.clear();
        hitBindingSettings.clear();
        for (Preferences s:
             switchBindingSettings) {
            s.clear();
            s.flush();
        }
    }
}
