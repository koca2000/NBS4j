package cz.koca2000.nbs4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Layer {

    public static final int NEUTRAL_PANNING = 0;
    public static final int MAXIMUM_PANNING = 100;

    private final HashMap<Integer, Note> notes = new HashMap<>();

    private String name = "";
    private int volume = 100;
    private int panning = 0;
    private boolean isLocked = false;

    private Song song;
    private boolean isFrozen = false;

    public Layer(){}

    /**
     * Copies parameters of the other layer. Notes are not copied. Copy is not frozen.
     * @param layer layer to be copied
     */
    public Layer(Layer layer){
        name = layer.name;
        volume = layer.volume;
        panning = layer.panning;
        isLocked = layer.isLocked;

        isFrozen = false;
        song = null;
    }

    Layer setSong(Song song){
        if (this.song != null)
            throw new IllegalStateException("Layer was already added to a song.");

        this.song = song;
        return this;
    }

    void removedFromSong(){
        this.song = null;
    }

    void setNote(int tick, Note note){
        note.setLayer(this);
        if (notes.containsKey(tick)){
            notes.get(tick).removedFromLayer();
        }
        notes.put(tick, note);
    }

    void freeze(){
        if (isFrozen)
            return;

        for (Note note : notes.values()){
            note.freeze();
        }

        isFrozen = true;
    }

    /**
     * Sets whether the layer is locked in OpenNoteBlockStudio
     * @param locked whether the layer is locked
     * @return this instance of {@link Layer}
     * @throws IllegalStateException if the layer is frozen and can not be modified
     */
    public Layer setLocked(boolean locked){
        throwIfFrozen();

        this.isLocked = locked;
        return this;
    }

    /**
     * Sets the display name of the layer.
     * @param name name of the layer
     * @return this instance of {@link Layer}
     * @throws IllegalStateException if the layer is frozen and can not be modified
     */
    public Layer setName(String name){
        throwIfFrozen();

        this.name = name;
        return this;
    }

    /**
     * Sets the stereo offset of the layer.
     * @param panning -100 two blocks left; 0 center; 100 two blocks right
     * @return this instance of {@link Layer}
     * @throws IllegalArgumentException if the panning is out of range [-100; 100] inclusive]
     * @throws IllegalStateException if the layer is frozen and can not be modified
     */
    public Layer setPanning(int panning){
        throwIfFrozen();

        if (panning < -MAXIMUM_PANNING || panning > MAXIMUM_PANNING)
            throw new IllegalArgumentException("Panning must be in range [-" + MAXIMUM_PANNING + "; " + MAXIMUM_PANNING +"] inclusive.");

        this.panning = panning;

        if (song != null && panning != NEUTRAL_PANNING)
            song.setStereo();

        return this;
    }

    /**
     * Sets the volume of this layer.
     * @param volume volume between 0 and 100.
     * @return this instance of {@link Layer}
     * @throws IllegalArgumentException if volume is outside of range [0; 100] inclusive.
     * @throws IllegalStateException if the layer is frozen and can not be modified
     */
    public Layer setVolume(int volume){
        throwIfFrozen();

        if (volume < 0 || volume > 100)
            throw new IllegalArgumentException("Volume must be in range [0; 100] inclusive.");

        this.volume = volume;
        return this;
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
     * Returns whether the layer is frozen and can not be modified.
     * @return true if the layer is frozen; otherwise, false
     */
    public boolean isFrozen(){
        return isFrozen;
    }

    /**
     * Returns note on specific tick on this layer.
     * @param tick tick of the note
     * @return {@link Note} if there is a note on the give tick; otherwise, null
     */
    public Note getNote(int tick){
        return notes.getOrDefault(tick, null);
    }

    /**
     * Returns unmodifiable {@link Map} of notes indexed by their tick.
     * @return unmodifiable {@link Map}
     */
    public Map<Integer, Note> getNotes(){
        return Collections.unmodifiableMap(notes);
    }

    /**
     * Returns index of this layer in its song.
     * @return index
     * @throws IllegalStateException if layer is not in any song.
     */
    public int getIndexInSong(){
        if (song == null)
            throw new IllegalStateException("Layer is not in any song.");
        for (int i = 0; i < song.getLayersCount(); i++){
            if (song.getLayer(i) == this)
                return i;
        }
        throw new IllegalStateException("Layer is not in any song.");
    }

    /**
     * Returns the song this layer belongs to.
     * @return {@link Song} if the layer was added to a song; otherwise, null
     */
    public Song getSong() {
        return song;
    }

    private void throwIfFrozen(){
        if (isFrozen)
            throw new IllegalStateException("Layer is frozen and can not be modified.");
    }
}
