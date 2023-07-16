package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

public final class Note {

    public static final int NEUTRAL_PANNING = 0;
    public static final int MAXIMUM_PANNING = 100;

    private final int instrument;
    private final boolean isCustomInstrument;
    private final int key;
    private final int pitch;
    private final int panning;
    private final byte volume;

    private Layer layer = null;

    private Note(@NotNull Builder noteBuilder){
        instrument = noteBuilder.instrument;
        isCustomInstrument = noteBuilder.isCustomInstrument;
        key = noteBuilder.key;
        pitch = noteBuilder.pitch;
        panning = noteBuilder.panning;
        volume = noteBuilder.volume;
    }

    /**
     * Sets the {@link Layer} during the initialization.
     * @param song Layer to which this note belongs
     * @throws IllegalStateException If the method was already called before.
     */
    void setLayer(@NotNull Layer song) {
        if (this.layer != null) {
            throw new IllegalStateException("Note already has the layer set.");
        }
        this.layer = song;
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
     * Returns the {@link Layer} to which the note belongs
     * @return {@link Layer}
     */
    public @NotNull Layer getLayer() {
        return layer;
    }

    public static final class Builder {
        private int instrument = 0;
        private boolean isCustomInstrument = false;
        private int key = 45;
        private int pitch = 0;
        private int panning = 0;
        private byte volume = 100;

        Builder() {
        }

        Builder(@NotNull Note note) {
            instrument = note.instrument;
            isCustomInstrument = note.isCustomInstrument;
            key = note.key;
            pitch = note.pitch;
            panning = note.panning;
            volume = note.volume;
        }

        /**
         * Sets index of the non-custom instrument of this note.
         * @param instrument index of the instrument
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setInstrument(int instrument){
            return setInstrument(instrument, false);
        }

        /**
         * Sets the non-custom instrument of this note.
         * @param instrument {@link Instrument}
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setInstrument(Instrument instrument){
            return setInstrument(instrument.getId(), false);
        }

        /**
         * Sets index of the instrument of this note and whether it is custom instrument.
         * @param instrument index of the instrument
         * @param isCustom whether it is custom instrument (custom instruments have their own indexing)
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setInstrument(int instrument, boolean isCustom){
            if (instrument < 0)
                throw new IllegalArgumentException("Instrument index can not be negative");

            this.instrument = instrument;
            isCustomInstrument = isCustom;

            return this;
        }

        /**
         * Sets the key of this note.
         * @param key Value 0 is A0 and 87 is C8.
         * @return this instance of {@link Builder}
         * @throws IllegalArgumentException if the key is outside of range [0; 87] inclusive.
         */
        @NotNull
        public Builder setKey(int key){
            if (key < 0 || key > 87)
                throw new IllegalArgumentException("Key must be in range [0; 87] inclusive.");

            this.key = (byte) key;
            return this;
        }

        /**
         * Sets the volume of this note.
         * @param volume volume between 0 and 100. Values greater than 100 are clipped to 100 and values lower than 0 are clipped to 0.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setVolume(int volume){
            if (volume < 0) {
                volume = 0;
            }
            if (volume > 100) {
                volume = 100;
            }

            this.volume = (byte) volume;
            return this;
        }

        /**
         * Sets the fine pitch of note.
         * @param pitch 0 is no fine pitch; +-100 is semitone difference
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setPitch(int pitch){
            this.pitch = pitch;
            return this;
        }

        /**
         * Sets the stereo offset of the note.
         * @param panning -100 two blocks left; 0 center; 100 two blocks right
         * @return this instance of {@link Builder}
         * @throws IllegalArgumentException if the panning is out of range [-{@link #MAXIMUM_PANNING}; {@link #MAXIMUM_PANNING}] inclusive]
         */
        @NotNull
        public Builder setPanning(int panning){
            if (panning < -MAXIMUM_PANNING || panning > MAXIMUM_PANNING)
                throw new IllegalArgumentException("Panning must be in range [-" + MAXIMUM_PANNING + "; " + MAXIMUM_PANNING + "] inclusive.");

            this.panning = panning;

            return this;
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
         * Creates new instance of {@link Note} based on data from this builder.
         * @return {@link Note}
         */
        @NotNull Note build() {
            return new Note(this);
        }
    }
}
