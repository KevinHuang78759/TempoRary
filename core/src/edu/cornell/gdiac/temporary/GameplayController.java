/*
 * GameplayController.java
 *
 * For many of you, this class will seem like the most unusual one in the entire project.
 * It implements a lot of functionality that looks like it should go into the various
 * GameObject subclasses. However, a lot of this functionality involves the creation or
 * destruction of objects.  We cannot do this without a lot of cyclic dependencies,
 * which are bad.
 *
 * You will notice that gameplay-wise, most of the features in this class are
 * interactions, not actions. This demonstrates why a software developer needs to
 * understand the difference between these two.
 *
 * You will definitely need to modify this file in Part 2 of the lab. However, you are
 * free to modify any file you want.  You are also free to add new classes and assets.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.temporary.entity.*;

/**
 * Controller to handle gameplay interactions.
 * This controller also acts as the root class for all the models.
 */
public class GameplayController {

	// Graphics assets for the entities
	/** Texture for all stars, as they look the same */
	private Texture particleTexture;

	private Texture enhancedParticle;

	private static final float GLOBAL_VOLUME_ADJ = 0.2f;

	/** Maximum amount of health */
	final int MAX_HEALTH = 30;
	/** Number of band member lanes */
	int NUM_LANES;

	int HIT_IND_SIZE = 100;

	private Texture perfectHitIndicator;
	private Texture goodHitIndicator;

	private Texture okIndicator;

	private Texture missIndicator;




	// List of objects with the garbage collection set.
	/** The currently active object */
	private Array<Particle> particles;

	private Array<Particle> noteIndicatorParticles;
	/** The backing set for garbage collection */
	private Array<Particle> backing;

	/** Index of the currently active band member */
	public int activeBandMember;
	/** Level object, stores bandMembers */
	public Level level;
	/** The Y coordinate at which a note will spawn. Notes should spawn completely invisible. */
	public float noteSpawnY;
	/**
	 * The y coordinate at which notes are considered "out of bounds." By the time a note reaches this y value
	 * it should already be completely invisible.
	 */
	public float noteDieY;
	/** The calibration offset (int samples */
	public int offset;
	/** Base offset for leniency in samples */
	private int baseLeniency;

	/** The minimum x value margin */
	public float LEFTBOUND;
	/** The maximum x value margin */
	public float RIGHTBOUND;
	/** The maximum y value margin */
	public float TOPBOUND;
	/** The minimum y value margin */
	public float BOTTOMBOUND;
	/** Width of an inact band member's lane */
	public float smallwidth;
	/** Width of an active band member's lane */
	public float largewidth;
	/** Width between each band member lane */
	public float inBetweenWidth;
	/** Y value of the hit area*/
	public float hitY;

	public SoundController<String> sfx;

	public Scoreboard sb;

	/**
	 * Create gameplaycontroler
	 * @param width
	 * @param height
	 */
	public GameplayController(float width, float height){
		particles = new Array<>();
		backing = new Array<>();
		noteIndicatorParticles =new Array<>();
		//Set margins so there is a comfortable amount of space between play area and screen boundaries
		//Values decided by pure look
		setBounds(width, height);
		sfx = new SoundController<>();
		//populate sound effects
		JsonReader jr = new JsonReader();
		JsonValue allSounds = jr.parse(Gdx.files.internal("assets.json"));
		allSounds = allSounds.get("soundEffects");
		for(int i = 0; i < allSounds.size; ++i){
			JsonValue cur = allSounds.get(i);
			sfx.addSound(cur.getString(0), cur.getString(1));
		}
	}

	float totalWidth;
	float totalHeight;

	public void setBounds(float width, float height){
		//Ratio of play area width to play area height
		float playAreaRatio = 2f;
		totalHeight = height;
		totalWidth = width;
		LEFTBOUND = width/8f;
		RIGHTBOUND = 7*width/8f;
		TOPBOUND = 17f*height/20f;
		BOTTOMBOUND = height/3f;

		if((RIGHTBOUND - LEFTBOUND)/(TOPBOUND - BOTTOMBOUND) > playAreaRatio){
			//If this is greater, then we are too wide, so we keep the height but scale the width
			float playWidth = playAreaRatio*(TOPBOUND - BOTTOMBOUND);
			float wCenter = (RIGHTBOUND + LEFTBOUND)/2f;
			RIGHTBOUND = wCenter + playWidth/2f;
			LEFTBOUND = wCenter - playWidth/2f;
		}
		else{
			//Otherwise we are too narrow, so keep the width and scale the height
			float playHeight = (RIGHTBOUND - LEFTBOUND)/playAreaRatio;
			float hCenter = (TOPBOUND + BOTTOMBOUND)/2f;
			TOPBOUND = hCenter + playHeight/2f;
			BOTTOMBOUND = hCenter - playHeight/2f;
		}

	}

	public void setWidths(){
		smallwidth = (RIGHTBOUND - LEFTBOUND)/(NUM_LANES - 1 + (NUM_LANES - 1)*0.25f + 4);
		inBetweenWidth = smallwidth/4f;
		largewidth = 4f*smallwidth;
	}

	public void setYVals(){
		//instantiate other variables
		noteSpawnY = TOPBOUND + smallwidth/2;
		noteDieY = BOTTOMBOUND - smallwidth/2;
		hitY = BOTTOMBOUND + smallwidth/2f;
		level.setBandMemberHitY(hitY);
	}

	public void setFxVolume(float fxVolume) {
		sfx.setVolumeAdjust(fxVolume);
		sb.setVolume(fxVolume);
	}
	/**
	 * Loads a level
	 */
	public void loadLevel(JsonValue levelData, AssetDirectory directory){
		sb = new Scoreboard(4, new int[]{1, 2, 3, 5}, new long[]{10, 20, 30});
		sb.setFontScale((totalHeight - TOPBOUND)/2f);
		particles = new Array<>();
		backing = new Array<>();
		level = null;
		level = new Level(levelData, directory);
		NUM_LANES = level.getBandMembers().length;
		// 70 is referring to ms
		perfectHit = (int) ((0.05f) * level.getMusic().getSampleRate());
		goodHit = (int) ((0.08f) * level.getMusic().getSampleRate());
		okHit = (int) ((0.12f) * level.getMusic().getSampleRate());
		miss = (int) ((0.18f) * level.getMusic().getSampleRate());

		//The space in between two lanes is 1/4 the width of a small lane
		//the width of the large lane is 6x the width of a small lane
		//In total, we have NUM_LANES - 1 small lanes, 1 large lane, and n - 1 in between segments
		//Therefore, the width of the small lane shall be 1/(5NUM_LANES/4 + 35/4) of the available total width
		//Values decided by pure look
		setWidths();
		//initiate default active band member to 0
		activeBandMember = 0;
		setYVals();
		switches = new boolean[NUM_LANES];
		triggers = new boolean[lpl];
		T_SwitchPhases = level.getSamplesPerBeat()/2;
		activeBandMember = 0;
		goalBandMember = 0;
	}

	public void reloadLevel(){
		sb.resetScoreboard();
		sb.setFontScale((totalHeight - TOPBOUND)/2f);
		particles = new Array<>();
		backing = new Array<>();
		level.resetLevel();
		NUM_LANES = level.getBandMembers().length;
		// 70 is referring to ms
		perfectHit = (int) ((0.05f) * level.getMusic().getSampleRate());
		goodHit = (int) ((0.08f) * level.getMusic().getSampleRate());
		okHit = (int) ((0.12f) * level.getMusic().getSampleRate());
		miss = (int) ((0.18f) * level.getMusic().getSampleRate());
		//The space in between two lanes is 1/4 the width of a small lane
		//the width of the large lane is 6x the width of a small lane
		//In total, we have NUM_LANES - 1 small lanes, 1 large lane, and n - 1 in between segments
		//Therefore, the width of the small lane shall be 1/(5NUM_LANES/4 + 35/4) of the available total width
		//Values decided by pure look
		setWidths();
		//initiate default active band member to 0
		activeBandMember = 0;
		setYVals();
		switches = new boolean[NUM_LANES];
		triggers = new boolean[lpl];
		activeBandMember = 0;
		goalBandMember = 0;
	}

	/**
	 * Resizing screen to new width and height
	 */
	public void resize(int width, int height){
		setBounds(width, height);
		setWidths();
		setYVals();
		updateBandMemberCoords();
		sb.setFontScale((totalHeight - TOPBOUND)/2f);

	}
	/**
	 * Sets the offset in determining beat calculation, converting it to samples
	 * @param offset offset from CalibrationMode in milliseconds
	 */
	public void setOffset(int offset) {
		// need to convert to seconds first
		this.offset = (int) (((float) offset / 1000) * level.getMusic().getSampleRate());
	}

	/**
	 * Sets up the colors, max competency, lines and other default values as well as creates each band member object
	 */
	public void setupBandMembers(){
		level.setActiveProperties(activeBandMember, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND);
		level.setBandMemberBl(new Vector2(LEFTBOUND, BOTTOMBOUND), inBetweenWidth);
	}

	/**
	 * Check for dead notes and out of bounds notes. Competency incremeting due to
	 * destroyed notes is also done in here.
	 */
	public void checkDeadNotes(){
		for(int i = 0; i < level.getBandMembers().length; ++i){
			for(Note n : level.getBandMembers()[i].getHitNotes()){
				//If a note is out of bounds and it has not been hit, we need to mark it destroyed and assign
				//a negative hit status
				if(n.getY() < noteDieY && !n.isDestroyed()){
					if (n.getNoteType() == Note.NoteType.HELD) {
					}
					n.setDestroyed(true);
					if(i == activeBandMember){
						sb.resetCombo();
						float hitStatusX = LEFTBOUND+((activeBandMember+1)*(HIT_IND_SIZE/3))+((activeBandMember+1) * smallwidth);
						float hitStatusY = BOTTOMBOUND-(HIT_IND_SIZE/1.6f);
						spawnHitIndicator(hitStatusX,hitStatusY,missIndicator,1);
					}
				}
				if(n.isDestroyed()){
					//if this note is destroyed we need to increment the competency of the
					//lane it was destroyed in by its hitstatus
					if(i == activeBandMember || i == goalBandMember){
						level.getBandMembers()[i].compUpdate(n.getHitStatus());
					}
				}
			}

			for(Note n : level.getBandMembers()[i].getSwitchNotes()){
				//If a note is out of bounds and it has not been hit, we need to mark it destroyed and assign
				//a negative hit status
				if(n.getY() < noteDieY && !n.isDestroyed()){
					n.setHitStatus(0);
					n.setDestroyed(true);
				}
				if(n.isDestroyed()){
					//if this note is destroyed we need to increment the competency of the
					//lane it was destroyed in by its hitstatus
					if(i == activeBandMember || i == goalBandMember){
						level.getBandMembers()[i].compUpdate(n.getHitStatus());
					}
				}
			}
		}
	}


	/**
	 * Check if the player has won the game.
	 * @return true if there are no more active and loading notes,
	 * and all band members have competency bar > 0.
	 */
	public boolean checkWinCon(){
		return (!level.isMusicPlaying() || !level.hasMoreNotes()) && particles.isEmpty();
	}

	/**
	 * Helper function to determine whether a band member has 0 competency for losing
	 * @return returns if at least bone band members has 0 competency
	 */
	public boolean hasZeroCompetency() {
		for (BandMember bandMember : level.getBandMembers()) {
			if (bandMember.getCurComp() == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Populates this mode from the given the directory.
	 *
	 * The asset directory is a dictionary that maps string keys to assets.
	 * Assets can include images, sounds, and fonts (and more). This
	 * method delegates to the gameplay controller
	 *
	 * @param directory 	Reference to the asset directory.
	 */
	public void populate(AssetDirectory directory) {
		particleTexture = directory.getEntry("quaver", Texture.class);
		enhancedParticle = directory.getEntry("doubleQ", Texture.class);

		perfectHitIndicator = directory.getEntry("perfect-hit", Texture.class);
		goodHitIndicator = directory.getEntry("good-hit", Texture.class);
		okIndicator = directory.getEntry("ok-hit", Texture.class);
		missIndicator = directory.getEntry("miss-hit", Texture.class);

	}

	/**
	 * Returns the list of the currently active (not destroyed) game objects
	 *
	 * As this method returns a reference and Lists are mutable, other classes can
	 * technical modify this list.  That is a very bad idea.  Other classes should
	 * only mark objects as destroyed and leave list management to this class.
	 *
	 * @return the list of the currently active (not destroyed) game objects
	 */
	public Array<Particle> getParticles() {
		return particles;
	}

	public Array<Particle> getNoteIndicatorParticles(){return noteIndicatorParticles;}

	/**
	 * Starts level
	 */
	public void start() {
		setupBandMembers();
		updateBandMemberCoords();
	}

	/**
	 * Updates the state.
	 *
	 */
	public void update(){
		//First, check for dead notes and remove them from active arrays
		checkDeadNotes();
		//Then, update the notes for each band member and spawn new notes
		level.updateBandMemberNotes(noteSpawnY);
		//Update the objects of this class (mostly stars)
		for(Particle o : particles){
			o.update(0f);
		}
		for (Particle o: noteIndicatorParticles){
			o.update(0f);
		}

	}

	/**
	 * Update the coordinates
	 */
	public void updateBandMemberCoords(){
		if(curP == PlayPhase.NOTES){
			//If we are in the notes phase, we use setActiveProperties
			level.setActiveProperties(activeBandMember, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND);
		}
		else{
			//Otherwise we must be in transition, so set the transition properties
			float progressFrac = t_progress/(float)T_SwitchPhases;
			level.setTransitionProperties(activeBandMember, goalBandMember, largewidth, smallwidth, TOPBOUND - BOTTOMBOUND, progressFrac);
		}
		//finally, set the bottom left
		level.setBandMemberBl(new Vector2(LEFTBOUND, BOTTOMBOUND), inBetweenWidth);
	}

	/**
	 * Resets the game, deleting all objects.
	 */
	public void reset() {
		particles.clear();
		curP = PlayPhase.NOTES;
		noteIndicatorParticles.clear();
	}

	/**
	 * The maximum number of lines per lane
	 */
	public int lpl = 4;



	/**
	 * Garbage collect for note indicators.
	 * The current implementation will only allow one indicator to appear at a time.
	 * In the future, if we want to change that, we will need to create an additional
	 * backings array.
	 */
	public void garbageCollectNoteIndicators() {
//		Array<Particle> backing = new Array<>();
//		for (Particle o : noteIndicatorParticles) {
//			if (!o.isDestroyed()) {
//				backing.add(o);
//			}
//		}
//		Array<Particle> tmp = backing;
//		backing = noteIndicatorParticles;
//		noteIndicatorParticles = tmp;
//		backing.clear();
		noteIndicatorParticles.clear();
	}

	/**
	 * Garbage collects all deleted objects.
	 *
	 * First perform garbage collection on the objects in here. Then perform garbage
	 * collection for each band member.
	 */
	public void garbageCollect() {
		// INVARIANT: backing and objects are disjoint
		for (Particle o : particles) {
			if (!o.isDestroyed()) {
				backing.add(o);
			}
		}
		// Swap the backing store and the objects.
		// This is essentially stop-and-copy garbage collection
		Array<Particle> tmp = backing;
		backing = particles;
		particles = tmp;
		backing.clear();
		for (BandMember bandMember : level.getBandMembers()) {
			bandMember.garbageCollect();
		}
	}

	/**
	 * Spawns hit particles at x, y, more hit particles with k
	 */
	public void spawnHitEffect(int k, float x, float y){
		for(int i = 0; i < k; ++i){
			for (int j = 0; j < 3; j++) {
				Particle s = new Particle();
				s.setTexture(particleTexture);
				s.getPosition().set(x, y);
				s.setSizeConfine(inBetweenWidth/2f);
				float vx = RandomController.rollFloat(-inBetweenWidth*0.07f, inBetweenWidth*0.07f);
				float vy = RandomController.rollFloat(-inBetweenWidth*0.07f, inBetweenWidth*0.07f);
				s.getVelocity().set(vx,vy);
				particles.add(s);
			}
		}
	}

	public void spawnEnhancedHitEffect(float x, float y){
		for (int j = 0; j < 6; j++) {
			Particle s = new Particle();
			s.setTexture(enhancedParticle);
			s.getPosition().set(x, y);
			s.setSizeConfine(inBetweenWidth/2f);
			float vx = RandomController.rollFloat(-inBetweenWidth*0.07f, inBetweenWidth*0.07f);
			float vy = RandomController.rollFloat(-inBetweenWidth*0.07f, inBetweenWidth*0.07f);
			s.getVelocity().set(vx,vy);
			particles.add(s);
		}
	}

	public void spawnHitIndicator(float x, float y, Texture texture, float scale){
		garbageCollectNoteIndicators();
		Particle s = new Particle();
		s.setTexture(texture);
		s.getPosition().set(x, y);
		s.setSizeConfine(HIT_IND_SIZE*scale);
		s.getVelocity().set(0,-smallwidth/60f);
		s.setAge(10);
		noteIndicatorParticles.add(s);
	}

	/**
	 * Enum to determine whether or not we are in a phase of hitting notes or switching to another band member
	 */
	public enum PlayPhase {
		NOTES,
		TRANSITION
	}

	/** initiate to NOTES phase*/
	PlayPhase curP = PlayPhase.NOTES;
	/** Total progress needed before we declare ourselves fully transitioned */
	long T_SwitchPhases = 8;
	/** The band member lane index that we are trying to switch to */
	int goalBandMember;
	/** The current transition progress */
	long t_progress;

	long t_start;

	/** Switch inputs */
	public boolean[] switches;
	/** Trigger inputs */
	public boolean[] triggers;

	public int perfectHit;
	public int goodHit;
	public int okHit;
	public int miss;


	/**
	 * Handles logic of a hit, whether it is on beat or not, etc.
	 * @param note the note that we are trying to hit
	 * @param currentSample the current sample of the song
	 * @param hitReg the hitReg array
	 * @param lifted whether the note was lifted (if held, can only be true if NoteType == HELD)
	 */
	public void checkHit(Note note,
						 long currentSample,
						 int perfectGain, int goodGain, int okGain, int offBeatLoss,
						 float spawnEffectY,
						 boolean destroy,
						 boolean[] hitReg,
						 boolean lifted) {
		Note.NoteType nt = note.getNoteType();
		// check for precondition that lifted is true iff note type is HELD
		assert !lifted || nt == Note.NoteType.HELD;

		//Check for all the switch notes of this lane, if one is close enough destroy it and
		//give it positive hit status
		long adjustedPosition = currentSample - this.offset;
		long dist = lifted ? Math.abs(adjustedPosition - (note.getHitSample() + note.getHoldSamples()))
				: Math.abs(adjustedPosition - note.getHitSample());

		// check if note was hit or on beat
		if(dist < miss) {
			if (dist < okHit) {
				//If so, destroy the note and set a positive hit status. Also set that we
				//have registered a hit for this line for this click. This ensures that
				//We do not have a single hit count for two notes that are close together
				int compGain = dist < perfectHit ? perfectGain : (dist < goodHit ? goodGain : okGain);

				float hitStatusX = LEFTBOUND+((activeBandMember+1)*(HIT_IND_SIZE/3))+((activeBandMember+1) * smallwidth);
				float hitStatusY = BOTTOMBOUND-(HIT_IND_SIZE/1.6f);
				if (dist < perfectHit){
					spawnHitIndicator(hitStatusX,hitStatusY,perfectHitIndicator,1.3f);
				} else{
					if (dist < goodHit){
						spawnHitIndicator(hitStatusX,hitStatusY,goodHitIndicator,1);
					}else{
						spawnHitIndicator(hitStatusX,hitStatusY,okIndicator,1);
					}
				}

				note.setHolding(true);
				note.setHitStatus(compGain);
				spawnHitEffect(note.getHitStatus(), note.getX(), spawnEffectY);
				if (dist < perfectHit) {
					spawnEnhancedHitEffect(note.getX(), spawnEffectY);
				}
				if (note.getLine() != -1){
					hitReg[note.getLine()] = true;
				}
				note.setDestroyed(destroy);
				// move the note to where the indicator is
				note.setBottomY(hitY);
				String soundKey = nt == Note.NoteType.SWITCH ?
						"switchHit" : (dist < perfectHit ? "perfectHit" : (dist < goodHit ? "goodHit" : "okHit"));
				sfx.playSound(soundKey, GLOBAL_VOLUME_ADJ);
				int pointsReceived = dist < perfectHit ? 500 : (dist < goodHit ? 250 : 100);
				sb.receiveHit(pointsReceived);
			} else {
				// lose some competency since you played a bit off beat
				sb.resetCombo();
				note.setHitStatus(offBeatLoss);
			}
		}
		//if we let go too early we need to reset the combo
		if (lifted && dist >= miss){
			sb.resetCombo();
			note.setHitStatus(offBeatLoss);
		}
	}


	/**
	 * Handle transitions and inputs
	 * @param input
	 */
	public void handleActions(InputController input){
		//Read in inputs
		switches = input.didSwitch();
		triggers = input.didTrigger();

		for (boolean trigger : triggers) {
			if (trigger) {
				sfx.playSound("tap", GLOBAL_VOLUME_ADJ);
				break;
			}
		}

		boolean[] lifted = input.triggerLifted;
		long currentSample = level.getCurrentSample();

		//This array tells us if a hit has already been registered in this frame for the ith bm.
		//We do not want one hit to count for two notes that are close together.
		boolean[] hitReg = new boolean[triggers.length];

		// SWITCH NOTE HIT HANDLING
		if (curP == PlayPhase.NOTES){
			for (boolean trigger : switches) {
				if (trigger) {
					sfx.playSound("switch", GLOBAL_VOLUME_ADJ);
					break;
				}
			}
			for (int i = 0; i < switches.length; ++i){
				if (switches[i] && i != activeBandMember){
					//Check only the lanes that are not the current active lane
					for(Note n : level.getBandMembers()[i].getSwitchNotes()) {
						checkHit(n, currentSample, 5, 4, 3, 0, n.getY(),true, hitReg, false);
					}
					//set goalBM
					goalBandMember = i;
					//change phase
					curP = PlayPhase.TRANSITION;
					//reset progress
					t_progress = 0;
					t_start = level.getCurrentSample();
					return;
				}
			}
		}
		else {
			// Transition phase
			garbageCollectNoteIndicators();

			// Increment progress
			t_progress = level.getCurrentSample() - t_start;

			// Check if we are done, if so set active BM and change phase
			if(t_progress >= T_SwitchPhases){
				curP = PlayPhase.NOTES;
				activeBandMember = goalBandMember;
			}

			// During this phase we need to change the BL and widths of each BM
			updateBandMemberCoords();
		}
		//Now check for hit and held notes
		int checkBandMember = curP == PlayPhase.NOTES ? activeBandMember : goalBandMember;
		for (Note n : level.getBandMembers()[checkBandMember].getHitNotes()){
			if (n.getNoteType() == Note.NoteType.BEAT){
				if (triggers[n.getLine()] && !hitReg[n.getLine()]){
					//Check for all the notes in this line and in the active band member
					//See if any are close enough
					checkHit(n, currentSample, 4, 3, 2, -1, n.getY(),true, hitReg, false);
				}
			}
			// HOLD NOTE
			else {
				//Check if we hit the trigger down close enough to the head
				if(triggers[n.getLine()] && !hitReg[n.getLine()]){
					checkHit(n, currentSample, 4, 3, 2, -1, n.getBottomY(),false, hitReg, false);
				}
				//check if we lifted close to the end (we only check if we ended up holding the note in the first place)
				if(lifted[n.getLine()] && n.isHolding()){
					checkHit(n, currentSample, 3, 2, 1, -1, n.getBottomY(),true, hitReg, true);
					n.setHeldFor(0);
					// destroy (if you are already holding)
					if (n.isHolding()) n.setDestroyed(true);
					n.setHolding(false);
				}
				// check the hold
				if (n.isHolding()) {
					// update competency every beat
					long samplesPerBeat = (long) (level.getMusic().getSampleRate() * (60f / level.getBpm()));
					// approximation of how many samples held
					long heldSamples = level.getCurrentSample() - n.getHitSample();
					if (heldSamples / samplesPerBeat > n.getHeldFor()) {
						level.getBandMembers()[activeBandMember].compUpdate(2);
						n.setHeldFor(n.getHeldFor() + 1);
					}
				}
				// destroy if the note head has gone past (will only be true while you're holding)
				// also reset the combo
				if (((currentSample - this.offset) - (n.getHitSample() + n.getHoldSamples())) > okHit && n.isHolding()) {
					spawnHitEffect(n.getHitStatus(), n.getX(), n.getBottomY());
					n.setDestroyed(true);
					sb.resetCombo();
				}
			}
		}
	}

	public void dispose(){
		level.dispose();
		level = null;
		sfx.dispose();
		sb.dispose();
	}
}