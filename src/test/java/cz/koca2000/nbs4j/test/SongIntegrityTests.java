package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SongIntegrityTests {

    Song.Builder songBuilder;
    Note emptyNote;

    @BeforeEach
    void prepareEmptySong(){
        songBuilder = Song.builder();
        emptyNote = Note.builder().build();
    }

    @Test
    void emptySongZeroLength(){
        assertEquals(0, songBuilder.build().getSongLength());
    }

    @Test
    void noteChangeSongLength(){
        songBuilder.layer(Layer.builder()
                .note(10, emptyNote)
                .build()
        );

        assertEquals(11, songBuilder.build().getSongLength());
    }

    @Test
    void multipleNotesChangeSongLengthSameTick(){
        songBuilder
                .layer(Layer.builder()
                        .note(5, emptyNote)
                        .build()
                )
                .layer(Layer.builder()
                        .note(5, emptyNote)
                        .build()
                );

        assertEquals(6, songBuilder.build().getSongLength());
    }

    @Test
    void multipleNotesChangeSongLengthDifferentTick(){
        songBuilder
                .layer(Layer.builder()
                        .note(5, emptyNote)
                        .build()
                )
                .layer(Layer.builder()
                        .note(10, emptyNote)
                        .build()
                );

        assertEquals(11, songBuilder.build().getSongLength());
    }

    @Test
    void noteOverwriteSongLength(){
        songBuilder.length(5)
                .layer(Layer.builder()
                        .note(10, emptyNote)
                        .build()
                );

        assertEquals(11, songBuilder.build().getSongLength());
    }

    @Test
    void setLengthLowerThanLastNote(){
        songBuilder
                .layer(Layer.builder()
                        .note(10, emptyNote)
                        .build()
                );

        assertThrows(IllegalArgumentException.class, () -> songBuilder.length(5));
    }

    @Test
    void nextTickEmptySong(){
        assertEquals(-1, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickSingleNote(){
        songBuilder
                .layer(Layer.builder()
                        .note(5, emptyNote)
                        .build()
                );

        assertEquals(5, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickMultipleNotes(){
        songBuilder
                .layer(Layer.builder()
                        .note(5, emptyNote)
                        .build()
                )
                .layer(Layer.builder()
                        .note(10, emptyNote)
                        .build()
                );

        assertEquals(5, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nextTickWithOffset(){
        songBuilder
                .layer(Layer.builder()
                        .note(5, emptyNote)
                        .build()
                )
                .layer(Layer.builder()
                        .note(10, emptyNote)
                        .build()
                );

        assertEquals(10, songBuilder.build().getNextNonEmptyTick(5));
    }

    @Test
    void nextTickTempoChange(){
        songBuilder
                .layer(Layer.builder()
                        .note(10, emptyNote)
                        .build()
                )
                .tempoChange(3, 5);

        assertEquals(3, songBuilder.build().getNextNonEmptyTick(-1));
    }

    @Test
    void nonCustomInstrumentCountEmptySong(){
        assertEquals(0, songBuilder.build().getNonCustomInstrumentsCount());
    }


    @Test
    void nonCustomInstrumentCountSingleNote(){
        songBuilder
                .layer(Layer.builder()
                        .note(10, Note.builder().instrument(5).build())
                        .build()
                );

        assertEquals(6, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountMultipleNotes(){
        songBuilder
                .layer(Layer.builder()
                        .note(10, Note.builder().instrument(5).build())
                        .note(5, Note.builder().instrument(3).build())
                        .build()
                );

        assertEquals(6, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountOnlyCustomInstruments(){
        songBuilder
                .layer(Layer.builder()
                        .note(5, Note.builder().instrument(2, true).build())
                        .build()
                );

        assertEquals(0, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void nonCustomInstrumentCountInstrumentIndexZero(){
        songBuilder
                .layer(Layer.builder()
                        .note(10, Note.builder().instrument(0).build())
                        .build()
                );

        assertEquals(1, songBuilder.build().getNonCustomInstrumentsCount());
    }

    @Test
    void layerGetSong() {
        Song song = songBuilder.layer(Layer.builder().name("Test layer").build()).build();

        LayerInSong layer = song.getLayer(0);
        assertNotNull(layer);
        assertEquals("Test layer", layer.getName());
        assertEquals(song, layer.getSong());
    }

    @Test
    void noteGetLayer() {
        Song song = songBuilder
                .layer(Layer.builder()
                        .note(0, Note.builder().key(70).build())
                        .build()
                )
                .build();
        LayerInSong layer = song.getLayer(0);

        assertNotNull(layer);

        NoteInSong note = layer.getNote(0);

        assertNotNull(note);
        assertEquals(70, note.getKey());
        assertEquals(layer, note.getLayer());
    }
}
