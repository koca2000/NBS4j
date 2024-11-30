package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface Layer {
    int NEUTRAL_PANNING = 0;
    int MAXIMUM_PANNING = 100;

    int MINIMUM_VOLUME = 0;
    int MAXIMUM_VOLUME = 100;

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
    int getVolume();

    /**
     * Returns the display name of the layer
     *
     * @return name of the layer
     */
    @NotNull
    String getName();

    /**
     * Returns whether the layer is locked in OpenNoteBlockStudio
     *
     * @return true if the layer is locked; otherwise, false
     */
    boolean isLocked();

    /**
     * Returns whether the layer contains any notes
     *
     * @return true if there are no notes in this layer; otherwise, false
     */
    boolean isEmpty();

    /**
     * Returns note on specific tick on this layer.
     *
     * @param tick tick of the note
     * @return {@link Note} if there is a note on the give tick; otherwise, null
     */
    @Nullable
    Note getNote(long tick);

    /**
     * Returns unmodifiable {@link Map} of notes indexed by their tick.
     *
     * @return unmodifiable {@link Map}
     */
    @UnmodifiableView
    @NotNull
    Map<Long, Note> getNotes();

    @NotNull
    static Builder builder() {
        return builder(false);
    }

    @NotNull
    static Builder builder(boolean isStrict) {
        return new Builder(isStrict);
    }

    @NotNull
    static Builder builder(@NotNull Layer layer) {
        return builder(layer, false);
    }

    @NotNull
    static Builder builder(@NotNull Layer layer, boolean isStrict) {
        return new Builder(layer, isStrict);
    }

    final class Builder {
        private final boolean isStrict;
        private final InteractiveReplacer replacer = new InteractiveReplacer();

        final HashMap<Long, Note> notes = new HashMap<>();
        String name = "";
        int volume = 100;
        int panning = 0;
        boolean isLocked = false;

        private Builder(boolean isStrict) {
            this.isStrict = isStrict;
        }

        private Builder(@NotNull Layer layer, boolean isStrict) {
            this(isStrict);
            name = layer.getName();
            volume = layer.getVolume();
            panning = layer.getPanning();
            isLocked = layer.isLocked();

            for (Map.Entry<Long, Note> entry : layer.getNotes().entrySet()) {
                note(entry.getKey(), entry.getValue());
            }
        }

        /**
         * Enables interactive way of replacing certain parts of the layer for a single call.
         * @return {@link InteractiveReplacer}
         */
        public InteractiveReplacer replace() {
            return replacer;
        }

        /**
         * Sets whether the layer is locked in OpenNoteBlockStudio
         *
         * @param locked whether the layer is locked
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder locked(boolean locked) {
            this.isLocked = locked;
            return this;
        }

        /**
         * Sets the display name of the layer.
         *
         * @param name name of the layer
         * @throws NullPointerException if the {@code name} is null
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder name(@NotNull String name) {
            Objects.requireNonNull(name);

            this.name = name;
            return this;
        }

        /**
         * Sets the stereo offset of the layer.
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

        /**
         * Sets the volume of this layer.
         *
         * @param volume Value between {@link #MINIMUM_VOLUME} and {@link #MAXIMUM_VOLUME}. If the {@link Builder} is not in strict mode, values are clipped to nearest valid value.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the value is out of range [{@link #MINIMUM_VOLUME}, {@link #MAXIMUM_VOLUME}] inclusive.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder volume(int volume) {
            volume = normalizeRangeOrThrow(volume, MINIMUM_VOLUME, MAXIMUM_VOLUME, "Volume");

            this.volume = volume;
            return this;
        }

        /**
         * Sets the note at the given tick of this layer.
         *
         * @param tick Tick of the note
         * @param note {@link Note} to be added to the layer. If null, there will be no note at the given tick.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the {@code tick} value is out of range [0, {@link Long#MAX_VALUE}] inclusive.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder note(long tick, @Nullable Note note) {
            return note(tick, note, true);
        }

        /**
         * Sets the note at the given tick of this layer.
         *
         * @param tick Tick of the note
         * @param note {@link Note} to be added to the layer. If null, there will be no note at the given tick.
         * @param allowOverwrite If false, attempt to overwrite an existing note is ignored or an exception is thrown.
         *                       Otherwise, the possibly existing note at the given tick is overwritten.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the {@code tick} value is out of range [0, {@link Long#MAX_VALUE}] inclusive.
         * @throws IllegalStateException if the {@link Builder} is in strict mode and the attempt to overwrite a note failed because {@code allowOverwrite} is false.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder note(long tick, @Nullable Note note, boolean allowOverwrite) {
            tick = normalizeRangeOrThrow(tick, 0L, Long.MAX_VALUE, "Tick");
            if (!allowOverwrite && notes.containsKey(tick)) {
                if (isStrict) {
                    throw new IllegalStateException("Attempted to overwrite a note when overwrite is not allowed.");
                }
                return this;
            }

            if (note == null) {
                notes.remove(tick);
            } else {
                notes.put(tick, note);
            }

            return this;
        }

        private <T extends Comparable<T>> T normalizeRangeOrThrow(T value, T min, T max, String parameterDisplayName) {
            if (isStrict && (value.compareTo(min) < 0 || value.compareTo(max) > 0)) {
                throw new IllegalArgumentException(parameterDisplayName + " must be in range [" + min + "; " + max + "] inclusive.");
            }

            if (value.compareTo(min) < 0) {
                return min;
            }

            if (value.compareTo(max) > 0) {
                return max;
            }

            return value;
        }

        /**
         * Creates new instance of {@link Layer} based on data from this builder.
         *
         * @return {@link Layer}
         */
        @NotNull
        public Layer build() {
            return new LayerImpl(this);
        }

        @FunctionalInterface
        public interface NoteReplacer {
            @Nullable
            Note replace(Note.Builder builder, @Nullable Note originalNote);
        }

        public class InteractiveReplacer {

            private InteractiveReplacer() {
            }

            /**
             * Sets the note at the given tick of this layer using the result of {@link NoteReplacer} callback.
             * If the callback returns null, there will be no note at the given tick.
             *
             * @param tick Tick of the note
             * @param noteReplacer {@link NoteReplacer} that is given an existing note that is currently at the given tick or null if there is none
             *                                         and a builder initialized with that note or empty builder if there is none.
             * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the {@code tick} value is out of range [0, {@link Long#MAX_VALUE}] inclusive.
             * @return this instance of {@link Builder}
             */
            @NotNull
            public Builder note(long tick, @NotNull NoteReplacer noteReplacer) {
                tick = normalizeRangeOrThrow(tick, 0L, Long.MAX_VALUE, "Tick");

                Note originalNote = notes.get(tick);
                Note.Builder noteBuilder = originalNote == null ? Note.builder(isStrict) : Note.builder(originalNote, isStrict);

                Note modifiedNote = noteReplacer.replace(noteBuilder, originalNote);

                if (modifiedNote == null) {
                    notes.remove(tick);
                } else {
                    notes.put(tick, modifiedNote);
                }

                return Builder.this;
            }
        }
    }
}
