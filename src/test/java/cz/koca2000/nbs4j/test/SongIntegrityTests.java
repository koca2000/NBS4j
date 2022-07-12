package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SongIntegrityTests {

    Song song;

    @BeforeEach
    void prepareEmptySongWithLayers(){
        song = new Song()
                .setLayersCount(2);
    }

    @Test
    void emptySongZeroLength(){
        assertEquals(0, song.getSongLength());
    }

    @Test
    void noteChangeSongLength(){
        song.setNote(10, 0, new Note());

        assertEquals(11, song.getSongLength());
    }

    @Test
    void multipleNotesChangeSongLengthSameTick(){
        song.setNote(5, 0, new Note());
        song.setNote(5, 1, new Note());

        assertEquals(6, song.getSongLength());
    }

    @Test
    void multipleNotesChangeSongLengthDifferentTick(){
        song.setNote(5, 0, new Note());
        song.setNote(10, 1, new Note());

        assertEquals(11, song.getSongLength());
    }

    @Test
    void noteOverwriteSongLength(){
        song.setLength(5)
                .setNote(10, 0, new Note());

        assertEquals(11, song.getSongLength());
    }

    @Test
    void setLengthLowerThanLastNote(){
        song.setNote(10, 0, new Note());

        assertThrows(IllegalArgumentException.class, () -> song.setLength(5));
    }

    @Test
    void noteReplace(){
        Note firstNote = new Note();
        Note secondNote = new Note();
        Layer layer = song.getLayer(0);

        song.setNote(5,0, firstNote);

        assertEquals(layer, firstNote.getLayer());

        song.setNote(5,0, secondNote);

        assertNull(firstNote.getLayer());
        assertEquals(layer, secondNote.getLayer());
    }

    @Test
    void layerReplace(){
        Layer originalLayer = song.getLayer(0);
        Layer newLayer = new Layer();

        assertEquals(song, originalLayer.getSong());

        song.setLayer(0, newLayer);

        assertNull(originalLayer.getSong());
        assertEquals(song, newLayer.getSong());
    }

    @Test
    void nextTickEmptySong(){
        assertEquals(-1, song.getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickSingleNote(){
        song.setNote(5, 0, new Note());

        assertEquals(5, song.getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickMultipleNotes(){
        song.setNote(5, 0, new Note())
                .setNote(10, 1, new Note());

        assertEquals(5, song.getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickWithOffset(){
        song.setNote(5, 0, new Note())
                .setNote(10, 1, new Note());

        assertEquals(10, song.getNextNonEmptyTick(5));
    }

    @Test
    void nextTickTempoChange(){
        song.setNote(10, 1, new Note())
                .setTempoChange(3, 5);

        assertEquals(3, song.getNextNonEmptyTick(-1));
    }

    @Test
    void nonCustomInstrumentCountEmptySong(){
        assertEquals(0, song.getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountSingleNote(){
        song.setNote(10, 0, new Note().setInstrument(5));

        assertEquals(6, song.getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountMultipleNotes(){
        song.setNote(10, 0, new Note().setInstrument(5))
                .setNote(5, 0, new Note().setInstrument(3));

        assertEquals(6, song.getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountOnlyCustomInstruments(){
        song.setNote(5, 0, new Note().setInstrument(2, true));

        assertEquals(0, song.getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountInstrumentIndexZero(){
        song.setNote(10, 0, new Note().setInstrument(0));

        assertEquals(1, song.getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountNoteInstrumentChanged(){
        Note note = new Note().setInstrument(3);

        song.setNote(10, 0, note);

        assertEquals(4, song.getNonCustomInstrumentsCount());

        note.setInstrument(5);

        assertEquals(6, song.getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountNoteCustomInstrumentChanged(){
        Note note = new Note().setInstrument(3, true);

        song.setNote(10, 0, note);

        assertEquals(0, song.getNonCustomInstrumentsCount());

        note.setInstrument(5, true);

        assertEquals(0, song.getNonCustomInstrumentsCount());
    }

}
