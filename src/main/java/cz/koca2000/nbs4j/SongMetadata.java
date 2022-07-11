package cz.koca2000.nbs4j;

public class SongMetadata {

    private String title = "";
    private String author = "";
    private String originalAuthor = "";
    private String description = "";
    private boolean autoSave = false;
    private byte autoSaveDuration = 0;
    private byte timeSignature = 4; // x/4ths

    private int minutesSpent = 0;
    private int leftClicks = 0;
    private int rightClicks = 0;
    private int noteBlocksAdded = 0;
    private int noteBlocksRemoved = 0;
    private String originalMidiFileName = "";

    private boolean loop = false;
    private byte loopMaxCount = 0;
    private short loopStartTick = 0;

    public SongMetadata setTitle(String title) {
        this.title = title;
        return this;
    }

    public SongMetadata setAuthor(String author) {
        this.author = author;
        return this;
    }

    public SongMetadata setOriginalAuthor(String originalAuthor) {
        this.originalAuthor = originalAuthor;
        return this;
    }

    public SongMetadata setDescription(String description) {
        this.description = description;
        return this;
    }

    public SongMetadata setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        return this;
    }

    public SongMetadata setAutoSaveDuration(byte autoSaveDuration) {
        this.autoSaveDuration = autoSaveDuration;
        return this;
    }

    public SongMetadata setTimeSignature(byte timeSignature) {
        this.timeSignature = timeSignature;
        return this;
    }

    public SongMetadata setMinutesSpent(int minutesSpent) {
        this.minutesSpent = minutesSpent;
        return this;
    }

    public SongMetadata setLeftClicks(int leftClicks) {
        this.leftClicks = leftClicks;
        return this;
    }

    public SongMetadata setRightClicks(int rightClicks) {
        this.rightClicks = rightClicks;
        return this;
    }

    public SongMetadata setNoteBlocksAdded(int noteBlocksAdded) {
        this.noteBlocksAdded = noteBlocksAdded;
        return this;
    }

    public SongMetadata setNoteBlocksRemoved(int noteBlocksRemoved) {
        this.noteBlocksRemoved = noteBlocksRemoved;
        return this;
    }

    public SongMetadata setOriginalMidiFileName(String originalMidiFileName) {
        this.originalMidiFileName = originalMidiFileName;
        return this;
    }

    public SongMetadata setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public SongMetadata setLoopMaxCount(byte loopMaxCount) {
        this.loopMaxCount = loopMaxCount;
        return this;
    }

    public SongMetadata setLoopStartTick(short loopStartTick) {
        this.loopStartTick = loopStartTick;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getOriginalAuthor() {
        return originalAuthor;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public byte getAutoSaveDuration() {
        return autoSaveDuration;
    }

    public byte getTimeSignature() {
        return timeSignature;
    }

    public int getMinutesSpent() {
        return minutesSpent;
    }

    public int getLeftClicks() {
        return leftClicks;
    }

    public int getRightClicks() {
        return rightClicks;
    }

    public int getNoteBlocksAdded() {
        return noteBlocksAdded;
    }

    public int getNoteBlocksRemoved() {
        return noteBlocksRemoved;
    }

    public String getOriginalMidiFileName() {
        return originalMidiFileName;
    }

    public boolean isLoop() {
        return loop;
    }

    public byte getLoopMaxCount() {
        return loopMaxCount;
    }

    public short getLoopStartTick() {
        return loopStartTick;
    }
}
