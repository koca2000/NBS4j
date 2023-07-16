package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Layer {

    public static final int NEUTRAL_PANNING = 0;
    public static final int MAXIMUM_PANNING = 100;

    private final Map<Long, Note> notes;

    private final String name;
    private final int volume;
    private final int panning;
    private final boolean isLocked;

    private Song song = null;

    /**
     * Makes a copy of the layer and its notes. Copy is not frozen.
     * @param builder layer to be copied
     */
    private Layer(@NotNull Builder builder, @NotNull Map<Long, Note> notes){
        name = builder.name;
        volume = builder.volume;
        panning = builder.panning;
        isLocked = builder.isLocked;
        this.notes = Collections.unmodifiableMap(notes);
    }

    /**
     * Sets the {@link Song} during the initialization.
     * @param song Song to which this layer belongs
     * @throws IllegalStateException If the method was already called before.
     */
    void setSong(@NotNull Song song) {
        if (this.song != null) {
            throw new IllegalStateException("Layer already has the song set.");
        }
        this.song = song;
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
    public int getVolume(){
        return volume;
    }

    /**
     * Returns the display name of the layer
     * @return name of the layer
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns whether the layer is locked in OpenNoteBlockStudio
     * @return true if the layer is locked; otherwise, false
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Returns whether the layer contains any notes
     * @return true if there are no notes in this layer; otherwise, false
     */
    public boolean isEmpty(){
        return notes.size() == 0;
    }

    /**
     * Returns note on specific tick on this layer.
     * @param tick tick of the note
     * @return {@link Note} if there is a note on the give tick; otherwise, null
     */
    @Nullable
    public Note getNote(long tick){
        return notes.getOrDefault(tick, null);
    }

    /**
     * Returns unmodifiable {@link Map} of notes indexed by their tick.
     * @return unmodifiable {@link Map}
     */
    @NotNull
    public Map<Long, Note> getNotes(){
        return notes;
    }

    /**
     * Returns the {@link Song} to which this layer belongs.
     * @return {@link Song}
     */
    public @NotNull Song getSong() {
        return song;
    }

    public static final class Builder {
        private final HashMap<Long, Note.Builder> notes = new HashMap<>();
        private String name = "";
        private int volume = 100;
        private int panning = 0;
        private boolean isLocked = false;

        Builder() {
        }

        Builder(@NotNull Layer layer) {
            name = layer.name;
            volume = layer.volume;
            panning = layer.panning;
            isLocked = layer.isLocked;

            for (Map.Entry<Long, Note> entry : layer.notes.entrySet()) {
                setNoteInternal(entry.getKey(), new Note.Builder(entry.getValue()));
            }
        }

        /**
         * Puts the note to the layer at the given tick.
         * @param tick Tick of the note
         * @param note Note to be added
         */
        void setNoteInternal(long tick, @NotNull Note.Builder note){
            notes.put(tick, note);
        }

        /**
         * Removes the note on the given tick from the layer
         * @param tick Tick on which the note will be removed
         */
        void removeNoteInternal(long tick) {
            notes.remove(tick);
        }

        /**
         * Sets whether the layer is locked in OpenNoteBlockStudio
         * @param locked whether the layer is locked
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setLocked(boolean locked){
            this.isLocked = locked;
            return this;
        }

        /**
         * Sets the display name of the layer.
         * @param name name of the layer
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setName(@NotNull String name){
            this.name = name;
            return this;
        }

        /**
         * Sets the stereo offset of the layer.
         * @param panning -100 two blocks left; 0 center; 100 two blocks right
         * @return this instance of {@link Builder}
         * @throws IllegalArgumentException if the panning is out of range [-100; 100] inclusive]
         */
        @NotNull
        public Builder setPanning(int panning){
            if (panning < -MAXIMUM_PANNING || panning > MAXIMUM_PANNING)
                throw new IllegalArgumentException("Panning must be in range [-" + MAXIMUM_PANNING + "; " + MAXIMUM_PANNING +"] inclusive.");

            this.panning = panning;

            return this;
        }

        /**
         * Sets the volume of this layer.
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

            this.volume = volume;
            return this;
        }

        /**
         * Returns note on the given tick
         * @param tick Tick of the note
         * @return {@link Note} or null
         */
        Note.Builder getNote(long tick){
            return notes.getOrDefault(tick, null);
        }

        /**
         * Returns map of notes in the layer
         * @return Map with tick as a key and {@link Note.Builder} as value
         */
        @NotNull
        Map<Long, Note.Builder> getNotes(){
            return notes;
        }

        /**
         * Creates new instance of {@link Layer} based on data from this builder.
         * @return {@link Layer}
         */
        @NotNull Layer build() {
            HashMap<Long, Note> builtNotes = new HashMap<>();
            for (Map.Entry<Long, Note.Builder> noteEntry : notes.entrySet()) {
                builtNotes.put(noteEntry.getKey(), noteEntry.getValue().build());
            }
            Layer layer = new Layer(this, builtNotes);

            for (Note note : builtNotes.values()) {
                note.setLayer(layer);
            }
            return layer;
        }
    }
}
