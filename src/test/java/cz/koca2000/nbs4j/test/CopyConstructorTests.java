package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.*;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class CopyConstructorTests {

    @Test
    void noteCopy(){
        Note note = Note.builder()
                .instrument(10, true)
                .key(49)
                .pitch(50)
                .panning(10)
                .volume(90)
                .build();

        Note noteCopy = Note.builder(note).build();

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
        Layer layer = Layer.builder()
                .name("Layer 1")
                .panning(20)
                .volume(40)
                .locked(true)
                .note(10, Note.builder().build())
                .build();

        Layer layerCopy = Layer.builder(layer).build();

        assertNotEquals(layer, layerCopy);
        assertEquals(layer.getName(), layerCopy.getName());
        assertEquals(layer.getPanning(), layerCopy.getPanning());
        assertEquals(layer.getVolume(), layerCopy.getVolume());
        assertEquals(layer.isLocked(), layerCopy.isLocked());
        assertEquals(layer.getNotes().size(), layerCopy.getNotes().size());
    }

    @Test
    void songCopy(){
        Song song = Song.builder()
                .layer(Layer.builder()
                        .name("Layer 111")
                        .note(10, Note.builder().instrument(Instrument.BELL).build())
                        .build()
                )
                .length(100)
                .layer(Layer.builder().build())
                .layer(Layer.builder().build())
                .layer(Layer.builder().build())
                .customInstrument(CustomInstrument.builder()
                        .setName("Custom instrument 123")
                        .build())
                .build();

        Song songCopy = Song.builder(song).build();

        assertNotEquals(song, songCopy);
        assertEquals(song.getLayersCount(), songCopy.getLayersCount());
        assertEquals(song.getSongLength(), songCopy.getSongLength());
        assertNotEquals(song.getLayer(0), songCopy.getLayer(0));
        assertEquals(song.getLayer(0).getName(), songCopy.getLayer(0).getName());
        assertNotNull(song.getLayer(0).getNote(10));
        assertNotNull(songCopy.getLayer(0).getNote(10));
        assertEquals(Objects.requireNonNull(song.getLayer(0).getNote(10)).getInstrument(), Objects.requireNonNull(songCopy.getLayer(0).getNote(10)).getInstrument());
        assertEquals(song.getCustomInstrumentsCount(), songCopy.getCustomInstrumentsCount());
        assertEquals(song.getCustomInstrument(0).getName(), songCopy.getCustomInstrument(0).getName());
    }
}
