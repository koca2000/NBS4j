package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class NBSReader {

    private NBSReader() {
    }

    @NotNull
    public static Song readSong(@NotNull InputStream stream) {
        Song.Builder song = Song.builder();

        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(stream));

            HeaderData header = readHeader(song, dataInputStream);

            List<Layer.Builder> layerBuilders = initializeLayerBuilders(dataInputStream);
            int layersCount = layerBuilders.size();

            readMetadata(song, header, dataInputStream);

            readNotes(header, layerBuilders, dataInputStream);

            readLayers(header, layerBuilders, layersCount, dataInputStream);

            List<CustomInstrument> customInstruments = readCustomInstruments(dataInputStream);

            List<Layer> layers = buildLayers(layerBuilders);

            int tempoChangerIndex = SongUtils.findTempoChangerInstrumentIndex(customInstruments);
            if (tempoChangerIndex != -1) {
                handleTempoChangerNotes(song, layers, tempoChangerIndex);
            }

            addCustomInstrumentsToSong(song, customInstruments);
            addLayersToSong(song, layers);

        } catch (Exception e) {
            throw new SongCorruptedException(e);
        }
        return song.build();
    }

    private static short readShort(@NotNull DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        return (short) (byte1 + (byte2 << 8));
    }

    private static int readInt(@NotNull DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        int byte3 = dataInputStream.readUnsignedByte();
        int byte4 = dataInputStream.readUnsignedByte();
        return (byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24));
    }

    @NotNull
    private static String readString(@NotNull DataInputStream dataInputStream) throws IOException {
        int length = readInt(dataInputStream);
        StringBuilder builder = new StringBuilder(length);
        for (; length > 0; --length) {
            char c = (char) dataInputStream.readByte();
            if (c == (char) 0x0D) {
                c = ' ';
            }
            builder.append(c);
        }
        return builder.toString();
    }

    @NotNull
    private static HeaderData readHeader(@NotNull Song.Builder song, @NotNull DataInputStream stream) throws IOException {
        HeaderData data = new HeaderData();

        short length = readShort(stream);
        if (length == 0) { // New nbs format
            data.version = stream.readByte();
            data.firstCustomInstrumentIndex = stream.readByte();

            if (data.version >= 3) // Until nbs 3 there wasn't length specified in the file
                song.length(readShort(stream));
        }
        else
            song.length(length);

        return data;
    }

    @NotNull
    private static List<Layer.Builder> initializeLayerBuilders(@NotNull DataInputStream stream) throws IOException {
        List<Layer.Builder> layers = new ArrayList<>();
        int count = readShort(stream);
        for (int i = 0; i < count; i++) {
            layers.add(Layer.builder());
        }
        return layers;
    }

    private static void readMetadata(@NotNull Song.Builder song, @NotNull HeaderData header, @NotNull DataInputStream stream) throws IOException {
        SongMetadata metadata = new SongMetadata();

        metadata.setTitle(readString(stream))
                .setAuthor(readString(stream))
                .setOriginalAuthor(readString(stream))
                .setDescription(readString(stream));
        song.initialTempo(readShort(stream) / 100f);
        metadata.setAutoSave(stream.readBoolean())
                .setAutoSaveDuration(stream.readByte())
                .setTimeSignature(stream.readByte())
                .setMinutesSpent(readInt(stream))
                .setLeftClicks(readInt(stream))
                .setRightClicks(readInt(stream))
                .setNoteBlocksAdded(readInt(stream))
                .setNoteBlocksRemoved(readInt(stream))
                .setOriginalMidiFileName(readString(stream));
        if (header.version >= 4) {
            metadata.setLoop(stream.readBoolean())
                    .setLoopMaxCount(stream.readByte())
                    .setLoopStartTick(readShort(stream));
        }
        song.metadata(metadata);
    }

    private static void readNotes(@NotNull HeaderData header, @NotNull List<Layer.Builder> layers, @NotNull DataInputStream stream) throws IOException {
        short tick = -1;
        while (true) {
            short jumpTicks = readShort(stream); // jumps till next tick

            if (jumpTicks == 0) {
                break;
            }
            tick += jumpTicks;

            short layer = -1;
            while (true) {
                short jumpLayers = readShort(stream); // jumps till next layer
                if (jumpLayers == 0) {
                    break;
                }
                layer += jumpLayers;

                readNote(header, tick, layer, layers, stream);
            }
        }
    }

    private static void readNote(@NotNull HeaderData header, short tick, short layer, @NotNull List<Layer.Builder> layers, @NotNull DataInputStream stream) throws IOException {
        byte instrument = stream.readByte();

        int instrumentId;
        boolean isCustomInstrument;
        if (instrument >= header.firstCustomInstrumentIndex) {
            instrumentId = instrument - header.firstCustomInstrumentIndex;
            isCustomInstrument = true;
        } else {
            instrumentId = instrument;
            isCustomInstrument = false;
        }

        byte key = stream.readByte();
        byte volume;
        int panning;
        short pitch;
        if (header.version >= 4) {
            volume = stream.readByte();
            panning = 100 - stream.readUnsignedByte(); // 0 is 2 blocks right in nbs format, we want -100 to be left and 100 to be right
            pitch = readShort(stream);
        } else {
            volume = 100;
            panning = 0;
            pitch = 0;
        }

        if (layer >= layers.size()) {
            layers.add(Layer.builder());
        }

        layers.get(layer)
                .note(tick, Note.builder()
                        .instrument(instrumentId, isCustomInstrument)
                        .key(key)
                        .volume(volume)
                        .panning(panning)
                        .pitch(pitch)
                        .build()
                );
    }

    private static void readLayers(@NotNull HeaderData header, @NotNull List<Layer.Builder> layers, int layersCount, @NotNull DataInputStream stream) throws IOException {
        for (int i = 0; i < layersCount; i++) {
            Layer.Builder layer = layers.get(i);
            layer.name(readString(stream));
            if (header.version >= 4) {
                layer.locked(stream.readByte() == 1);
            }

            layer.volume(stream.readByte());
            if (header.version >= 2) {
                layer.panning(100 - stream.readUnsignedByte()); // 0 is 2 blocks right in nbs format, we want -100 to be left and 100 to be right
            }
        }
    }

    private static List<CustomInstrument> readCustomInstruments(@NotNull DataInputStream stream) throws IOException {
        List<CustomInstrument> customInstruments = new ArrayList<>();

        byte customInstrumentCount = stream.readByte();

        for (int index = 0; index < customInstrumentCount; index++) {
            customInstruments.add(CustomInstrument.builder()
                    .setName(readString(stream))
                    .setFileName(readString(stream))
                    .setKey(stream.readByte())
                    .setShouldPressKey(stream.readBoolean())
                    .build());
        }
        return customInstruments;
    }

    @NotNull
    private static List<Layer> buildLayers(@NotNull List<Layer.Builder> layers) {
        List<Layer> builtLayers = new ArrayList<>();
        for (Layer.Builder layerBuilder : layers) {
            builtLayers.add(layerBuilder.build());
        }
        return builtLayers;
    }

    private static void handleTempoChangerNotes(@NotNull Song.Builder song, @NotNull List<Layer> layers, int instrumentIndex) {
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            Layer processedLayer = handleTempoChangeNotesInLayer(song, layer, instrumentIndex);
            if (layer != processedLayer) {
                layers.set(i, processedLayer);
            }
        }
    }

    private static Layer handleTempoChangeNotesInLayer(@NotNull Song.Builder song, @NotNull Layer layer, int instrumentIndex) {
        Layer.Builder modifiedLayer = null;
        for (Map.Entry<Long, Note> noteEntry : layer.getNotes().entrySet()) {
            long tick = noteEntry.getKey();
            Note note = noteEntry.getValue();

            if (note.isCustomInstrument() && note.getInstrument() == instrumentIndex) {
                if (modifiedLayer == null) {
                    modifiedLayer = Layer.builder(layer);
                }
                modifiedLayer.note(tick, null);
                song.tempoChange(tick, Math.abs(note.getPitch()) / 15f);
            }
        }
        return modifiedLayer != null ? modifiedLayer.build() : layer;
    }

    private static void addCustomInstrumentsToSong(@NotNull Song.Builder song, @NotNull List<CustomInstrument> customInstruments) {
        for (CustomInstrument customInstrument : customInstruments) {
            song.customInstrument(customInstrument);
        }
    }

    private static void addLayersToSong(@NotNull Song.Builder song, @NotNull List<Layer> layers) {
        for (Layer layer : layers) {
            if (CustomInstrument.TEMPO_CHANGER_INSTRUMENT_NAME.equals(layer.getName()) && layer.getNotes().isEmpty()) {
                continue;
            }
            song.layer(layer);
        }
    }

    private static class HeaderData{
        private int version = 0;
        private int firstCustomInstrumentIndex = 10; //Backward compatibility - most of the songs with old structure are from 1.12
    }

}
