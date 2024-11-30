package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class LayerImpl implements Layer {

    private final Map<Long, Note> notes;

    private final String name;
    private final int volume;
    private final int panning;
    private final boolean isLocked;

    /**
     * Makes a copy of the layer and its notes. Copy is not frozen.
     * @param builder layer to be copied
     */
    LayerImpl(@NotNull Builder builder){
        name = builder.name;
        volume = builder.volume;
        panning = builder.panning;
        isLocked = builder.isLocked;
        this.notes = Collections.unmodifiableMap(new HashMap<>(builder.notes));
    }

    @Override
    public int getPanning() {
        return panning;
    }

    @Override
    public int getVolume(){
        return volume;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public boolean isEmpty(){
        return notes.isEmpty();
    }

    @Override
    @Nullable
    public Note getNote(long tick) {
        return notes.get(tick);
    }

    @Override
    @NotNull
    public Map<Long, Note> getNotes(){
        return notes;
    }
}
