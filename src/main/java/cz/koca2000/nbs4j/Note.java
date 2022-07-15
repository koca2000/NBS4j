package cz.koca2000.nbs4j;

public final class Note {

    public static final int NEUTRAL_PANNING = 0;
    public static final int MAXIMUM_PANNING = 100;

    private Layer layer;

    private int instrument = 0;
    private boolean isCustomInstrument = false;
    private int key = 45;
    private int pitch = 0;
    private int panning = 0;
    private byte volume = 100;

    private boolean isFrozen = false;

    public Note(){}

    /**
     * Creates a copy of the note that is not frozen and does not belong to any song and layer.
     * @param note note to be copied
     */
    public Note(Note note){
        instrument = note.instrument;
        isCustomInstrument = note.isCustomInstrument;
        key = note.key;
        pitch = note.pitch;
        panning = note.panning;
        volume = note.volume;

        isFrozen = false;
        layer = null;
    }

    void setLayer(Layer layer){
        if (this.layer != null)
            throw new IllegalStateException("Note was already added to a layer.");

        this.layer = layer;
    }

    void removedFromLayer(){
        this.layer = null;
    }

    /**
     * Sets index of the instrument of this note.
     * @param instrument index of the instrument
     * @return this instance of {@link Note}
     * @throws IllegalStateException if the note is frozen and can not be modified
     */
    public Note setInstrument(int instrument){
        return setInstrument(instrument, false);
    }

    /**
     * Sets index of the instrument of this note and whether it is custom instrument.
     * @param instrument index of the instrument
     * @param isCustom whether it is custom instrument (custom instruments have their own indexing)
     * @return this instance of {@link Note}
     * @throws IllegalStateException if the note is frozen and can not be modified
     */
    public Note setInstrument(int instrument, boolean isCustom){
        throwIfFrozen();

        if (instrument < 0)
            throw new IllegalArgumentException("Instrument index can not be negative");

        this.instrument = instrument;
        isCustomInstrument = isCustom;

        if (!isCustomInstrument && layer != null)
            layer.getSong().increaseNonCustomInstrumentsCountTo(instrument + 1);

        return this;
    }

    /**
     * Sets the key of this note.
     * @param key Value 0 is A0 and 87 is C8.
     * @return this instance of {@link Note}
     * @throws IllegalArgumentException if the key is outside of range [0; 87] inclusive.
     * @throws IllegalStateException if the note is frozen and can not be modified
     */
    public Note setKey(int key){
        throwIfFrozen();

        if (key < 0 || key > 87)
            throw new IllegalArgumentException("Key must be in range [0; 87] inclusive.");

        this.key = (byte) key;
        return this;
    }

    /**
     * Sets the volume of this note.
     * @param volume volume between 0 and 100.
     * @return this instance of {@link Note}
     * @throws IllegalArgumentException if volume is outside of range [0; 100] inclusive.
     * @throws IllegalStateException if the note is frozen and can not be modified
     */
    public Note setVolume(int volume){
        throwIfFrozen();

        if (volume < 0 || volume > 100)
            throw new IllegalArgumentException("Volume must be in range [0; 100] inclusive.");

        this.volume = (byte) volume;
        return this;
    }

    /**
     * Sets the fine pitch of note.
     * @param pitch 0 is no fine pitch; +-100 is semitone difference
     * @return this instance of {@link Note}
     * @throws IllegalStateException if the note is frozen and can not be modified
     */
    public Note setPitch(int pitch){
        throwIfFrozen();

        this.pitch = pitch;
        return this;
    }

    /**
     * Sets the stereo offset of the note.
     * @param panning -100 two blocks left; 0 center; 100 two blocks right
     * @return this instance of {@link Note}
     * @throws IllegalArgumentException if the panning is out of range [-{@link #MAXIMUM_PANNING}; {@link #MAXIMUM_PANNING}] inclusive]
     * @throws IllegalStateException if the note is frozen and can not be modified
     */
    public Note setPanning(int panning){
        throwIfFrozen();

        if (panning < -MAXIMUM_PANNING || panning > MAXIMUM_PANNING)
            throw new IllegalArgumentException("Panning must be in range [-" + MAXIMUM_PANNING + "; " + MAXIMUM_PANNING + "] inclusive.");

        this.panning = panning;

        if (layer != null && panning != NEUTRAL_PANNING)
            layer.getSong().setStereo();

        return this;
    }

    void freeze(){
        isFrozen = true;
    }

    /**
     * Returns the index of the instrument of this note. To recognize whether it is custom instrument use {@link #isCustomInstrument()}
     * @return index of the instrument
     */
    public int getInstrument() {
        return instrument;
    }

    /**
     * Returns whether the note uses custom instrument.
     * @return true if used instrument is custom instrument; otherwise, false
     */
    public boolean isCustomInstrument() {
        return isCustomInstrument;
    }

    /**
     * Returns the key of the note.
     * @return value in range [0; 87]; 0 is A0 and 87 is C8.
     */
    public int getKey() {
        return key;
    }

    /**
     * Returns fine pitch of the note.
     * @return 0 is no fine pitch; +-100 is semitone difference
     */
    public int getPitch() {
        return pitch;
    }

    /**
     * Returns value of stereo offset of this note.
     * @return value in range [-100; 100]; -100 two blocks left; 0 center; 100 two blocks right
     */
    public int getPanning() {
        return panning;
    }

    /**
     * Returns the volume of this note.
     * @return value in range [0; 100]
     */
    public byte getVolume() {
        return volume;
    }

    /**
     * Return the layer this note is in.
     * @return {@link Layer} if the note was added to a song; otherwise, null.
     */
    public Layer getLayer() {
        return layer;
    }

    private void throwIfFrozen(){
        if (isFrozen)
            throw new IllegalStateException("Layer is frozen and can not be modified.");
    }
}
