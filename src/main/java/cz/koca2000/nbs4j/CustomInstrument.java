package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomInstrument {

    private String name = "";
    private String fileName = "";
    private int key = 0;
    private boolean shouldPressKey = false;

    private boolean isFrozen = false;
    private Song song;

    public CustomInstrument(){}

    /**
     * Creates a copy of the custom instrument. Copy is not frozen and does not belong to any song.
     * @param customInstrument custom instrument to be copied
     */
    public CustomInstrument(@NotNull CustomInstrument customInstrument){
        name = customInstrument.name;
        fileName = customInstrument.fileName;
        key = customInstrument.key;
        shouldPressKey = customInstrument.shouldPressKey;

        isFrozen = false;
        song = null;
    }

    @NotNull
    CustomInstrument setSong(@NotNull Song song){
        if (this.song != null)
            throw new IllegalStateException("Custom instrument was already added to a song.");

        this.song = song;
        return this;
    }

    /**
     * Sets the name of this custom instrument
     * @param name name of the custom instrument
     * @return this instance of {@link CustomInstrument}
     * @throws IllegalArgumentException if the argument is null.
     * @throws IllegalStateException if the custom instrument is frozen and can not be modified
     */
    @NotNull
    public CustomInstrument setName(@NotNull String name){
        throwIfFrozen();

        this.name = name;
        return this;
    }

    /**
     * Sets the name of the file used in OpenNoteBlockStudio
     * @param fileName name of the file
     * @return this instance of {@link CustomInstrument}
     * @throws IllegalArgumentException if the argument is null.
     * @throws IllegalStateException if the custom instrument is frozen and can not be modified
     */
    @NotNull
    public CustomInstrument setFileName(@NotNull String fileName){
        throwIfFrozen();

        this.fileName = fileName;
        return this;
    }

    /**
     * Key of this custom instrument.
     * @param key Value 0 is A0 and 87 is C8.
     * @return this instance of {@link CustomInstrument}
     * @throws IllegalArgumentException if the argument is not in range [0; 87] inclusive.
     * @throws IllegalStateException if the custom instrument is frozen and can not be modified
     */
    @NotNull
    public CustomInstrument setKey(int key){
        throwIfFrozen();

        if (key < 0 || key > 87)
            throw new IllegalArgumentException("Key must be in range [0; 87].");
        this.key = key;
        return this;
    }

    /**
     * Sets whether OpenNoteBlockStudio should press key on piano when playing this instrument's note.
     * @param shouldPressKey true if the key should be pressed; otherwise, false
     * @return this instance of {@link CustomInstrument}
     * @throws IllegalStateException if the custom instrument is frozen and can not be modified
     */
    @NotNull
    public CustomInstrument setShouldPressKey(boolean shouldPressKey) {
        throwIfFrozen();

        this.shouldPressKey = shouldPressKey;
        return this;
    }

    /**
     * Returns the name of this custom instrument.
     * @return name of the instrument
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns name of the file used in OpenNoteBlockStudio for this custom instrument.
     * @return file name
     */
    @NotNull
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the key of this custom instrument.
     * @return value in range [0; 87]; 0 is A0 and 87 is C8.
     */
    public int getKey() {
        return key;
    }

    /**
     * Returns the song this custom instrument belongs to.
     * @return {@link Song} if the custom instrument was added to a song; otherwise, null
     */
    @Nullable
    public Song getSong() {
        return song;
    }

    /**
     * Returns whether OpenNoteBlockStudio should press key on piano when playing this instrument's note.
     * @return true if the key should be pressed; otherwise, false
     */
    public boolean shouldPressKey() {
        return shouldPressKey;
    }

    void freeze(){
        isFrozen = true;
    }

    private void throwIfFrozen(){
        if (isFrozen)
            throw new IllegalStateException("Custom instrument is frozen and can not be modified.");
    }
}
