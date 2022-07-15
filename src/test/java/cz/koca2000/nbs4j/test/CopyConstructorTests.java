package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.CustomInstrument;
import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CopyConstructorTests {

    @Test
    void noteCopy(){
        Note note = new Note()
                .setInstrument(10, true)
                .setKey(49)
                .setPitch(50)
                .setPanning(10)
                .setVolume(90);

        Note noteCopy = new Note(note);

        assertNotEquals(note, noteCopy);
        assertEquals(note.getInstrument(), noteCopy.getInstrument());
        assertEquals(note.isCustomInstrument(), noteCopy.isCustomInstrument());
        assertEquals(note.getKey(), noteCopy.getKey());
        assertEquals(note.getPitch(), noteCopy.getPitch());
        assertEquals(note.getPanning(), noteCopy.getPanning());
        assertEquals(note.getVolume(), noteCopy.getVolume());
    }

    @Test
    void layerCopy(){
        Layer layer = new Layer()
                .setName("Layer 1")
                .setPanning(20)
                .setVolume(40)
                .setLocked(true)
                .setNote(5, new Note()
                        .setInstrument(8));

        Layer layerCopy = new Layer(layer);

        assertNotEquals(layer, layerCopy);
        assertEquals(layer.getName(), layerCopy.getName());
        assertEquals(layer.getPanning(), layerCopy.getPanning());
        assertEquals(layer.getVolume(), layerCopy.getVolume());
        assertEquals(layer.isLocked(), layerCopy.isLocked());
        assertEquals(layer.getNotes().size(), layerCopy.getNotes().size());
        assertEquals(layer.getNote(5).getInstrument(), layerCopy.getNote(5).getInstrument());
    }

    @Test
    void songCopy(){
        Song song = new Song()
                .setLayersCount(5)
                .setLength(100)
                .setNote(10, 0, new Note()
                        .setInstrument(7))
                .addCustomInstrument(new CustomInstrument()
                        .setName("Custom instrument 123"));

        song.getLayer(0).setName("Layer 111");

        Song songCopy = new Song(song);

        assertNotEquals(song, songCopy);
        assertEquals(song.getLayersCount(), songCopy.getLayersCount());
        assertEquals(song.getSongLength(), songCopy.getSongLength());
        assertNotEquals(song.getLayer(0), songCopy.getLayer(0));
        assertEquals(song.getLayer(0).getName(), songCopy.getLayer(0).getName());
        assertNotEquals(song.getLayer(0).getNote(10), songCopy.getLayer(0).getNote(10));
        assertEquals(song.getLayer(0).getNote(10).getInstrument(), songCopy.getLayer(0).getNote(10).getInstrument());
        assertEquals(song.getCustomInstrumentsCount(), songCopy.getCustomInstrumentsCount());
        assertNotEquals(song.getCustomInstrument(0), songCopy.getCustomInstrument(0));
        assertEquals(song.getCustomInstrument(0).getName(), songCopy.getCustomInstrument(0).getName());
    }

}
