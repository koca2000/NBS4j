package cz.koca2000.nbs4j;

public final class Note {

    public static final int NEUTRAL_PANNING = 100;

    private Layer layer;

    private int instrument = 0;
    private boolean isCustomInstrument = false;
    private int key = 45;
    private int pitch = 0;
    private int panning = 100;
    private byte volume = 100;

    private boolean isFrozen = false;

    void setLayer(Layer layer){
        if (this.layer != null)
            throw new IllegalStateException("Note was already added to a layer.");

        this.layer = layer;
    }

    public Note withInstrument(int instrument){
        return withInstrument(instrument, false);
    }

    public Note withInstrument(int instrument, boolean isCustom){
        throwIfFrozen();

        if (instrument < 0)
            throw new IllegalArgumentException("Instrument index can not be negative");

        this.instrument = instrument;
        isCustomInstrument = isCustom;

        return this;
    }

    public Note withKey(byte key){
        throwIfFrozen();

        this.key = key;
        return this;
    }

    public Note withVolume(byte volume){
        throwIfFrozen();

        this.volume = volume;
        return this;
    }

    public Note withPitch(int pitch){
        throwIfFrozen();

        this.pitch = pitch;
        return this;
    }

    public Note withPanning(int panning){
        throwIfFrozen();

        this.panning = panning;
        return this;
    }

    void freeze(){
        isFrozen = true;
    }

    public int getInstrument() {
        return instrument;
    }

    public boolean isCustomInstrument() {
        return isCustomInstrument;
    }

    public int getKey() {
        return key;
    }

    public int getPitch() {
        return pitch;
    }

    public int getPanning() {
        return panning;
    }

    public byte getVolume() {
        return volume;
    }

    public Layer getLayer() {
        return layer;
    }

    private void throwIfFrozen(){
        if (isFrozen)
            throw new IllegalStateException("Layer is frozen and can not be modified.");
    }
}
