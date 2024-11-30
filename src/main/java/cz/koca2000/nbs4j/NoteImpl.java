package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

final class NoteImpl implements Note {

    private final int instrument;
    private final boolean isCustomInstrument;
    private final int key;
    private final int pitch;
    private final int panning;
    private final byte volume;

    NoteImpl(@NotNull Builder noteBuilder){
        instrument = noteBuilder.instrument;
        isCustomInstrument = noteBuilder.isCustomInstrument;
        key = noteBuilder.key;
        pitch = noteBuilder.pitch;
        panning = noteBuilder.panning;
        volume = noteBuilder.volume;
    }

    @Override
    public int getInstrument() {
        return instrument;
    }

    @Override
    public boolean isCustomInstrument() {
        return isCustomInstrument;
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public int getPitch() {
        return pitch;
    }

    @Override
    public int getPanning() {
        return panning;
    }

    @Override
    public byte getVolume() {
        return volume;
    }
}
