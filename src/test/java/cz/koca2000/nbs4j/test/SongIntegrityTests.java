package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.Layer;
import cz.koca2000.nbs4j.Note;
import cz.koca2000.nbs4j.Song;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SongIntegrityTests {

    Song.Builder songBuilder;

    @BeforeEach
    void prepareEmptySongWithLayers(){
        songBuilder = new Song.Builder()
                .setLayersCount(2);
    }

    @Test
    void emptySongZeroLength(){
        assertEquals(0, songBuilder.build().getSongLength());
    }

    @Test
    void noteChangeSongLength(){
        songBuilder.addNoteToLayerAtTick(0, 10, builder -> {});

        assertEquals(11, songBuilder.build().getSongLength());
    }

    @Test
    void multipleNotesChangeSongLengthSameTick(){
        songBuilder.addNoteToLayerAtTick(0, 5, builder -> {})
                .addNoteToLayerAtTick(1, 5, builder -> {});

        assertEquals(6, songBuilder.build().getSongLength());
    }

    @Test
    void multipleNotesChangeSongLengthDifferentTick(){
        songBuilder.addNoteToLayerAtTick(0, 5, builder -> {})
                .addNoteToLayerAtTick(1, 10, builder -> {});

        assertEquals(11, songBuilder.build().getSongLength());
    }

    @Test
    void noteOverwriteSongLength(){
        songBuilder.setLength(5)
                .addNoteToLayerAtTick(0, 10, builder -> {});

        assertEquals(11, songBuilder.build().getSongLength());
    }

    @Test
    void setLengthLowerThanLastNote(){
        songBuilder.addNoteToLayerAtTick(0, 10, builder -> {});

        assertThrows(IllegalArgumentException.class, () -> songBuilder.setLength(5));
    }

    @Test
    void nextTickEmptySong(){
        assertEquals(-1, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickSingleNote(){
        songBuilder.addNoteToLayerAtTick(0, 5, builder -> {});

        assertEquals(5, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickMultipleNotes(){
        songBuilder.addNoteToLayerAtTick(0, 5, builder -> {})
                .addNoteToLayerAtTick(1, 10, builder -> {});

        assertEquals(5, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickWithOffset(){
        songBuilder.addNoteToLayerAtTick(0, 5, builder -> {})
                .addNoteToLayerAtTick(1, 10, builder -> {});

        assertEquals(10, songBuilder.build().getNextNonEmptyTick(5));
    }

    @Test
    void nextTickTempoChange(){
        songBuilder.addNoteToLayerAtTick(1, 10, builder -> {})
                .setTempoChange(3, 5);

        assertEquals(3, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nonCustomInstrumentCountEmptySong(){
        assertEquals(0, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountSingleNote(){
        songBuilder.addNoteToLayerAtTick(0, 10, builder -> builder.setInstrument(5));

        assertEquals(6, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountMultipleNotes(){
        songBuilder.addNoteToLayerAtTick(0, 10, builder -> builder.setInstrument(5))
                .addNoteToLayerAtTick(0, 5, builder -> builder.setInstrument(3));

        assertEquals(6, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountOnlyCustomInstruments(){
        songBuilder.addNoteToLayerAtTick(0, 5, builder -> builder.setInstrument(2, true));

        assertEquals(0, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountInstrumentIndexZero(){
        songBuilder.addNoteToLayerAtTick(10, 0, builder -> builder.setInstrument(0));

        assertEquals(1, songBuilder.build().getNonCustomInstrumentsCount());
    }

}
