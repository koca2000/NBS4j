package cz.koca2000.nbs4j.test;

import cz.koca2000.nbs4j.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaveLoadTests {

    static Song originalSong;
    static Song originalSongWithoutTempoChanger;

    @BeforeAll
    static void prepareSong(){
        originalSongWithoutTempoChanger = Song.builder()
                .layer(Layer.builder()
                        .name("Test layer 1")
                        .volume(50)
                        .panning(50)
                        .locked(true)
                        .note(5, Note.builder()
                                .instrument(6)
                                .volume(20)
                                .key(70)
                                .pitch(10)
                                .build()
                        )
                        .build()
                )
                .layer(Layer.builder()
                        .name("Test layer 2")
                        .volume(25)
                        .panning(-50)
                        .note(10, Note.builder()
                                .instrument(0, true)
                                .volume(40)
                                .key(30)
                                .pitch(-10)
                                .build()
                        )
                        .build()
                )
                .customInstrument(CustomInstrument.builder()
                        .setName("Custom Instrument 1")
                        .setFileName("file/name")
                        .setKey(10)
                        .setShouldPressKey(true).build())
                .initialTempo(8.0f)
                .build();

        originalSong = Song.builder(originalSongWithoutTempoChanger)
                .tempoChange(5, 20)
                .tempoChange(15, 20)
                .build();
    }

    @ParameterizedTest
    @EnumSource
    void layerName(NBSVersion nbsVersion){
        Song savedSong = saveAndLoad(originalSong, nbsVersion);

        assertEquals(originalSong.getLayersCount(), savedSong.getLayersCount());
        for (int i = 0; i < originalSong.getLayersCount(); i++){
            assertEquals(originalSong.getLayer(i).getName(), savedSong.getLayer(i).getName());
        }
    }

    @ParameterizedTest
    @EnumSource
    void layerVolume(NBSVersion nbsVersion){
        Song savedSong = saveAndLoad(originalSong, nbsVersion);

        assertEquals(originalSong.getLayersCount(), savedSong.getLayersCount());
        for (int i = 0; i < originalSong.getLayersCount(); i++){
            assertEquals(originalSong.getLayer(i).getVolume(), savedSong.getLayer(i).getVolume());
        }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = { "V1" })
    void layerPanning(NBSVersion nbsVersion){
        Song savedSong = saveAndLoad(originalSong, nbsVersion);

        assertEquals(originalSong.getLayersCount(), savedSong.getLayersCount());
        for (int i = 0; i < originalSong.getLayersCount(); i++){
            assertEquals(originalSong.getLayer(i).getPanning(), savedSong.getLayer(i).getPanning());
        }
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = { "V1", "V2", "V3" })
    void layerIsLocked(NBSVersion nbsVersion){
        Song savedSong = saveAndLoad(originalSong, nbsVersion);

        assertEquals(originalSong.getLayersCount(), savedSong.getLayersCount());
        for (int i = 0; i < originalSong.getLayersCount(); i++){
            assertEquals(originalSong.getLayer(i).isLocked(), savedSong.getLayer(i).isLocked());
        }
    }

    @ParameterizedTest
    @EnumSource
    void layerContainsSameNotes(NBSVersion nbsVersion){
        Song comparingSong = nbsVersion.isNewerOrEqual(NBSVersion.V4) ? originalSong : originalSongWithoutTempoChanger;
        Song savedSong = saveAndLoad(comparingSong, nbsVersion);

        assertEquals(comparingSong.getLayersCount(), savedSong.getLayersCount());
        long nextTickOriginal = comparingSong.getNextNonEmptyTick(-1);
        long nextTickSaved = savedSong.getNextNonEmptyTick(-1);

        while (nextTickOriginal == nextTickSaved && nextTickOriginal != -1) {
            for (int i = 0; i < comparingSong.getLayersCount(); i++) {
                Note noteOriginal = comparingSong.getLayer(i).getNote(nextTickOriginal);
                Note noteSaved = savedSong.getLayer(i).getNote(nextTickSaved);

                assertEquals(noteOriginal != null, noteSaved != null);

                if (noteOriginal == null || noteSaved == null) {
                    continue;
                }

                assertEquals(noteOriginal.getInstrument(), noteSaved.getInstrument());
                assertEquals(noteOriginal.isCustomInstrument(), noteSaved.isCustomInstrument());
                assertEquals(noteOriginal.getKey(), noteSaved.getKey());
                if (nbsVersion.isNewerOrEqual(NBSVersion.V4)) {
                    assertEquals(noteOriginal.getPanning(), noteSaved.getPanning());
                    assertEquals(noteOriginal.getPitch(), noteSaved.getPitch());
                    assertEquals(noteOriginal.getVolume(), noteSaved.getVolume());
                }
            }
            nextTickOriginal = comparingSong.getNextNonEmptyTick(nextTickOriginal);
            nextTickSaved = savedSong.getNextNonEmptyTick(nextTickSaved);
        }

        assertEquals(nextTickOriginal, nextTickSaved);
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = { "V1", "V2", "V3" })
    void tempoChanges(NBSVersion nbsVersion) {
        Song savedSong = saveAndLoad(originalSong, nbsVersion);

        assertEquals(originalSong.getTempoChanges().size(), savedSong.getTempoChanges().size());
        for (Map.Entry<Long, Float> tempoChangeEntry : originalSong.getTempoChanges().entrySet()) {
            long tick = tempoChangeEntry.getKey();
            Float originalTempo = tempoChangeEntry.getValue();
            Float savedTempo = savedSong.getTempoChanges().get(tick);
            assertEquals(originalTempo, savedTempo);
        }
    }

    @ParameterizedTest
    @EnumSource
    void customInstrumentName(NBSVersion nbsVersion){
        Song comparingSong = originalSongWithoutTempoChanger;
        Song savedSong = saveAndLoad(comparingSong, nbsVersion);

        assertEquals(comparingSong.getCustomInstrumentsCount(), savedSong.getCustomInstrumentsCount());
        for (int i = 0; i < comparingSong.getCustomInstrumentsCount(); i++){
            assertEquals(comparingSong.getCustomInstrument(i).getName(), savedSong.getCustomInstrument(i).getName());
        }
    }

    @ParameterizedTest
    @EnumSource
    void customInstrumentFileName(NBSVersion nbsVersion){
        Song comparingSong = originalSongWithoutTempoChanger;
        Song savedSong = saveAndLoad(comparingSong, nbsVersion);

        assertEquals(comparingSong.getCustomInstrumentsCount(), savedSong.getCustomInstrumentsCount());
        for (int i = 0; i < comparingSong.getCustomInstrumentsCount(); i++){
            assertEquals(comparingSong.getCustomInstrument(i).getFileName(), savedSong.getCustomInstrument(i).getFileName());
        }
    }

    @ParameterizedTest
    @EnumSource
    void customInstrumentPitch(NBSVersion nbsVersion){
        Song comparingSong = originalSongWithoutTempoChanger;
        Song savedSong = saveAndLoad(comparingSong, nbsVersion);

        assertEquals(comparingSong.getCustomInstrumentsCount(), savedSong.getCustomInstrumentsCount());
        for (int i = 0; i < comparingSong.getCustomInstrumentsCount(); i++){
            assertEquals(comparingSong.getCustomInstrument(i).getKey(), savedSong.getCustomInstrument(i).getKey());
        }
    }

    @ParameterizedTest
    @EnumSource
    void customInstrumentPressKey(NBSVersion nbsVersion){
        Song comparingSong = originalSongWithoutTempoChanger;
        Song savedSong = saveAndLoad(comparingSong, nbsVersion);

        assertEquals(comparingSong.getCustomInstrumentsCount(), savedSong.getCustomInstrumentsCount());
        for (int i = 0; i < comparingSong.getCustomInstrumentsCount(); i++){
            assertEquals(comparingSong.getCustomInstrument(i).shouldPressKey(), savedSong.getCustomInstrument(i).shouldPressKey());
        }
    }

    @ParameterizedTest
    @EnumSource
    void tempo(NBSVersion nbsVersion){
        Song savedSong = saveAndLoad(originalSong, nbsVersion);

        assertEquals(originalSong.getTempo(0), savedSong.getTempo(0));
    }

    @ParameterizedTest
    @EnumSource
    void lengthInTicks(NBSVersion nbsVersion) {
        Song comparingSong = nbsVersion.isNewerOrEqual(NBSVersion.V4) ? originalSong : originalSongWithoutTempoChanger;
        Song savedSong = saveAndLoad(comparingSong, nbsVersion);

        assertEquals(comparingSong.getSongLength(), savedSong.getSongLength());
    }

    private static Song saveAndLoad(Song song, NBSVersion nbsVersion){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        song.save(nbsVersion, outputStream);
        return Song.fromStream(new ByteArrayInputStream(outputStream.toByteArray()));
    }
}
