package cz.koca2000.nbs4j;

public final class NoteInSong implements Note {

    private final LayerInSong layer;
    private final Note note;

    NoteInSong(LayerInSong layer, Note note) {
        this.layer = layer;
        if (note instanceof NoteInSong) {
            note = ((NoteInSong) note).getNoteData();
        }
        this.note = note;
    }

    @Override
    public int getInstrument() {
        return note.getInstrument();
    }

    @Override
    public boolean isCustomInstrument() {
        return note.isCustomInstrument();
    }

    @Override
    public int getKey() {
        return note.getKey();
    }

    @Override
    public int getPitch() {
        return note.getPitch();
    }

    @Override
    public int getPanning() {
        return note.getPanning();
    }

    @Override
    public byte getVolume() {
        return note.getVolume();
    }

    /**
     * Returns the {@link LayerImpl} to which the note belongs
     * @return {@link LayerImpl}
     */
    public LayerInSong getLayer() {
        return layer;
    }

    /**
     * Returns the inner {@link Note} instance.
     * @return {@link Note}
     */
    public Note getNoteData() {
        return note;
    }
}
