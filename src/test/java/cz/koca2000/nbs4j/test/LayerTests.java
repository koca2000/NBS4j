package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LayerTests {

    Note note;
    Layer layer;
    Song song;

    @BeforeEach
    void prepare(){
        note = new Note()
                .setInstrument(4);
        layer = new Layer();
        song = new Song();
    }

    @Test
    void layerWithoutSongSetNote(){
        layer.setNote(5, note);

        assertFalse(layer.isEmpty());
        assertEquals(note, layer.getNote(5));
    }

    @Test
    void layerWithoutSongRemoveNote(){
        layer.setNote(5, note)
                .removeNote(5);

        assertTrue(layer.isEmpty());
        assertNull(layer.getNote(5));
    }

    @Test
    void layerWithNotesAddToSong(){
        layer.setNote(5, note);

        assertEquals(0, song.getSongLength());
        assertEquals(-1, song.getNextNonEmptyTick(-1));

        song.addLayer(layer);

        assertEquals(6, song.getSongLength());
        assertEquals(5, song.getNextNonEmptyTick(-1));
    }

    @Test
    void layerWithSongSetNoteThroughLayer(){
        song.addLayer(layer);

        layer.setNote(5, note);

        assertEquals(6, song.getSongLength());
        assertEquals(5, song.getNextNonEmptyTick(-1));
    }

    @Test
    void layerWithSongRemoveNoteThroughLayer(){
        song.addLayer(layer);

        layer.setNote(5, note)
                .removeNote(5);

        assertEquals(0, song.getSongLength());
        assertEquals(-1, song.getNextNonEmptyTick(-1));
    }
}
