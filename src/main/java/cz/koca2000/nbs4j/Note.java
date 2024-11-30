package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

public interface Note {
    int NEUTRAL_PANNING = 0;
    int MAXIMUM_PANNING = 100;

    int MINIMUM_KEY = 0;
    int MAXIMUM_KEY = 87;

    int MINIMUM_VOLUME = 0;
    int MAXIMUM_VOLUME = 100;

    /**
     * Returns the index of the instrument of this note. To recognize whether it is custom instrument use {@link #isCustomInstrument()}
     *
     * @return index of the instrument
     */
    int getInstrument();

    /**
     * Returns whether the note uses custom instrument.
     *
     * @return true if used instrument is custom instrument; otherwise, false
     */
    boolean isCustomInstrument();

    /**
     * Returns the key of the note.
     *
     * @return value in range [0; 87]; 0 is A0 and 87 is C8.
     */
    int getKey();

    /**
     * Returns fine pitch of the note.
     *
     * @return 0 is no fine pitch; +-100 is semitone difference
     */
    int getPitch();

    /**
     * Returns value of stereo offset of this note.
     *
     * @return value in range [-100; 100]; -100 two blocks left; 0 center; 100 two blocks right
     */
    int getPanning();

    /**
     * Returns the volume of this note.
     *
     * @return value in range [0; 100]
     */
    byte getVolume();

    static Builder builder() {
        return builder(false);
    }

    static Builder builder(boolean isStrict) {
        return new Builder(isStrict);
    }

    @NotNull
    static Builder builder(@NotNull Note note) {
        return new Builder(note, false);
    }

    @NotNull
    static Builder builder(@NotNull Note note, boolean isStrict) {
        return new Builder(note, isStrict);
    }

    final class Builder {
        private final boolean isStrict;
        int instrument = 0;
        boolean isCustomInstrument = false;
        int key = 45;
        int pitch = 0;
        int panning = 0;
        byte volume = 100;

        private Builder(boolean isStrict) {
            this.isStrict = isStrict;
        }

        private Builder(@NotNull Note note, boolean isStrict) {
            this(isStrict);
            instrument = note.getInstrument();
            isCustomInstrument = note.isCustomInstrument();
            key = note.getKey();
            pitch = note.getPitch();
            panning = note.getPanning();
            volume = note.getVolume();
        }

        /**
         * Sets index of the non-custom instrument of this note.
         *
         * @param instrument Index of the instrument. If the {@link Builder} is not in strict mode, values are clipped to nearest valid value.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the instrument is negative
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder instrument(int instrument) {
            return instrument(instrument, false);
        }

        /**
         * Sets the non-custom instrument of this note.
         *
         * @param instrument {@link Instrument}
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder instrument(Instrument instrument) {
            return instrument(instrument.getId(), false);
        }

        /**
         * Sets index of the instrument of this note and whether it is custom instrument.
         *
         * @param instrument Index of the instrument. If the {@link Builder} is not in strict mode, values are clipped to nearest valid value.
         * @param isCustom   Whether it is a custom instrument (custom instruments have their own indexing)
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the instrument is negative
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder instrument(int instrument, boolean isCustom) {
            if (instrument < 0) {
                if (isStrict) {
                    throw new IllegalArgumentException("Instrument index can not be negative");
                }
                instrument = 0;
            }

            this.instrument = instrument;
            isCustomInstrument = isCustom;

            return this;
        }

        /**
         * Sets the key of this note.
         *
         * @param key Value 0 is A0 and 87 is C8. If the {@link Builder} is not in strict mode, values are clipped to nearest valid value.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the value is out of range [{@link #MINIMUM_KEY}, {@link #MAXIMUM_KEY}] inclusive.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder key(int key) {
            key = normalizeRangeOrThrow(key, MINIMUM_KEY, MAXIMUM_KEY, "Key");

            this.key = (byte) key;
            return this;
        }

        /**
         * Sets the volume of this note.
         *
         * @param volume Value between {@link #MINIMUM_VOLUME} and {@link #MAXIMUM_VOLUME}. If the {@link Builder} is not in strict mode, values are clipped to nearest valid value.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the value is out of range [{@link #MINIMUM_VOLUME}, {@link #MAXIMUM_VOLUME}] inclusive.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder volume(int volume) {
            volume = normalizeRangeOrThrow(volume, MINIMUM_VOLUME, MAXIMUM_VOLUME, "Volume");

            this.volume = (byte) volume;
            return this;
        }

        /**
         * Sets the fine pitch of note.
         *
         * @param pitch 0 is no fine pitch; +-100 is semitone difference
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder pitch(int pitch) {
            this.pitch = pitch;
            return this;
        }

        /**
         * Sets the stereo offset of the note.
         *
         * @param panning -100 two blocks left; 0 center; 100 two blocks right. If the {@link Builder} is not in strict mode, values are clipped to nearest valid value.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the value is out of range [-{@link #MAXIMUM_PANNING}; {@link #MAXIMUM_PANNING}] inclusive
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder panning(int panning) {
            panning = normalizeRangeOrThrow(panning, -MAXIMUM_PANNING, MAXIMUM_PANNING, "Panning");

            this.panning = panning;
            return this;
        }

        private int normalizeRangeOrThrow(int value, int min, int max, String parameterDisplayName) {
            if (isStrict && (value < min || value > max)) {
                throw new IllegalArgumentException(parameterDisplayName + " must be in range [" + min + "; " + max + "] inclusive.");
            }

            return Math.min(Math.max(value, min), max);
        }

        /**
         * Creates new instance of {@link Note} based on data from this builder.
         *
         * @return {@link Note}
         */
        @NotNull
        public Note build() {
            return new NoteImpl(this);
        }
    }
}
