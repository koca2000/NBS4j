package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LayerInSong implements Layer {

    private final Song song;
    private final Layer layer;
    private final @UnmodifiableView Map<Long, NoteInSong> notes;

    LayerInSong(Song song, Layer layer) {
        this.song = song;
        if (layer instanceof LayerInSong) {
            layer = ((LayerInSong) layer).getLayerData();
        }
        this.layer = layer;

        Map<Long, NoteInSong> notesInLayer = new HashMap<>();
        for (Map.Entry<Long, Note> noteEntry : layer.getNotes().entrySet()){
            notesInLayer.put(noteEntry.getKey(), new NoteInSong(this, noteEntry.getValue()));
        }

        notes = Collections.unmodifiableMap(notesInLayer);
    }

    @Override
    public int getPanning() {
        return layer.getPanning();
    }

    @Override
    public int getVolume() {
        return layer.getVolume();
    }

    @Override
    @NotNull
    public String getName() {
        return layer.getName();
    }

    @Override
    public boolean isLocked() {
        return layer.isLocked();
    }

    @Override
    public boolean isEmpty() {
        return layer.isEmpty();
    }

    @Override
    @Nullable
    public NoteInSong getNote(long tick) {
        return notes.get(tick);
    }

    @Override
    @UnmodifiableView
    @NotNull
    public Map<Long, Note> getNotes() {
        return layer.getNotes();
    }

    /**
     * Returns unmodifiable {@link Map} of notes with reference to this layer indexed by their tick.
     *
     * @return unmodifiable {@link Map}
     */
    @UnmodifiableView
    @NotNull
    public Map<Long, NoteInSong> getNotesInSong() {
        return notes;
    }

    /**
     * Returns the {@link Song} to which this layer belongs.
     *
     * @return {@link Song}
     */
    @NotNull
    public Song getSong() {
        return song;
    }

    /**
     * Returns the inner {@link Layer} instance.
     * @return {@link Layer}
     */
    @NotNull
    public Layer getLayerData() {
        return layer;
    }
}
