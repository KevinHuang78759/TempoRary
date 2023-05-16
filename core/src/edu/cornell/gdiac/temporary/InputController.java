/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * This class is a singleton for this application, but we have not designed
 * it as one.  That is to give you some extra functionality should you want
 * to add multiple ships.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.util.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

import java.util.Arrays;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {

	public static final int MAX_LINES_PER_LANE = 4;
	public static final int MAX_BAND_MEMBERS = 4;

	private IntMap<String> masterKeybindingMap;

	// Fields to manage game state
	/** Whether the reset button was pressed. */
	protected boolean resetPressed;
	/** Whether the exit button was pressed. */
	protected boolean exitPressed;

	// Mouse controls
	private boolean mouseLeftClicked;
	private boolean mouseLifted;
	private boolean mouseLast;

	private boolean calibrationHitPressed;
	private boolean calibrationHitJustPressed;
	private boolean calibrationHitLast;

	// Controls for the game
	// TODO SWITCH THIS TO ARRAYS
	// TODO REMOVE STATIC FIELDS
	public static IntMap<int[]> switchesBindingsMain;
	public static IntMap<int[]> switchesBindingsAlt;

	public static int[] triggerBindingsMain;
	public static int[] triggerBindingsAlt;

	//Arrays to registering switch and trigger presses
	//We need to track their previous values so that we dont register a hold as repeated clicks
	boolean[] triggerPress;
	boolean[] triggerLifted;
	private static boolean[] triggers;
	private boolean[] triggerLast;
	private static boolean[] switches;
	private boolean[] switchesLast;
	private boolean[] moves;

	/** XBox Controller support */
	private XBoxController xbox;

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() {
		return resetPressed;
	}

	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() { return exitPressed; }

	/**
	 * Returns true if the calibration hit button was pressed.
	 *
	 * @return true if calibration hit button was pressed
	 */
	public boolean didCalibrationPress() {
		return calibrationHitPressed;
	}

	/**
	 * Returns true if calibration hit button was just hit
	 * @return true if calibration hit button was just hit
	 */
	public boolean didCalibrationHit() {
		return calibrationHitJustPressed;
	}

	/**
	 * Returns true if left mouse button was released
	 * @return true if left mouse button was released
	 */
	public boolean didMouseLift() {
		return mouseLifted;
	}

	/** Returns an array that represents if the switch lane keys are being pressed */
	public boolean[] didSwitch() {
		return switches;
	}

	/** Returns an array that represents if the note hit trigger keys are being pressed  */
	public boolean[] didTrigger(){
		return triggers;
	}

	/** Returns x coordinate of mouse */
	public float getMouseX() {
		return Gdx.input.getX();
	}

	/** Returns y coordinate of mouse */
	public float getMouseY() {
		return Gdx.input.getY();
	}

	/** The singleton instance of the input controller */
	private static InputController theController = null;

	/**
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}

	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > 0) {
			xbox = controllers.get(0);
		} else {
			xbox = null;
		}
		clicking = false;
		typed = false;
		// we hardcode 4 for this because this is now the specification for the game
		triggerLast = new boolean[4];
		switchesLast = new boolean[4];
		triggers = new boolean[4];
		switches = new boolean[4];
		triggerLifted = new boolean[4];
		triggerBindingsMain = SaveManager.getInstance().getHitKeybindingSettings(new int[]{
				Input.Keys.D,
				Input.Keys.F,
				Input.Keys.J,
				Input.Keys.K
		}, true);
		triggerBindingsAlt = SaveManager.getInstance().getHitKeybindingSettings(new int[]{
				Input.Keys.LEFT,
				Input.Keys.DOWN,
				Input.Keys.UP,
				Input.Keys.RIGHT
		}, false);
		// DEFAULT PRESETS!
		int[] switchesBindingsMainAll = new int[]{
				Input.Keys.E,
				Input.Keys.R,
				Input.Keys.U,
				Input.Keys.I,
		};
		int[] switchesBindingsAltAll = new int[]{
				Input.Keys.NUM_1,
				Input.Keys.NUM_2,
				Input.Keys.NUM_3,
				Input.Keys.NUM_4,
		};
		switchesBindingsMain = new IntMap<>();
		switchesBindingsAlt = new IntMap<>();
		for (int i = 0; i < 4; i++) {
			switchesBindingsMain.put(i, Arrays.copyOfRange(switchesBindingsMainAll, 0, i + 1));
			switchesBindingsAlt.put(i, Arrays.copyOfRange(switchesBindingsAltAll, 0, i + 1));
		}
		switchesBindingsMain = SaveManager.getInstance().getSwitchKeybindingSettings(switchesBindingsMain, true);
		switchesBindingsAlt = SaveManager.getInstance().getSwitchKeybindingSettings(switchesBindingsAlt, false);
		masterKeybindingMap = new IntMap<>();
		// fill up keybinding map
		// TODO: TURN THIS INTO CONSTANTS
		for (int val : triggerBindingsMain) {
			masterKeybindingMap.put(val, "triggerMain");
		}
		for (int val : triggerBindingsAlt) {
			masterKeybindingMap.put(val, "triggerAlt");
		}
		for (int val : switchesBindingsMainAll) {
			masterKeybindingMap.put(val, "switchesMain");
		}
		for (int val : switchesBindingsAltAll) {
			masterKeybindingMap.put(val, "switchesAlt");
		}
	}

	// TODO: FIX THIS
	/**
	 * Sets bindings to -1 if there are multiple bindings
	 * @param binding Input.Key integer binding
	 * @param lane
	 */
	private void updateKeybindingMap(int binding, String newPossessor, int numBandMembers) {
		String bindingObj = masterKeybindingMap.get(binding);
		if (bindingObj != null) {
			switch (bindingObj) {
				case "switchesMain":
					for (IntMap.Entry<int[]> kv : switchesBindingsMain) {
						if (!bindingObj.equals(newPossessor) || kv.key == numBandMembers) {
							int[] arr = switchesBindingsMain.get(kv.key);
							for (int i = 0; i < arr.length; i++) {
								if (arr[i] == binding) {
									arr[i] = -99;
								}
							}
						}
					}
					break;
				case "switchesAlt":
					for (IntMap.Entry<int[]> kv : switchesBindingsAlt) {
						if (!bindingObj.equals(newPossessor) || kv.key == numBandMembers) {
							int[] arr = switchesBindingsAlt.get(kv.key);
							for (int i = 0; i < arr.length; i++) {
								if (arr[i] == binding) {
									arr[i] = -99;
								}
							}
						}
					}
					break;
				case "triggerMain":
					for (int i = 0; i < triggerBindingsMain.length; i++) {
						if (triggerBindingsMain[i] == binding) {
							triggerBindingsMain[i] = -99;
						}
					}
					break;
				case "triggerAlt":
					for (int i = 0; i < triggerBindingsAlt.length; i++) {
						if (triggerBindingsAlt[i] == binding) {
							triggerBindingsAlt[i] = -99;
						}
					}
					break;
			}
		}
		// add new binding
		masterKeybindingMap.put(binding, newPossessor);
	}

	/**
	 *
	 * @param numBandMembers number of band members for the new keybindings to change
	 * @param lane number of which lane to change the keybinding for, must be < numBandMembers
	 * @param newKeybind new keybinding to set
	 * @param main true if setting the main keybindings, else set the alt binding
	 */
	public void setKeybinding(int numBandMembers, int lane, int newKeybind, boolean main) {
		assert lane < numBandMembers && lane >= 0;
		updateKeybindingMap(newKeybind, main ? "switchesMain" : "switchesAlt", numBandMembers);
		if (main)
			switchesBindingsMain.get(numBandMembers)[lane] = newKeybind;
		else
			switchesBindingsAlt.get(numBandMembers)[lane] = newKeybind;
	}

	// keybindings for hitting the notes (triggers)
	public void setKeybinding(int line, int newKeybind, boolean main) {
		updateKeybindingMap(newKeybind, main ? "triggerMain" : "triggerAlt", -1);
		if (main)
			triggerBindingsMain[line] = newKeybind;
		else
			triggerBindingsAlt[line] = newKeybind;
	}

	// READ INPUT FUNCTIONS
	/**
	 * readInput for screens that don't need band member switching information but may need xbox support
	 * i.e. calibration
	 * */
	public void readInput() {
		readInput(4);
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 */
	public void readInput(int numBandMembers) {
		// Check to see if a GamePad is connected
		if (xbox != null && xbox.isConnected()) {
			readGamepad();
			readKeyboard(true, numBandMembers); // Read as a back-up
		} else {
			readKeyboard(false, numBandMembers);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 */
	private void readGamepad() {
		// TODO: Map gamepad inputs
		resetPressed = xbox.getA();
		exitPressed  = xbox.getBack();
	}

	// READ KEYBOARD FOR THE GAME (not level editor)

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(boolean secondary, int numBandMembers) {
		// Give priority to gamepad results, get input from keyboard
		resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.ENTER));
		exitPressed = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		calibrationHitPressed = (secondary && calibrationHitPressed) || Gdx.input.isKeyPressed(Input.Keys.SPACE);

		calibrationHitJustPressed = !calibrationHitLast && calibrationHitPressed;
		calibrationHitLast = calibrationHitPressed;

		triggerPress = new boolean[4];
		boolean[] switchesPress = new boolean[4];

		for (int i = 0; i < triggerBindingsMain.length; i++) {
			triggerPress[i] = Gdx.input.isKeyPressed(triggerBindingsMain[i]) || Gdx.input.isKeyPressed(triggerBindingsAlt[i]);
			if (i < numBandMembers) {
				switchesPress[i] = Gdx.input.isKeyPressed(switchesBindingsMain.get(numBandMembers - 1)[i])
						|| Gdx.input.isKeyPressed(switchesBindingsAlt.get(numBandMembers - 1)[i]);
			}
		}

		//Compute actual values by comparing with previous value. We only register a click if the trigger or switch
		// went from false to true. We only register a lift if the trigger went from true to false.
		for (int i = 0; i < Math.max(switchesPress.length, triggerPress.length); ++i){
			if (i < triggers.length){
				triggers[i] = !triggerLast[i] && triggerPress[i];
				triggerLifted[i] = triggerLast[i] && !triggerPress[i];
				triggerLast[i] = triggerPress[i];
			}
			if (i < switches.length) {
				switches[i] = !switchesLast[i] && switchesPress[i];
				switchesLast[i] = switchesPress[i];
			}
		}

		// get mouse input
		boolean mousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

		mouseLeftClicked = !mouseLast && mousePressed;
		mouseLifted = mouseLast && !mousePressed;
		mouseLast = mousePressed;
	}

	/**
	 * Returns the current key bindings for switching
	 *
	 * @return
	 */
	public static String[] switchKeyBinds(int numBandMembers, boolean main) {
		String[] bindings = new String[numBandMembers + 1];
		for (int i = 0; i < numBandMembers + 1; i++) {
			if (switchesBindingsMain.get(numBandMembers)[i] >= 0 && main) {
				bindings[i] = Input.Keys.toString(switchesBindingsMain.get(numBandMembers)[i]);
			} else if (switchesBindingsAlt.get(numBandMembers)[i] >= 0 && !main) {
				bindings[i] = Input.Keys.toString(switchesBindingsAlt.get(numBandMembers)[i]);
			}
			else {
				bindings[i] = "";
			}
		}
		return bindings;
	}

	/**
	 * Returns the current key bindings for note hitting
	 * @return
	 */
	public static String[] triggerKeyBinds(boolean main) {
		String[] bindings = new String[triggers.length];
		for (int i = 0; i < bindings.length; i++) {
			if (triggerBindingsMain[i] >= 0 && main) {
				bindings[i] = Input.Keys.toString(triggerBindingsMain[i]);
			} else if (triggerBindingsAlt[i] >= 0 && !main) {
				bindings[i] = Input.Keys.toString(triggerBindingsAlt[i]);
			} else {
				bindings[i] = "";
			}
		}
		return bindings;
	}


	// START LEVEL EDITOR EXCLUSIVE KEYBINDINGS AND VARIABLES

	private char characterTyped;
	private boolean typed;

	private float mouseX;
	private float mouseY;
	public float mouseMoveX;
	public float mouseMoveY;
	public boolean mouseMoved;
	public boolean mouseClicked;

	private boolean erased;
	private boolean erasedLast;

	private boolean undid;
	private boolean undidLast;

	private boolean redid;
	private boolean redidLast;

	private boolean play;
	private boolean playPress;
	private boolean playLast;

	private boolean track;
	private boolean trackLast;

	private boolean save;
	private boolean saveLast;

	private boolean load;
	private boolean loadLast;

	private boolean upDuration;
	private boolean upDurationLast;
	private boolean downDuration;
	private boolean downDurationLast;

	private boolean placeStart;
	private boolean placeStartLast;

	private boolean placeFlags;
	private boolean placeFlagsLast;

	private boolean placeHits;
	private boolean placeHitsLast;

	public class Processor implements InputProcessor {

		public boolean keyDown (int keycode){
			return false;
		}

		public boolean keyUp (int keycode){
			return false;
		}

		public boolean keyTyped (char character){
			characterTyped = character;
			typed = true;
			return false;
		}

		public boolean touchDown (int x, int y, int pointer, int button){
			mouseClicked = true;
			mouseX = x;
			mouseY = y;
			return false;
		}

		public boolean touchUp (int x, int y, int pointer, int button){
			clicking = false;
			mouseClicked = false;
			mouseX = x;
			mouseY = y;
			return false;
		}

		public boolean touchDragged (int x, int y, int pointer){
			return false;
		}

		public boolean mouseMoved (int x, int y){
			mouseMoveX = x;
			mouseMoveY = y;
			mouseMoved = true;
			return false;
		}

		public boolean scrolled (float amountX, float amountY){
			return false;
		}
	}

	private Processor processor = new Processor();

	/**
	 * Reads the keyboard input specifically for the level editor
	 */
	public void readKeyboardLevelEditor() {
		exitPressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);

		moves = new boolean[]{
				Gdx.input.isKeyPressed(Input.Keys.UP),
				Gdx.input.isKeyPressed(Input.Keys.DOWN),
				Gdx.input.isKeyPressed(Input.Keys.LEFT),
				Gdx.input.isKeyPressed(Input.Keys.RIGHT)
		};

		boolean erasedPress = Gdx.input.isKeyPressed(Input.Keys.E);
		boolean undidPress = Gdx.input.isKeyPressed(Input.Keys.U);
		boolean redidPress = Gdx.input.isKeyPressed(Input.Keys.R);
		playPress = Gdx.input.isKeyPressed(Input.Keys.SPACE);
		boolean trackPress = Gdx.input.isKeyPressed(Input.Keys.V);
		boolean placeStartPress = Gdx.input.isKeyPressed(Input.Keys.P);
		boolean placeFlagsPress = Gdx.input.isKeyPressed(Input.Keys.C);
		boolean placeHitsPress = Gdx.input.isKeyPressed(Input.Keys.X);
		boolean savePress = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.S);
		boolean loadPress = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.L);
		boolean upDurationPress = Gdx.input.isKeyPressed(Input.Keys.NUM_9);
		boolean downDurationPress = Gdx.input.isKeyPressed(Input.Keys.NUM_8);

		erased = !erasedLast && erasedPress;
		erasedLast = erasedPress;

		undid = !undidLast && undidPress;
		undidLast = undidPress;

		redid = !redidLast && redidPress;
		redidLast = redidPress;

		play = !playLast && playPress;
		playLast = playPress;

		track = !trackLast && trackPress;
		trackLast = trackPress;

		save = !saveLast && savePress;
		saveLast = savePress;

		load = !loadLast && loadPress;
		loadLast = loadPress;

		upDuration = !upDurationLast && upDurationPress;
		upDurationLast = upDurationPress;

		downDuration = !downDurationLast && downDurationPress;
		downDurationLast = downDurationPress;

		placeStart = !placeStartLast && placeStartPress;
		placeStartLast = placeStartPress;

		placeFlags = !placeFlagsLast && placeFlagsPress;
		placeFlagsLast = placeFlagsPress;

		placeHits = !placeHitsLast && placeHitsPress;
		placeHitsLast = placeHitsPress;
	}

	// getter methods input reading in level editor

	private boolean clicking;

	public boolean didClick() {
		if (!clicking && mouseClicked) {
			clicking = true;
			return true;
		}
		return false;
	}

	public boolean didType() {
		if (typed){
			typed = false;
			return true;
		} else {
			return false;
		}
	}

	public boolean[] getMoves(){
		return moves;
	}

	public char getCharTyped() {return characterTyped;}

	public boolean didErase() {return erased;}

	public boolean didSetQuarter() {return Gdx.input.isKeyPressed(Input.Keys.Q);}
	public boolean didSetThird() {return Gdx.input.isKeyPressed(Input.Keys.T);}
	public boolean didSetFree() {return Gdx.input.isKeyPressed(Input.Keys.F);}

	public boolean didPrecision1() {return Gdx.input.isKeyPressed(Input.Keys.NUM_1);}
	public boolean didPrecision2() {return Gdx.input.isKeyPressed(Input.Keys.NUM_2);}
	public boolean didPrecision3() {return Gdx.input.isKeyPressed(Input.Keys.NUM_3);}

	public boolean setSwitchNotes() {return Gdx.input.isKeyPressed(Input.Keys.S) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);}
	public boolean setBeatNotes() {return Gdx.input.isKeyPressed(Input.Keys.B);}
	public boolean setHeldNotes() {return Gdx.input.isKeyPressed(Input.Keys.H);}

	public boolean didPressPlay() {return play;}
	public boolean didPressPlay(boolean relocate) {
		if (!relocate) {
			return play && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
		}
		else {
			return play && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
		}
	}
	public boolean didHoldPlay() {return playPress;}

	public boolean didPressTrack() {return track;}

	public boolean didResetSong() {return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT);}

	public boolean didSpeedUp() {return Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);}

	public boolean didUndo() {return undid;}

	public boolean didRedo() {return redid;}

	public boolean pressedPlaceStart() {return placeStart;}

	public boolean pressedPlaceHits() {return placeHits;}

	public boolean pressedPlaceFlags() {return placeFlags;}

	public boolean didSave() {return save;}

	public boolean didLoad() {return load;}

	public boolean durationUp() {return upDuration;}

	public boolean durationDown() {return downDuration;}

	public boolean finishedTyping() {return Gdx.input.isKeyPressed(Input.Keys.ENTER);}

	public void setEditorProcessor() {
		Gdx.input.setInputProcessor(processor);
	}

}
