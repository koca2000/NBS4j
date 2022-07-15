package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SongTests {

    @Test
    void songTempo(){
        Song song = new Song()
                .setTempoChange(-1, 5);

        assertEquals(5, song.getTempo(-1));
        assertEquals(5, song.getTempo(0));
    }

    @Test
    void songNextTick(){
        Song song = new Song()
                .setLayersCount(1)
                .setNote(5, 0, new Note());

        assertEquals(5, song.getNextNonEmptyTick(0));
    }
}
