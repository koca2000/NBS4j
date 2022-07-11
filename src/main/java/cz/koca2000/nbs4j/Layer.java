package cz.koca2000.nbs4j;

import java.util.HashMap;

public final class Layer {

    public static final int NEUTRAL_PANNING = 100;

    private final HashMap<Integer, Note> notes = new HashMap<>();

    private String name = "";
    private int volume = 100;
    private int panning = 100;
    private boolean isLocked = false;

    private Song song;
    private boolean isFrozen = false;

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

    public Note getNote(int tick){
        return notes.getOrDefault(tick, null);
    }

    void freeze(){
        if (isFrozen)
            return;

        for (Note note : notes.values()){
            note.freeze();
        }

        isFrozen = true;
    }

    public Layer setLocked(boolean locked){
        throwIfFrozen();

        this.isLocked = locked;
        return this;
    }

    public Layer setName(String name){
        throwIfFrozen();

        this.name = name;
        return this;
    }

    public Layer setPanning(int panning){
        throwIfFrozen();

        this.panning = panning;

        if (song != null && panning != NEUTRAL_PANNING)
            song.setStereo();

        return this;
    }

    public Layer setVolume(int volume){
        throwIfFrozen();

        this.volume = volume;
        return this;
    }

    public int getPanning() {
        return panning;
    }

    public int getVolume(){
        return volume;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isEmpty(){
        return notes.size() == 0;
    }

    public boolean isFrozen(){
        return isFrozen;
    }

    public Song getSong() {
        return song;
    }

    private void throwIfFrozen(){
        if (isFrozen)
            throw new IllegalStateException("Layer is frozen and can not be modified.");
    }
}
