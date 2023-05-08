package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** This class utilizes LibGDX Preferences to store saved games */
public class SaveManager {
    // TODO: ADD CLASS THAT USES CLASSES FOR THE DATA FROM ARCHSPEC?
    /** Singleton save manager */
    private static SaveManager saveManager;
    /** Preferences for best times */
    private final Preferences[] levels;
    /** Preferences for user settings */
    private final Preferences settings;

    private SaveManager() {
        // TODO: UPDATE THIS WITH A LOOP OF ALL OF OUR LEVELS
        this.levels = new Preferences[]{Gdx.app.getPreferences("edu.cornell.gdiac.temporary.levels")};
        this.settings = Gdx.app.getPreferences("edu.cornell.gdiac.temporary.settings");
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
    public void saveSettings() {

    }

    // TODO: RETRIEVE THE KEYBINDINGS THAT USER SAVED
    public int[] getKeybindingSettings() {
        return null;
    }

    // TODO: RETRIEVE MUSIC VOLUME
    public float getMusicVolume() {
        return 0;
    }

    // TODO: RETRIEVE SOUND FX VOLUME
    public float getFXVolume() {
        return 0;
    }

    // TODO: CLEAR SAVED DATA
    public void clearSavedData() {

    }

}
