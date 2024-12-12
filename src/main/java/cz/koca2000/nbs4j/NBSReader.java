package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class NBSReader {

    private NBSReader() {
    }

    @NotNull
    public static Song readSong(@NotNull InputStream stream) {
        Song.Builder song = Song.builder();

        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(stream));

            HeaderData header = readHeader(song, dataInputStream);

            List<Layer.Builder> layers = initializeLayerBuilders(dataInputStream);
            int layersCount = layers.size();

            readMetadata(song, header, dataInputStream);

            readNotes(header, layers, dataInputStream);

            readLayers(header, layers, layersCount, dataInputStream);

            readCustomInstruments(song, dataInputStream);

            buildLayers(song, layers);
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
        song.tempoChange(0,readShort(stream) / 100f);
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

    private static void readCustomInstruments(@NotNull Song.Builder song, @NotNull DataInputStream stream) throws IOException {
        byte customInstrumentCount = stream.readByte();

        for (int index = 0; index < customInstrumentCount; index++) {
            song.customInstrument(CustomInstrument.builder()
                    .setName(readString(stream))
                    .setFileName(readString(stream))
                    .setKey(stream.readByte())
                    .setShouldPressKey(stream.readBoolean())
                    .build());
        }
    }

    private static void buildLayers(@NotNull Song.Builder song, @NotNull List<Layer.Builder> layers) {
        for (Layer.Builder layerBuilder : layers) {
            song.layer(layerBuilder.build());
        }
    }

    private static class HeaderData{
        private int version = 0;
        private int firstCustomInstrumentIndex = 10; //Backward compatibility - most of the songs with old structure are from 1.12
    }

}
