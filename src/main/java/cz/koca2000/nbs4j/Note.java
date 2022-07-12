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

    void removedFromLayer(){
        this.layer = null;
    }

    public Note setInstrument(int instrument){
        return setInstrument(instrument, false);
    }

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

    public Note setKey(int key){
        throwIfFrozen();

        if (key < 0 || key > 87)
            throw new IllegalArgumentException("Key must be in range [0; 87] inclusive.");

        this.key = (byte) key;
        return this;
    }

    public Note setVolume(int volume){
        throwIfFrozen();

        if (volume < 0 || volume > 100)
            throw new IllegalArgumentException("Volume must be in range [0; 100] inclusive.");

        this.volume = (byte) volume;
        return this;
    }

    public Note setPitch(int pitch){
        throwIfFrozen();

        this.pitch = pitch;
        return this;
    }

    public Note setPanning(int panning){
        throwIfFrozen();

        if (panning < -100 || panning > 100)
            throw new IllegalArgumentException("Panning must be in range [-100; 100] inclusive.");

        this.panning = panning;

        if (layer != null && panning != NEUTRAL_PANNING)
            layer.getSong().setStereo();

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
