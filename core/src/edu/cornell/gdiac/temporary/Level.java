package edu.cornell.gdiac.temporary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.AudioEngine;
import edu.cornell.gdiac.audio.AudioSource;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.temporary.entity.BandMember;
import edu.cornell.gdiac.temporary.entity.Note;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Level {

    private Texture arrow;
    private Texture pawIndicator;

    private Texture hitCBOX;
    private Texture switchCBOX;

    private FreeTypeFontGenerator fontGenerator;

    private BitmapFont font;
    private FreeTypeFontGenerator.FreeTypeFontParameter fontParameter;

    public BandMember[] getBandMembers() {
        return bandMembers;
    }

    public Texture getHitNoteTexture() {
        return hitNoteTexture;
    }

    public Texture getSwitchNoteTexture() {
        return switchNoteTexture;
    }

    private Texture switchIndicator;

    private Texture switchIndicatorHit;

    private Texture bkgTexture;

    public Texture getHoldNoteTexture() {
        return holdNoteTexture;
    }

    public String getLevelName() {
        return levelName;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getMaxCompetency() {
        return maxCompetency;
    }

    public int getSpawnOffset() {
        return spawnOffset;
    }

    public MusicQueue getMusic() {
        return music;
    }

    public void setBandMembers(BandMember[] bandMembers) {
        this.bandMembers = bandMembers;
    }

    public void setHitNoteTexture(Texture hitNoteTexture) {
        this.hitNoteTexture = hitNoteTexture;
    }

    public void setSwitchNoteTexture(Texture switchNoteTexture) {
        this.switchNoteTexture = switchNoteTexture;
    }

    public void setHoldNoteTexture(Texture holdNoteTexture) {
        this.holdNoteTexture = holdNoteTexture;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public void setMaxCompetency(int maxCompetency) {
        this.maxCompetency = maxCompetency;
    }

    public void setSpawnOffset(int spawnOffset) {
        this.spawnOffset = spawnOffset;
    }

    public void setMusic(MusicQueue music) {
        this.music = music;
    }

    public int getBpm() { return this.bpm; }

    public long getaThreshold() {
        return aThreshold;
    }

    public void setaThreshold(long aThreshold) {
        this.aThreshold = aThreshold;
    }

    public long getbThreshold() {
        return bThreshold;
    }

    public void setbThreshold(long bThreshold) {
        this.bThreshold = bThreshold;
    }

    public long getcThreshold() {
        return cThreshold;
    }

    public void setcThreshold(long cThreshold) {
        this.cThreshold = cThreshold;
    }

    public long getsThreshold() {
        return sThreshold;
    }

    public void setsThreshold(long sThreshold) {
        this.sThreshold = sThreshold;
    }

    private Vector2 TR;
    private Vector2 BL;

    private BandMember[] bandMembers;

    // TEXTURES
    private Texture hitNoteTexture;
    private Texture switchNoteTexture;
    private Texture holdNoteTexture;
    private Texture holdEndTexture;
    private Texture holdTrailTexture;
    private Texture hpbar;
    private Texture noteIndicator;
    private Texture noteIndicatorHit;
    private FilmStrip[] violinSet;
    private FilmStrip[] drummerSet;
    private FilmStrip[] singerSet;
    private FilmStrip[] pianoSet;
    private FilmStrip backSplash;
    private FilmStrip frontSplash;
    private FilmStrip ghostLoom;

    private Texture autoplayBackground;
    private Texture progressBackground;
    private Texture progressForeground;
    private Texture progressKnob;

    // PROPERTIES
    private String levelName;
    private int levelNumber;
    private int maxCompetency;
    private int spawnOffset;
    private long spawnOffsetSwitch;
    private int bpm;
    private long aThreshold;
    private long bThreshold;
    private long cThreshold;
    private long sThreshold;
    private MusicQueue music;

    private boolean isTutorial;
    private Queue<Long> autoplayRanges;
    private Queue<Long> switchRanges;
    private Queue<Long> arrowAppear;

    private long startRange;
    private long endRange;

    private long startSwitchRange;
    private long endSwitchRange = -1;
    public long getStartSwitchRange() {
        return startSwitchRange;
    }
    public long getEndSwitchRange() {
        return endSwitchRange;
    }

    public boolean getIsTutorial() {
        return isTutorial;
    }

    private int toBandMember;

    public int getToBandMember() {
        return toBandMember;
    }

    public boolean isInAutoplayRange() {
        long currentSample = getCurrentSample();
        if (isTutorial && !autoplayRanges.isEmpty()) {
            if (autoplayRanges.size % 2 == 0) {
                if (currentSample >= autoplayRanges.first()) {
                    startRange = autoplayRanges.removeFirst();
                    endRange = autoplayRanges.first();
                    return true;
                }
            } else {
                if (currentSample <= autoplayRanges.first()) {
                    return true;
                } else {
                    autoplayRanges.removeFirst();
                }
            }
            return false;
        }
        return false;
    }

    public boolean isAutoSwitching() {
        long currentSample = getCurrentSample();
        if (isTutorial && (currentSample <= endSwitchRange || endSwitchRange == -1 || !switchRanges.isEmpty())) {
            // set it to one second before
            if (!switchRanges.isEmpty()) {
                if (currentSample >= switchRanges.first() - music.getSampleRate()) {
                    startSwitchRange = switchRanges.first() - music.getSampleRate();
                    endSwitchRange = switchRanges.first() + music.getSampleRate();
                    switchRanges.removeFirst();
                    toBandMember = switchRanges.removeFirst().intValue();
                }
            }
            return currentSample <= endSwitchRange && currentSample >= startSwitchRange;
        }
        return false;
    }

    public boolean isArrowAppearing() {
        long currentSample = getCurrentSample();
        if (isTutorial && !arrowAppear.isEmpty()) {
            if (arrowAppear.size % 2 == 0) {
                if (currentSample >= arrowAppear.first()) {
                    arrowAppear.removeFirst();
                    return true;
                }
            } else {
                if (currentSample <= arrowAppear.first()) {
                    return true;
                } else {
                    arrowAppear.removeFirst();
                }
            }
            return false;
        }
        return false;
    }

    /**
     * The last sample that health was decremented due to continuous decay
     */
    private long lastDec;

    /**
     * Horizontal slice of border
     */
    private Texture HUnit;

    /**
     * set to true if music has been started
     */
    private boolean musicInitialized = false;
    /**
     * Vertical slice of border
     */
    private Texture VUnit;
    /**
     * Corner Texture
     */
    private Texture CUnit;

    /**
     * Line separation texture
     */
    private Texture sepLine;

    /**
     * background of each lane
     */
    private Texture activeLane;
    private Texture inactiveLane;
    AudioSource songSource;
    JsonValue data;

    float maxSample;

    // this comparator is used for comparing comp flag data later on
    private static class CustomComparator implements Comparator<Long[]> {
        @Override
        public int compare(Long[] o1, Long[] o2) {
            return Long.compare(o1[0], o2[0]);
        }
    }

    public Level(JsonValue data, AssetDirectory directory) {
        sample = 0;
        JsonReader jr = new JsonReader();
        JsonValue assets = jr.parse(Gdx.files.internal("assets.json"));
        bkgTexture = directory.getEntry(data.getString("background"), Texture.class);
        // load all related level textures
        hitNoteTexture = directory.getEntry("hit", Texture.class);
        switchNoteTexture = directory.getEntry("switch", Texture.class);
        holdNoteTexture = directory.getEntry("hold-start", Texture.class);
        holdTrailTexture = directory.getEntry("hold-trail", Texture.class);
        hpbar = directory.getEntry("hp-bar", Texture.class);
        holdTrailTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.Repeat);
        holdEndTexture = directory.getEntry("hold-end", Texture.class);
        noteIndicator = directory.getEntry("note-indicator", Texture.class);
        noteIndicatorHit = directory.getEntry("note-indicator-hit", Texture.class);
        switchIndicator = directory.getEntry("switch-indicator", Texture.class);
        switchIndicatorHit = directory.getEntry("switch-indicator-hit", Texture.class);

        violinSet = new FilmStrip[7];
        violinSet[0] = new FilmStrip(directory.getEntry("violin-INACTIVE-NOTES", Texture.class), 1, 6, 6);
        violinSet[1] = new FilmStrip(directory.getEntry("violin-INACTIVE-NO-NOTES", Texture.class), 1, 6, 6);
        violinSet[2] = new FilmStrip(directory.getEntry("violin-INACTIVE-LOW", Texture.class), 1, 2, 2);
        violinSet[3] = new FilmStrip(directory.getEntry("violin-ACTIVE-LEFT", Texture.class), 3, 3, 8);
        violinSet[4] = new FilmStrip(directory.getEntry("violin-ACTIVE-RIGHT", Texture.class), 3, 3, 8);
        violinSet[5] = new FilmStrip(directory.getEntry("violin-ACTIVE-IDLE", Texture.class), 1, 6, 6);
        violinSet[6] = new FilmStrip(directory.getEntry("violin-ACTIVE-MISS", Texture.class), 1, 1, 1);

        singerSet = new FilmStrip[7];
        singerSet[0] = new FilmStrip(directory.getEntry("singer-INACTIVE-NOTES", Texture.class), 1, 6, 6);
        singerSet[1] = new FilmStrip(directory.getEntry("singer-INACTIVE-NO-NOTES", Texture.class), 3, 3, 8);
        singerSet[2] = new FilmStrip(directory.getEntry("singer-INACTIVE-LOW", Texture.class), 2, 2, 4);
        singerSet[3] = new FilmStrip(directory.getEntry("singer-ACTIVE-LEFT", Texture.class), 3, 3, 8);
        singerSet[4] = new FilmStrip(directory.getEntry("singer-ACTIVE-RIGHT", Texture.class), 3, 3, 8);
        singerSet[5] = new FilmStrip(directory.getEntry("singer-ACTIVE-IDLE", Texture.class), 3, 3, 8);
        singerSet[6] = new FilmStrip(directory.getEntry("singer-ACTIVE-MISS", Texture.class), 1, 1, 1);

        drummerSet = new FilmStrip[7];
        drummerSet[0] = new FilmStrip(directory.getEntry("drummer-INACTIVE-NOTES", Texture.class), 2, 3, 6);
        drummerSet[1] = new FilmStrip(directory.getEntry("drummer-INACTIVE-NO-NOTES", Texture.class), 2, 3, 6);
        drummerSet[2] = new FilmStrip(directory.getEntry("drummer-INACTIVE-LOW", Texture.class), 2, 2, 4);
        drummerSet[3] = new FilmStrip(directory.getEntry("drummer-ACTIVE-LEFT", Texture.class), 3, 3, 8);
        drummerSet[4] = new FilmStrip(directory.getEntry("drummer-ACTIVE-RIGHT", Texture.class), 3, 3, 8);
        drummerSet[5] = new FilmStrip(directory.getEntry("drummer-ACTIVE-IDLE", Texture.class), 2, 3, 6);
        drummerSet[6] = new FilmStrip(directory.getEntry("drummer-ACTIVE-MISS", Texture.class), 1, 1, 1);

        pianoSet = new FilmStrip[7];
        pianoSet[0] = new FilmStrip(directory.getEntry("piano-INACTIVE-NOTES", Texture.class), 2, 3, 6);
        pianoSet[1] = new FilmStrip(directory.getEntry("piano-INACTIVE-NO-NOTES", Texture.class), 2, 3, 6);
        pianoSet[2] = new FilmStrip(directory.getEntry("piano-INACTIVE-LOW", Texture.class), 2, 2, 4);
        pianoSet[3] = new FilmStrip(directory.getEntry("piano-ACTIVE-LEFT", Texture.class), 3, 3, 8);
        pianoSet[4] = new FilmStrip(directory.getEntry("piano-ACTIVE-RIGHT", Texture.class), 3, 3, 8);
        pianoSet[5] = new FilmStrip(directory.getEntry("piano-ACTIVE-IDLE", Texture.class), 2, 3, 6);
        pianoSet[6] = new FilmStrip(directory.getEntry("piano-ACTIVE-MISS", Texture.class), 2, 2, 4);

        ghostLoom = new FilmStrip(directory.getEntry("ghost-loom", Texture.class), 1, 1, 1);

        backSplash = new FilmStrip(directory.getEntry("back-splash", Texture.class), 5, 5, 23);
        frontSplash = new FilmStrip(directory.getEntry("front-splash", Texture.class), 5, 5, 21);

        arrow = directory.getEntry("pointer-arrow", Texture.class);
        pawIndicator = directory.getEntry("paw-indicator", Texture.class);
        hitCBOX = directory.getEntry("hitCBox", Texture.class);
        switchCBOX = directory.getEntry("switchCBox", Texture.class);

        autoplayBackground = directory.getEntry("autoplay-background", Texture.class);
        progressBackground = directory.getEntry("autoplay-progress-background", Texture.class);
        progressForeground = directory.getEntry("autoplay-progress-foreground", Texture.class);
        progressKnob = directory.getEntry("autoplay-progress-knob", Texture.class);

        this.data = data;
        //Read in Json  Value and populate asset textures
        lastDec = 0;
        levelName = data.getString("levelName");
        levelNumber = data.getInt("levelNumber");
        maxCompetency = data.getInt("maxCompetency");
        aThreshold = data.get("thresholdA").asLong();
        bThreshold = data.get("thresholdB").asLong();
        cThreshold = data.get("thresholdC").asLong();
        sThreshold = data.get("thresholdS").asLong();
        bpm = data.getInt("bpm");
        String song = data.getString("song");
        int fallSpeed = data.getInt("fallSpeed");
        music = ((AudioEngine) Gdx.audio).newMusic(Gdx.files.internal(assets.get("samples").getString(song)));
        songSource = music.getSource(0);
        music.setVolume(0.8f);
        maxSample = songSource.getDuration() * songSource.getSampleRate();
        HUnit = directory.getEntry("borderHUnit", Texture.class);
        VUnit = directory.getEntry("borderVUnit", Texture.class);
        CUnit = directory.getEntry("borderCorner", Texture.class);
        activeLane = directory.getEntry("activeLane", Texture.class);
        inactiveLane = directory.getEntry("inactiveLane", Texture.class);
        sepLine = directory.getEntry("separationLine", Texture.class);

        // preallocate band members
        bandMembers = new BandMember[data.get("bandMembers").size];
        spawnOffset = 10*music.getSampleRate()/fallSpeed;
        // switch note is twice as slow
        spawnOffsetSwitch = 2L * spawnOffset;
        float samplesPerBeat = songSource.getSampleRate() * 60f/bpm;
        for(int i = 0; i < bandMembers.length; i++){
            bandMembers[i] = new BandMember();
            JsonValue bandMemberData = data.get("bandMembers").get(i);

            // we store comp flags as arrays of size 3: song position, lossRate, gainRate
            // we sort these comp flags in a PriorityQueue for each band member based on song position
            JsonValue compFlags = bandMemberData.get("compFlags");
            PriorityQueue<Long[]> compData = new PriorityQueue<>(10, new CustomComparator());
            for(int j = 0; j < compFlags.size; ++j){
                JsonValue thisCompFlag = compFlags.get(j);
                Long[] arr = new Long[3];
                arr[0] = thisCompFlag.getLong("position");
                arr[1] = (long) thisCompFlag.getInt("rate");
                arr[2] = (long) thisCompFlag.getInt("gain");
                compData.add(arr);
            }
            bandMembers[i].setCompData(compData);

            Queue<Note> notes = new Queue<>();
            JsonValue noteData = bandMemberData.get("notes");
            for(int j = 0; j < noteData.size; ++j){
                JsonValue thisNote = noteData.get(j);
                Note n;

                if (thisNote.getString("type").equals("beat")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.BEAT, thisNote.getLong("position") - spawnOffset, hitNoteTexture);
                }
                else if (thisNote.getString("type").equals("switch")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.SWITCH, thisNote.getLong("position") - spawnOffsetSwitch, switchNoteTexture);
                }
                else {
                    if(thisNote.getLong("position") + thisNote.getLong("duration")> maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.HELD, thisNote.getLong("position") - spawnOffset, holdNoteTexture);
                    n.setHoldTextures(holdTrailTexture,1,holdEndTexture,1, backSplash, frontSplash, getAnimationRateFromBPM(bpm));
                    n.setHoldSamples(thisNote.getLong("duration"));
                }
                n.setHitSample(thisNote.getLong("position"));
                notes.addLast(n);
            }
            bandMembers[i].setAllNotes(notes);
            bandMembers[i].setCurComp(maxCompetency);
            bandMembers[i].setMaxComp(maxCompetency);
            bandMembers[i].setHpBarFilmStrip(hpbar, 47);
            bandMembers[i].setIndicatorTextures(noteIndicator, noteIndicatorHit);
            bandMembers[i].setSPB(samplesPerBeat);
            switch (bandMemberData.getString("instrument")) {
                case "violin":
                    bandMembers[i].setFilmStrips(violinSet);
                    break;
                case "piano":
                    bandMembers[i].setFilmStrips(pianoSet);
                    break;
                case "drum":
                    bandMembers[i].setFilmStrips(drummerSet);
                    break;
                case "voice":
                    bandMembers[i].setFilmStrips(singerSet);
                    break;
            }
            bandMembers[i].setLoomFilmStrip(ghostLoom);
            bandMembers[i].recieveSample(sample);
            bandMembers[i].pickFrame();
        }

        // TUTORIAL LOGIC

        // need autoplay ranges, need switch ranges, need random hit
        JsonValue tutorialData = data.get("tutorialData");
        if (tutorialData != null) {
            fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-SemiBold.ttf"));

            fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            fontParameter.size = 96;
            font = fontGenerator.generateFont(fontParameter);
            fontScale = 1f;
            isTutorial = true;

            autoplayRanges = new Queue<>();
            switchRanges = new Queue<>();
            arrowAppear = new Queue<>();

            long[] autoplayRanges = tutorialData.get("autoplayRanges").asLongArray();
            for (long val : autoplayRanges) {
                this.autoplayRanges.addLast(val);
            }

            long[] switchRanges = tutorialData.get("switchSamples").asLongArray();
            for (long val : switchRanges) {
                this.switchRanges.addLast(val);
            }

            long[] arrowAppear = tutorialData.get("arrowAppear").asLongArray();
            for (long val : arrowAppear) {
                this.arrowAppear.addLast(val);
            }
        }
    }

    public void setMusicVolume(float vol) {
        music.setVolume(vol);
    }

    public float getMusicVolume() {
        return music.getVolume();
    }

    /**
     * Takes BPM and converts it to rate of animation (as all animations should be on beat)
     * @param bpm BPM of song in level
     * @return animation rate
     */
    public float getAnimationRateFromBPM(int bpm) {
        return bpm * 2 / 60f / 60f;
    }

    public int getSamplesPerBeat(){
        return music.getSampleRate()*60/bpm;
    }

    /**
     * Sets all the bottom left corners of the BandMembers. Call this after setting width and height properties
     * @param windowBL - Bottom Left of the play area
     * @param inBetweenWidth - Width between each band member
     */
    public void setBandMemberBl(Vector2 windowBL, float inBetweenWidth){
        float xCoord = windowBL.x;
        float yCoord = windowBL.y;
        for (BandMember bandMember : bandMembers) {
            bandMember.setBottomLeft(new Vector2(xCoord, yCoord));
            xCoord += inBetweenWidth + bandMember.getWidth();
        }
    }

    public void setBandMemberHitY(float hity){
        for(BandMember bandMember : bandMembers){
            bandMember.setHitY(hity);
        }
    }

    /**
     * Sets width and height of BandMembers outside of transition. Call once after transitioning is finished
     * @param activeBandMember - index of active BandMember
     * @param large_width - width of active BandMember
     * @param short_width - width of inactive BandMember
     */
    public void setActiveProperties(int activeBandMember, float large_width, float short_width, float maxLineHeight){
        for (BandMember bandMember : bandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
            bandMember.setHeight(maxLineHeight);
            bandMember.setLaneTint(0.1f);
        }
        bandMembers[activeBandMember].setWidth(large_width);
        bandMembers[activeBandMember].setLineHeight(maxLineHeight);
        bandMembers[activeBandMember].setLaneTint(1f);
        bandMembers[activeBandMember].setMode(1);
    }

    /**
     * Sets the width and line height of BandMembers during transition call at the start and during transition
     * @param previousBM - BandMember we are transitioning away from
     * @param nextBM - BandMember we are transitioning into
     * @param large_width - width of fully active BandMember
     * @param short_width - with of fully in active Band Member
     * @param t_progress - transition progress, should be between 0 and 1. 1 means transition completed.
     * */
    public void setTransitionProperties(int previousBM, int nextBM, float large_width, float short_width, float maxLineHeight, float t_progress){
        //set default widths and heights, which means everyone with no lines and everyone has no lines
        for (BandMember bandMember : bandMembers) {
            bandMember.setWidth(short_width);
            bandMember.setLineHeight(0f);
            bandMember.setHeight(maxLineHeight);
        }

        //set the width and line heights of the active and goal bandmembers accordingly
        bandMembers[previousBM].setWidth(large_width - (large_width - short_width)*t_progress);
        bandMembers[nextBM].setWidth(short_width + (large_width - short_width)*t_progress);
        bandMembers[nextBM].setLaneTint(findTintColorProgress(t_progress, new SingleOperator() {
            @Override
            public float op(float x) {
                return 0.9f*(float)Math.sin(x * Math.PI/2) + 0.1f;
            }
        }));
        bandMembers[previousBM].setLaneTint(findTintColorProgress(1f - t_progress, new SingleOperator() {
            @Override
            public float op(float x) {
                return 0.9f*x * x + 0.1f;
            }
        }));
        bandMembers[previousBM].setLineHeight(maxLineHeight*(1f-t_progress));
        bandMembers[nextBM].setLineHeight(maxLineHeight*t_progress);
    }

    private float findTintColorProgress(float t_progress, SingleOperator op){
        return op.op(t_progress);
    }

    private long sample;

    public void receiveInterrupt(int BM_id, boolean DFflag, boolean JKflag, boolean MISSflag){
        bandMembers[BM_id].recieveSample(sample);
        bandMembers[BM_id].recieveFlags(DFflag, JKflag, MISSflag);
    }

    public void swapActive(int prev, int next){
        bandMembers[prev].changeMode();
        bandMembers[next].changeMode();
    }

    /**
     * We check to see if we have reached the song position of the next comp rate change, and change if we have
     * */
    public void updateCompRates() {
        for (int i=0; i < bandMembers.length; i++) {
            Long[] top = bandMembers[i].getCompData().peek();
            if (top == null) return;
            if (getCurrentSample() >= top[0]) {
                top = bandMembers[i].getCompData().poll();
                bandMembers[i].setLossRate((top[1]).intValue());
                bandMembers[i].setGainRate((top[2]).intValue());
            }
        }
    }

    public int gainRate(int activeBandMember) {
        return bandMembers[activeBandMember].getGainRate();
    }

    /**
     * Spawns new notes according to what sample we are at. Also decrements bandmembers competency. It also updates the frame of the bandmember.
     */
    public void updateBandMemberNotes(float spawnY, int mode, int ticks, int introLength){
        //mode: 0 is INTRO, 1 is PLAYING, 2 is GAME_OVER
        //First get the sample we at
        int rate = music.getSampleRate();
        if (mode == 1)       {
            sample = getCurrentSample();
        } else if (mode == 2){
            sample += (int) ((((float) rate)/60f)*(0.8f*(1f - (((float) ticks)/120f))));
        } else if (mode == 0){
            int startSample = -(int) (((float) rate/60f)*introLength);
            sample = startSample + (int) (((float) rate/60f)*ticks);
        }

        for (int i = 0; i < bandMembers.length; ++i) {
            BandMember bandMember = bandMembers[i];
            if (mode==1) {
                bandMember.recieveSample(sample);
                bandMember.pickFrame();
            }
            if (mode==2){
                bandMember.setGameOver(true);
                bandMember.pickFrame();
            }

            if (!bandMember.getHitNotes().isEmpty() && mode == 1 && !bandMember.hasHold()) {
                float loss = -1f * bandMember.getLossRate() * (sample - lastDec) / ((float) music.getSampleRate());
                // TODO: ONLY UPDATE IF YOU ARE NOT HOLDING
                bandMember.compUpdate(loss);
            }
            //spawn new notes accordingly
            bandMember.spawnNotes(sample);
            //update the note frames
            bandMember.updateNotes(spawnY, sample);

        }
        lastDec = sample;
    }

    /**
     * Starts the music
     */
    public void startmusic(){
        musicInitialized = true;
        music.play();
    }

    public boolean hasMoreNotes(){
        boolean tog = false;
        for(BandMember bm : bandMembers){
            tog = tog ||  bm.hasMoreNotes();
        }
        return tog;
    }

    public void resetLevel(){
        sample = 0;
        float oldVolume = music.getVolume();
        lastDec = 0;
        music.stop();
        music.reset();
        music.clearSources();
        music = ((AudioEngine) Gdx.audio).newMusicBuffer( songSource.getChannels() == 1, songSource.getSampleRate() );
        music.addSource(songSource);
        // reset volume
        music.setVolume(oldVolume);
        bandMembers = new BandMember[data.get("bandMembers").size];
        int fallSpeed = data.getInt("fallSpeed");
        spawnOffset = 10*music.getSampleRate()/fallSpeed;
        // switch note is twice as slow
        spawnOffsetSwitch = 2L * spawnOffset;
        float samplesPerBeat = songSource.getSampleRate() * 60f/bpm;
        for(int i = 0; i < bandMembers.length; i++){
            bandMembers[i] = new BandMember();
            JsonValue bandMemberData = data.get("bandMembers").get(i);
            Queue<Note> notes = new Queue<>();
            JsonValue noteData = bandMemberData.get("notes");
            for(int j = 0; j < noteData.size; ++j){
                JsonValue thisNote = noteData.get(j);
                Note n;
                if (thisNote.getString("type").equals("beat")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.BEAT, thisNote.getLong("position") - spawnOffset, hitNoteTexture);
                }
                else if (thisNote.getString("type").equals("switch")){
                    if(thisNote.getLong("position") > maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.SWITCH, thisNote.getLong("position") - spawnOffsetSwitch, switchNoteTexture);
                }
                else {
                    if(thisNote.getLong("position") + thisNote.getLong("duration")> maxSample){
                        continue;
                    }
                    n = new Note(thisNote.getInt("line"), Note.NoteType.HELD, thisNote.getLong("position") - spawnOffset, holdNoteTexture);
                    n.setHoldTextures(holdTrailTexture,1,holdEndTexture,1, backSplash, frontSplash, getAnimationRateFromBPM(bpm));
                    n.setHoldSamples(thisNote.getLong("duration"));
                }
                n.setHitSample(thisNote.getLong("position"));
                notes.addLast(n);
            }
            bandMembers[i].setAllNotes(notes);
            bandMembers[i].setCurComp(maxCompetency);
            bandMembers[i].setMaxComp(maxCompetency);
            JsonValue compFlags = bandMemberData.get("compFlags");
            PriorityQueue<Long[]> compData = new PriorityQueue<>(10, new CustomComparator());
            for(int j = 0; j < compFlags.size; ++j){
                JsonValue thisCompFlag = compFlags.get(j);
                Long[] arr = new Long[3];
                arr[0] = thisCompFlag.getLong("position");
                arr[1] = (long) thisCompFlag.getInt("rate");
                arr[2] = (long) thisCompFlag.getInt("gain");
                compData.add(arr);
            }
            bandMembers[i].setCompData(compData);
            bandMembers[i].setSPB(samplesPerBeat);
            bandMembers[i].setHpBarFilmStrip(hpbar, 47);
            switch (bandMemberData.getString("instrument")) {
                case "violin":
                    bandMembers[i].setFilmStrips(violinSet);
                    break;
                case "piano":
                    bandMembers[i].setFilmStrips(pianoSet);
                    break;
                case "drum":
                    bandMembers[i].setFilmStrips(drummerSet);
                    break;
                case "voice":
                    bandMembers[i].setFilmStrips(singerSet);
                    break;
            }
            bandMembers[i].setLoomFilmStrip(ghostLoom);
            bandMembers[i].recieveSample(sample);
            bandMembers[i].pickFrame();
        }

        // TUTORIAL LOGIC

        // need autoplay ranges, need switch ranges, need random hit
        JsonValue tutorialData = data.get("tutorialData");
        if (tutorialData != null) {
            fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Blinker-SemiBold.ttf"));

            fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            fontParameter.size = 96;
            font = fontGenerator.generateFont(fontParameter);
            fontScale = 1f;
            isTutorial = true;

            autoplayRanges = new Queue<>();
            switchRanges = new Queue<>();
            arrowAppear = new Queue<>();

            long[] autoplayRanges = tutorialData.get("autoplayRanges").asLongArray();
            for (long val : autoplayRanges) {
                this.autoplayRanges.addLast(val);
            }

            long[] switchRanges = tutorialData.get("switchSamples").asLongArray();
            for (long val : switchRanges) {
                this.switchRanges.addLast(val);
            }

            long[] arrowAppear = tutorialData.get("arrowAppear").asLongArray();
            for (long val : arrowAppear) {
                this.arrowAppear.addLast(val);
            }
        }
    }

    public void stopMusic() {
        music.stop();
    }

    public void pauseMusic() {
        music.pause();
    }

    /**
     * returns true if music is playing
     */
    public boolean isMusicPlaying(){

        return Math.abs(music.getPosition()-songSource.getDuration()) > 0.001*songSource.getDuration();
    }

    /**
     * Gets the current sample of the song
     * @return
     */
    public long getCurrentSample(){
        return (long)(music.getPosition()*music.getSampleRate());
    }

    public long getLevelSample(){
        return sample;
    }

    /**
     * Returns true if the player has unlocked this level
     * @return
     */
    public boolean hasUnlocked(){ return true; };
    float fontScale;
    /**
     * this draws everything the level needs to display on the given canvas
     * @param canvas - what are we drawing on
     * @param active - which band member is active
     * @param goal - which band member are we/have we transitioned to?
     * @param triggers - which triggers are pressed?
     * @param switches - which switches are pressed?
     */
    public void drawEverything(GameCanvas canvas, int active, int goal, boolean[] triggers, boolean[] switches, float borderThickness){
        canvas.drawBackground(bkgTexture,0,0);
        for(int i = 0; i < bandMembers.length; ++i){
            //Draw the border of each band member
            bandMembers[i].drawBackground(canvas, activeLane, inactiveLane);
            bandMembers[i].drawBorder(canvas, HUnit, VUnit, CUnit, borderThickness);
            //draw the character sprite and the comp bar
            if (i == active){
                bandMembers[i].drawGhostLoom(canvas);
            }
            bandMembers[i].drawCharacterSprite(canvas);
            bandMembers[i].drawHPBar(canvas);

            //If we are the goal of the active lane we need to draw separation lines and held/beat notes
            //We also need to draw a separate hit bar for each line
            if(active == i || goal == i){

                bandMembers[i].drawHitNotes(canvas);
                bandMembers[i].drawLineSeps(canvas, sepLine);
                bandMembers[i].drawIndicator(canvas, noteIndicator, noteIndicatorHit, triggers);
                if (isInAutoplayRange()) {
                    int j = bandMembers[i].getHitNotes().isEmpty() ? -1 : bandMembers[i].getHitNotes().first().getLine();
                    bandMembers[i].drawPawIndicator(canvas, pawIndicator, j);

                }
                if (isArrowAppearing()) {
                    bandMembers[i].drawArrow(canvas, arrow);
                }
                if(isTutorial){
                    bandMembers[i].drawControlBox(canvas, hitCBOX);
                    String[] controls = InputController.triggerKeyBinds(true);
                    for(int k = 0; k < 4; ++k){
                        GlyphLayout layout = new GlyphLayout(font,controls[k]);
                        float boundSize = hitCBOX.getHeight()*Math.min(0.2f * bandMembers[i].getHeight()/hitCBOX.getHeight(), 0.85f*bandMembers[i].getWidth()/hitCBOX.getWidth());
                        System.out.println(hitCBOX.getHeight() + " " +  bandMembers[i].getHeight() + " " + bandMembers[i].getWidth());
                        fontScale *= 0.5*Math.min(boundSize/layout.width, boundSize/layout.height);
                        font.getData().setScale(fontScale);
                        layout = new GlyphLayout(font, controls[k]);
                        font.setColor(Color.WHITE);
                        canvas.drawTextSetColor(controls[k], font, bandMembers[i].getBottomLeft().x + bandMembers[i].getWidth()/8f + k*bandMembers[i].getWidth()/4f - layout.width/2, 0.85f*bandMembers[i].getHeight()+ bandMembers[i].getBottomLeft().y + layout.height/2);
                    }
                }
            }
            //Otherwise just draw the switch notes, and we only have 1 hit bar to draw
            else{
                bandMembers[i].drawSwitchNotes(canvas);
                bandMembers[i].drawIndicator(canvas, switchIndicator, switchIndicatorHit, switches[i]);
                if (isAutoSwitching() && i == toBandMember && getCurrentSample() <= (endSwitchRange + startSwitchRange)/2f) {
                    bandMembers[i].drawPawIndicator(canvas, pawIndicator);
                }
                if(isTutorial){
                    bandMembers[i].drawControlBox(canvas, switchCBOX);
                    String[] controls = InputController.switchKeyBinds(bandMembers.length);
                    GlyphLayout layout = new GlyphLayout(font,controls[i]);
                    float boundSize = switchCBOX.getHeight()*Math.min(0.2f * bandMembers[i].getHeight()/switchCBOX.getHeight(), 0.85f*bandMembers[i].getWidth()/switchCBOX.getWidth());
                    fontScale *= 0.5*Math.min(boundSize/layout.width, boundSize/layout.height);
                    font.getData().setScale(fontScale);
                    font.setColor(Color.WHITE);
                    layout = new GlyphLayout(font,controls[i]);
                    canvas.drawTextSetColor(controls[i], font, bandMembers[i].getBottomLeft().x + bandMembers[i].getWidth()/2f - layout.width/2f, 0.85f*bandMembers[i].getHeight()+ bandMembers[i].getBottomLeft().y + layout.height/2);
                }
            }
        }

        if (isInAutoplayRange() || isAutoSwitching()) {
            canvas.draw(autoplayBackground,
                    Color.BLACK,
                    autoplayBackground.getWidth()/2f, autoplayBackground.getHeight()/2f,
                    (TR.x + BL.x)/2f, (TR.y + BL.y)/2f,
                    0f,
                    (TR.x - BL.x)/(autoplayBackground.getWidth()),(TR.y - BL.y)/(autoplayBackground.getHeight()));
            canvas.draw(progressBackground,
                    Color.WHITE,
                    progressBackground.getWidth()/2f, progressBackground.getHeight()/2f,
                    (TR.x + BL.x)/2f, (BL.y),
                    0f,
                    (TR.x - BL.x)/(progressBackground.getWidth()),(BL.y - (BL.y - (TR.y - BL.y) * 0.1f))/(progressBackground.getHeight()));
            long st = isInAutoplayRange() ? startRange : startSwitchRange;
            long fin = isInAutoplayRange() ? endRange : endSwitchRange;
            canvas.draw(progressForeground,
                    Color.WHITE,
                    0, progressForeground.getHeight()/2f,
                    0, (BL.y),
                    0f,
                    (TR.x - BL.x)/(progressForeground.getWidth()) * ((float) (getCurrentSample() - st)/(fin - st)),
                    (BL.y - (BL.y - (TR.y - BL.y) * 0.1f))/(progressForeground.getHeight()));
            canvas.draw(progressKnob,
                    Color.WHITE,
                    progressKnob.getWidth()/2f, progressKnob.getHeight()/2f,
                    progressForeground.getWidth() * (TR.x - BL.x)/(progressForeground.getWidth()) * ((float) (getCurrentSample() - st)/(fin - st)),
                    (BL.y),
                    0f,
                    ((BL.y - (TR.y - BL.y) * 0.2f) - BL.y)/progressKnob.getWidth(),((BL.y - (TR.y - BL.y) * 0.2f) - BL.y)/progressKnob.getWidth());
        }
    }

    public void setBounds(Vector2 TR, Vector2 BL) {
        this.TR = TR;
        this.BL = BL;
    }

    public void dispose(){
        music.dispose();
        activeLane.dispose();
        inactiveLane.dispose();
        hitNoteTexture.dispose();
        holdTrailTexture.dispose();
        holdNoteTexture.dispose();
        switchNoteTexture.dispose();
        holdEndTexture.dispose();
        noteIndicator.dispose();
        noteIndicatorHit.dispose();
        hpbar.dispose();
        music.dispose();
    }
}
