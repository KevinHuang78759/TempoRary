package edu.cornell.gdiac.temporary;

/**
 * This class contains exit codes for screen switching.
 * The format is TO_[mode], where mode is the mode (maybe not necessarily a controller class) you are going to
 * The number should not conflict with anything else (i.e. don't make variables the same number)
 * Whenever you need to add a state to switch to, add it here
 */
public class ExitCode {
    // exit out of game
    public static final int TO_EXIT = 0;
    // exit to playing
    public static final int TO_PLAYING = 1;
    // exit to calibration mode
    public static final int TO_CALIBRATION = 2;
    // exit to editor
    public static final int TO_EDITOR = 3;
    // exit to menu
    public static final int TO_MENU = 4;
}
