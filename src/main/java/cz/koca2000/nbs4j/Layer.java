package cz.koca2000.nbs4j;

import java.util.HashMap;

public final class Layer {

    public static final int NEUTRAL_PANNING = 100;

    private final HashMap<Integer, Note> notes = new HashMap<>();

    private String name = "";
    private int volume = 100;
    private int panning = 100;
    private boolean isLocked = false;

    private boolean isFrozen = false;

    void setNote(int tick, Note note){
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

    public Layer withLocked(boolean locked){
        throwIfFrozen();

        this.isLocked = locked;
        return this;
    }

    public Layer withName(String name){
        throwIfFrozen();

        this.name = name;
        return this;
    }

    public Layer withPanning(int panning){
        throwIfFrozen();

        this.panning = panning;
        return this;
    }

    public Layer withVolume(int volume){
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

    private void throwIfFrozen(){
        if (isFrozen)
            throw new IllegalStateException("Layer is frozen and can not be modified.");
    }
}
