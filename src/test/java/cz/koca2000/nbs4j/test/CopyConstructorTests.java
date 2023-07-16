package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.CustomInstrument;
import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CopyConstructorTests {

    @Test
    void noteCopy(){
        Song song = new Song.Builder()
                .addNoteToLayerAtTick(0, 0, note ->
                        note.setInstrument(10, true)
                                .setKey(49)
                                .setPitch(50)
                                .setPanning(10)
                                .setVolume(90)
                )
                .build();
        Note note = song.getLayer(0).getNote(0);

        assertNotNull(note);

        Song song2 = new Song.Builder()
                .addNoteCopyToLayerAtTick(0,0, note, note2 -> {})
                .build();
        Note noteCopy = song2.getLayer(0).getNote(0);
        assertNotNull(noteCopy);

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
        Song song = new Song.Builder()
                .addLayer(layer ->
                        layer.setName("Layer 1")
                                .setPanning(20)
                                .setVolume(40)
                                .setLocked(true)
                )
                .build();
        Layer layer = song.getLayer(0);

        Song song2 = new Song.Builder()
                .addLayerCopy(layer, l -> {})
                .build();
        Layer layerCopy = song2.getLayer(0);

        assertNotEquals(layer, layerCopy);
        assertEquals(layer.getName(), layerCopy.getName());
        assertEquals(layer.getPanning(), layerCopy.getPanning());
        assertEquals(layer.getVolume(), layerCopy.getVolume());
        assertEquals(layer.isLocked(), layerCopy.isLocked());
        assertEquals(layer.getNotes().size(), layerCopy.getNotes().size());
    }

    @Test
    void songCopy(){
        Song song = new Song.Builder()
                .setLayersCount(5)
                .setLength(100)
                .addNoteToLayerAtTick(0, 10, n -> n.setInstrument(7))
                .getLayer(0, l -> l.setName("Layer 111"))
                .addCustomInstrument(new CustomInstrument.Builder()
                        .setName("Custom instrument 123")
                        .build())
                .build();

        Song songCopy = new Song.Builder(song).build();

        assertNotEquals(song, songCopy);
        assertEquals(song.getLayersCount(), songCopy.getLayersCount());
        assertEquals(song.getSongLength(), songCopy.getSongLength());
        assertNotEquals(song.getLayer(0), songCopy.getLayer(0));
        assertEquals(song.getLayer(0).getName(), songCopy.getLayer(0).getName());
        assertEquals(song.getLayer(0).getNote(10).getInstrument(), songCopy.getLayer(0).getNote(10).getInstrument());
        assertEquals(song.getCustomInstrumentsCount(), songCopy.getCustomInstrumentsCount());
        assertEquals(song.getCustomInstrument(0).getName(), songCopy.getCustomInstrument(0).getName());
    }

}
