package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class NBSReader {

    private NBSReader(){}

    @NotNull
    public static Song readSong(@NotNull InputStream stream) {
        Song.Builder song = new Song.Builder();

        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(stream));

            HeaderData header = readHeader(song, dataInputStream);

            song.setLayersCount(readShort(dataInputStream));

            readMetadata(song, header, dataInputStream);

            readNotes(song, header, dataInputStream);

            readLayers(song, header, dataInputStream);

            readCustomInstruments(song, dataInputStream);
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
            data.Version = stream.readByte();
            data.FirstCustomInstrumentIndex = stream.readByte();

            if (data.Version >= 3) // Until nbs 3 there wasn't length specified in the file
                song.setLength(readShort(stream));
        }
        else
            song.setLength(length);

        return data;
    }

    private static void readMetadata(@NotNull Song.Builder song, @NotNull HeaderData header, @NotNull DataInputStream stream) throws IOException {
        SongMetadata metadata = song.getMetadata();

        metadata.setTitle(readString(stream))
                .setAuthor(readString(stream))
                .setOriginalAuthor(readString(stream))
                .setDescription(readString(stream));
        song.setTempoChange(-1,readShort(stream) / 100f);
        metadata.setAutoSave(stream.readBoolean())
                .setAutoSaveDuration(stream.readByte())
                .setTimeSignature(stream.readByte())
                .setMinutesSpent(readInt(stream))
                .setLeftClicks(readInt(stream))
                .setRightClicks(readInt(stream))
                .setNoteBlocksAdded(readInt(stream))
                .setNoteBlocksRemoved(readInt(stream))
                .setOriginalMidiFileName(readString(stream));
        if (header.Version >= 4) {
            metadata.setLoop(stream.readBoolean())
                    .setLoopMaxCount(stream.readByte())
                    .setLoopStartTick(readShort(stream));
        }
    }

    private static void readNotes(@NotNull Song.Builder song, @NotNull HeaderData header, @NotNull DataInputStream stream) throws IOException {
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

                byte instrument = stream.readByte();

                int instrumentId;
                boolean isCustomInstrument;
                if (instrument >= header.FirstCustomInstrumentIndex){
                    instrumentId = instrument - header.FirstCustomInstrumentIndex;
                    isCustomInstrument = true;
                } else {
                    instrumentId = instrument;
                    isCustomInstrument = false;
                }

                byte key = stream.readByte();
                byte volume;
                int panning;
                short pitch;
                if (header.Version >= 4) {
                    volume = stream.readByte();
                    panning = 100 - stream.readUnsignedByte(); // 0 is 2 blocks right in nbs format, we want -100 to be left and 100 to be right
                    pitch = readShort(stream);
                } else {
                    volume = 100;
                    panning = 0;
                    pitch = 0;
                }

                song.addNoteToLayerAtTick(layer, tick,
                        note -> note.setInstrument(instrumentId, isCustomInstrument)
                                .setKey(key)
                                .setVolume(volume)
                                .setPanning(panning)
                                .setPitch(pitch));
            }
        }
    }

    private static void readLayers(@NotNull Song.Builder song, @NotNull HeaderData header, @NotNull DataInputStream stream) throws IOException {
        for (int i = 0; i < song.getLayersCount(); i++) {
            Layer.Builder layer = song.getLayer(i);

            layer.setName(readString(stream));
            if (header.Version >= 4){
                layer.setLocked(stream.readByte() == 1);
            }

            layer.setVolume(stream.readByte());
            if (header.Version >= 2){
                layer.setPanning(100 - stream.readUnsignedByte()); // 0 is 2 blocks right in nbs format, we want -100 to be left and 100 to be right
            }
        }
    }

    private static void readCustomInstruments(@NotNull Song.Builder song, @NotNull DataInputStream stream) throws IOException {
        byte customInstrumentCount = stream.readByte();

        for (int index = 0; index < customInstrumentCount; index++) {
            song.addCustomInstrument(new CustomInstrument.Builder()
                    .setName(readString(stream))
                    .setFileName(readString(stream))
                    .setKey(stream.readByte())
                    .setShouldPressKey(stream.readBoolean())
                    .build());
        }
    }

    private static class HeaderData{
        public int Version = 0;
        public int FirstCustomInstrumentIndex = 10; //Backward compatibility - most of the songs with old structure are from 1.12
    }

}
