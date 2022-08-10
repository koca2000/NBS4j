package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

public class SongMetadata {

    private String title = "";
    private String author = "";
    private String originalAuthor = "";
    private String description = "";
    private boolean autoSave = false;
    private byte autoSaveDuration = 10;
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

    public SongMetadata(){}

    /**
     * Makes a copy of all metadata
     * @param metadata metadata to be copied
     */
    SongMetadata(@NotNull SongMetadata metadata){
        title = metadata.title;
        author = metadata.author;
        originalAuthor = metadata.originalAuthor;
        description = metadata.description;
        autoSave = metadata.autoSave;
        autoSaveDuration = metadata.autoSaveDuration;
        timeSignature = metadata.timeSignature;
        minutesSpent = metadata.minutesSpent;
        leftClicks = metadata.leftClicks;
        rightClicks = metadata.rightClicks;
        noteBlocksAdded = metadata.noteBlocksAdded;
        noteBlocksRemoved = metadata.noteBlocksRemoved;
        originalMidiFileName = metadata.originalMidiFileName;
        loop = metadata.loop;
        loopMaxCount = metadata.loopMaxCount;
        loopStartTick = metadata.loopStartTick;
    }

    /**
     * Sets the title of the song
     * @param title song title
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is null.
     */
    @NotNull
    public SongMetadata setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets name of the author of the nbs song.
     * @param author author's name
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is null.
     */
    @NotNull
    public SongMetadata setAuthor(@NotNull String author) {
        this.author = author;
        return this;
    }

    /**
     * Sets name of the author of the original song that inspired the nbs song.
     * @param originalAuthor original song author's name
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is null.
     */
    @NotNull
    public SongMetadata setOriginalAuthor(@NotNull String originalAuthor) {
        this.originalAuthor = originalAuthor;
        return this;
    }

    /**
     * Sets description of the song.
     * @param description song's description
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is null.
     */
    @NotNull
    public SongMetadata setDescription(@NotNull String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets whether the song should be auto-saved during modifications in OpenNoteBlockStudio
     * @param autoSave true if the OpenNoteBlockStudio should do auto-save.
     * @return this instance of {@link SongMetadata}
     */
    @NotNull
    public SongMetadata setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        return this;
    }

    /**
     * Sets the duration between two auto-saves in OpenNoteBlockStudio. No longer used.
     * @param autoSaveDuration minutes between auto-saves in range [1; 60] inclusive.
     * @see #setAutoSave(boolean)
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is not in range [1; 60].
     */
    @NotNull
    public SongMetadata setAutoSaveDuration(byte autoSaveDuration) {
        if (autoSaveDuration < 1 || autoSaveDuration > 60)
            throw new IllegalArgumentException("Auto-save duration must be in range [1; 60].");
        this.autoSaveDuration = autoSaveDuration;
        return this;
    }

    /**
     * Sets time signature as x/4ths
     * @param timeSignature x part of x/4ths time signature
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is not in range [2; 8].
     */
    @NotNull
    public SongMetadata setTimeSignature(byte timeSignature) {
        if (timeSignature < 2 || timeSignature > 8)
            throw new IllegalArgumentException("Time signature must be in range [2; 8].");
        this.timeSignature = timeSignature;
        return this;
    }

    /**
     * Sets how many minutes were spent by editing the song in OpenNoteBlockStudio.
     * @param minutesSpent number of minutes
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setMinutesSpent(int minutesSpent) {
        if (minutesSpent < 0)
            throw new IllegalArgumentException("Amount of minutes spent on project can not be negative.");
        this.minutesSpent = minutesSpent;
        return this;
    }

    /**
     * Sets how many left clicks were done while editing the song in OpenNoteBlockStudio.
     * @param leftClicks number of left clicks
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setLeftClicks(int leftClicks) {
        if (leftClicks < 0)
            throw new IllegalArgumentException("Number of left clicks can not be negative.");
        this.leftClicks = leftClicks;
        return this;
    }

    /**
     * Sets how many right clicks were done while editing the song in OpenNoteBlockStudio.
     * @param rightClicks number of right clicks
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setRightClicks(int rightClicks) {
        if (rightClicks < 0)
            throw new IllegalArgumentException("Number of right clicks can not be negative.");
        this.rightClicks = rightClicks;
        return this;
    }

    /**
     * Sets how many notes were added while editing the song in OpenNoteBlockStudio.
     * @param noteBlocksAdded number of added notes
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setNoteBlocksAdded(int noteBlocksAdded) {
        if (noteBlocksAdded < 0)
            throw new IllegalArgumentException("Number of note blocks added can not be negative.");
        this.noteBlocksAdded = noteBlocksAdded;
        return this;
    }

    /**
     * Sets how many notes were removed while editing the song in OpenNoteBlockStudio.
     * @param noteBlocksRemoved number of removed notes
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setNoteBlocksRemoved(int noteBlocksRemoved) {
        if (noteBlocksRemoved < 0)
            throw new IllegalArgumentException("Number of note blocks removed can not be negative.");
        this.noteBlocksRemoved = noteBlocksRemoved;
        return this;
    }

    /**
     * Sets the name of the midi file that were used to generate this nbs song.
     * @param originalMidiFileName name of the midi file
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is null.
     */
    @NotNull
    public SongMetadata setOriginalMidiFileName(@NotNull String originalMidiFileName) {
        this.originalMidiFileName = originalMidiFileName;
        return this;
    }

    /**
     * Sets whether the playback in OpenNoteBlockStudio should loop.
     * @param loop true if song should loop; otherwise, false
     * @return this instance of {@link SongMetadata}
     */
    @NotNull
    public SongMetadata setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Sets how many times the song may be looped in OpenNoteBlockStudio.
     * @param loopMaxCount number of loops to do, 0 is infinite loop
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setLoopMaxCount(byte loopMaxCount) {
        if (loopMaxCount < 0)
            throw new IllegalArgumentException("Maximum count of loops can not be negative.");
        this.loopMaxCount = loopMaxCount;
        return this;
    }

    /**
     * Sets from which tick should the loop playback start in OpenNoteBlockStudio.
     * @param loopStartTick first tick of the loop
     * @return this instance of {@link SongMetadata}
     * @throws IllegalArgumentException if the argument is negative.
     */
    @NotNull
    public SongMetadata setLoopStartTick(short loopStartTick) {
        if (loopStartTick < 0)
            throw new IllegalArgumentException("First tick of loop can not be negative.");
        this.loopStartTick = loopStartTick;
        return this;
    }

    /**
     * Returns the title of the song.
     * @return title
     */
    @NotNull
    public String getTitle() {
        return title;
    }

    /**
     * Returns the name of the author of the song.
     * @return author's name
     */
    @NotNull
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the name of the author of the original song this nbs song was inspired by.
     * @return original song author's name
     */
    @NotNull
    public String getOriginalAuthor() {
        return originalAuthor;
    }

    /**
     * Returns description of the song.
     * @return song's description
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Returns whether the song should be auto-saved during modifications in OpenNoteBlockStudio
     * @return true if the OpenNoteBlockStudio should do auto-save; otherwise, false.
     */
    public boolean isAutoSave() {
        return autoSave;
    }

    /**
     * Returns the duration between two auto-saves in OpenNoteBlockStudio. No longer used.
     * @return minutes between auto-saves
     */
    public byte getAutoSaveDuration() {
        return autoSaveDuration;
    }

    /**
     * Returns x/4ths time signature
     * @return x part of x/4ths time signature
     */
    public byte getTimeSignature() {
        return timeSignature;
    }

    /**
     * Returns how many minutes were spent by editing the song in OpenNoteBlockStudio.
     * @return number of minutes
     */
    public int getMinutesSpent() {
        return minutesSpent;
    }

    /**
     * Returns how many left clicks were done while editing the song in OpenNoteBlockStudio.
     * @return number of left clicks
     */
    public int getLeftClicks() {
        return leftClicks;
    }

    /**
     * Returns how many right clicks were done while editing the song in OpenNoteBlockStudio.
     * @return number of right clicks
     */
    public int getRightClicks() {
        return rightClicks;
    }

    /**
     * Returns how many notes were added while editing the song in OpenNoteBlockStudio.
     * @return number of added notes
     */
    public int getNoteBlocksAdded() {
        return noteBlocksAdded;
    }

    /**
     * Returns how many notes were removed while editing the song in OpenNoteBlockStudio.
     * @return number of removed notes
     */
    public int getNoteBlocksRemoved() {
        return noteBlocksRemoved;
    }

    /**
     * Returns the name of the midi file that were used to generate this nbs song.
     * @return name of the midi file
     */
    @NotNull
    public String getOriginalMidiFileName() {
        return originalMidiFileName;
    }

    /**
     * Returns whether the playback in OpenNoteBlockStudio should loop.
     * @return true if song should loop; otherwise, false
     */
    public boolean isLoop() {
        return loop;
    }

    /**
     * Returns how many times the song may be looped in OpenNoteBlockStudio.
     * @return number of loops to do, 0 is infinite loop
     */
    public byte getLoopMaxCount() {
        return loopMaxCount;
    }

    /**
     * Returns from which tick should the loop playback start in OpenNoteBlockStudio.
     * @return first tick of the loop
     */
    public short getLoopStartTick() {
        return loopStartTick;
    }
}
