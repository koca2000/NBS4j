package cz.koca2000.nbs4j;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class NBSReader {

    private NBSReader(){}

    public static Song readSong(InputStream stream) {
        Song song = new Song();

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
        return song;
    }

    private static short readShort(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        return (short) (byte1 + (byte2 << 8));
    }

    private static int readInt(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        int byte3 = dataInputStream.readUnsignedByte();
        int byte4 = dataInputStream.readUnsignedByte();
        return (byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24));
    }

    private static String readString(DataInputStream dataInputStream) throws IOException {
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

    private static HeaderData readHeader(Song song, DataInputStream stream) throws IOException {
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

    private static void readMetadata(Song song, HeaderData header, DataInputStream stream) throws IOException {
        SongMetadata metadata = song.getMetadata();

        metadata.setTitle(readString(stream))
                .setAuthor(readString(stream))
                .setOriginalAuthor(readString(stream))
                .setDescription(readString(stream));
        song.setTempoChange(0,readShort(stream) / 100f);
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
            metadata.setLoop(stream.readByte() == 1)
                    .setLoopMaxCount(stream.readByte())
                    .setLoopStartTick(readShort(stream));
        }
    }

    private static void readNotes(Song song, HeaderData header, DataInputStream stream) throws IOException {
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

                Note note = new Note();
                byte instrument = stream.readByte();
                if (instrument >= header.FirstCustomInstrumentIndex)
                    note.setInstrument(instrument - header.FirstCustomInstrumentIndex, true);
                else
                    note.setInstrument(instrument);

                note.setKey(stream.readByte());
                if (header.Version >= 4) {
                    note.setVolume(stream.readByte())
                            .setPanning(200 - stream.readUnsignedByte()) // 0 is right in nbs format
                            .setPitch(readShort(stream));
                }

                song.setNote(tick, layer, note);
            }
        }
    }

    private static void readLayers(Song song, HeaderData header, DataInputStream stream) throws IOException {
        for (int i = 0; i < song.getLayersCount(); i++) {
            Layer layer = song.getLayer(i);

            layer.setName(readString(stream));
            if (header.Version >= 4){
                layer.setLocked(stream.readByte() == 1);
            }

            layer.setVolume(stream.readByte());
            if (header.Version >= 2){
                layer.setPanning(200 - stream.readUnsignedByte()); // 0 is right in nbs format
            }
        }
    }

    private static void readCustomInstruments(Song song, DataInputStream stream) throws IOException {
        byte customInstrumentCount = stream.readByte();

        for (int index = 0; index < customInstrumentCount; index++) {
            song.addCustomInstrument(new CustomInstrument()
                    .setName(readString(stream))
                    .setFileName(readString(stream))
                    .setPitch(stream.readByte()));
            stream.readByte();
        }
    }

    private static class HeaderData{
        public int Version = 0;
        public int FirstCustomInstrumentIndex = 10; //Backward compatibility - most of the songs with old structure are from 1.12
    }

}
