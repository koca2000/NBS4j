package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SongTests {

    @Test
    void songTempo(){
        Song song = Song.builder()
                .tempoChange(-1, 5)
                .build();

        assertEquals(5, song.getTempo(-1));
        assertEquals(5, song.getTempo(0));
    }

    @Test
    void songNextTick(){
        Song song = Song.builder()
                .layer(Layer.builder()
                        .note(5, Note.builder().key(5).build())
                        .build()
                )
                .build();

        assertEquals(5, song.getNextNonEmptyTick(0));
    }

    @Test
    void songNextTickTempoChange(){
        Song song = Song.builder()
                .layer(Layer.builder()
                        .note(5, Note.builder().key(5).build())
                        .build()
                )
                .tempoChange(3, 15)
                .build();

        assertEquals(3, song.getNextNonEmptyTick(0));
    }

    @Test
    void songNextTickAfterLayerRemoval(){
        Song song = Song.builder()
                .layer(Layer.builder()
                        .note(5, Note.builder().key(5).build())
                        .build()
                )
                .layer(Layer.builder()
                        .note(10, Note.builder().key(5).build())
                        .build()
                )
                .layer(0, null)
                .build();

        assertEquals(10, song.getNextNonEmptyTick(0));
    }

    @Test
    void songNextTickTempoChangeAfterLayerRemoval(){
        Song song = Song.builder()
                .layer(Layer.builder()
                        .note(5, Note.builder().key(5).build())
                        .build()
                )
                .tempoChange(7, 15)
                .layer(0, null)
                .build();

        assertEquals(7, song.getNextNonEmptyTick(0));
    }

    @Test
    void songLengthInSeconds(){
        Song song = Song.builder()
                .tempoChange(-1, 20)
                .tempoChange(10, 10)
                .length(40)
                .build();

        assertEquals(0.5 + 3, song.getSongLengthInSeconds(), 0.001);
    }

    @Test
    void songTimeAtTick(){
        Song song = Song.builder()
                .tempoChange(-1, 20)
                .tempoChange(10, 10)
                .length(20)
                .build();

        assertEquals(0.05, song.getTimeInSecondsAtTick(1), 0.001);
        assertEquals(0.5, song.getTimeInSecondsAtTick(10), 0.001);
        assertEquals(0.5 + 1, song.getTimeInSecondsAtTick(21), 0.001);
    }
}
