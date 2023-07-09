package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongTests {

    @Test
    void songTempo(){
        Song song = new Song.Builder()
                .setTempoChange(-1, 5)
                .build();

        assertEquals(5, song.getTempo(-1));
        assertEquals(5, song.getTempo(0));
    }

    @Test
    void songNextTick(){
        Song song = new Song.Builder()
                .setLayersCount(1)
                .addNoteToLayerAtTick(0, 5, builder -> builder.setKey(5))
                .build();

        assertEquals(5, song.getNextNonEmptyTick(0));
    }

    @Test
    void songLengthInSeconds(){
        Song song = new Song.Builder()
                .setTempoChange(-1, 20)
                .setTempoChange(10, 10)
                .setLength(40)
                .build();

        assertEquals(0.5 + 3, song.getSongLengthInSeconds(), 0.001);
    }

    @Test
    void songTimeAtTick(){
        Song song = new Song.Builder()
                .setTempoChange(-1, 20)
                .setTempoChange(10, 10)
                .setLength(20)
                .build();

        assertEquals(0.05, song.getTimeInSecondsAtTick(1), 0.001);
        assertEquals(0.5, song.getTimeInSecondsAtTick(10), 0.001);
        assertEquals(0.5 + 1, song.getTimeInSecondsAtTick(21), 0.001);
    }
}
