package cz.koca2000.nbs4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class NBSWriter {

    public static void writeSong(Song song, int nbsVersion, OutputStream stream){
        try {
            DataOutputStream outputStream = new DataOutputStream(stream);

            int instrumentsCount = roundInstrumentCountToMinecraftVanillaCount(song.getNonCustomInstrumentsCount());
            writeHeader(outputStream, song, nbsVersion, instrumentsCount);

            writeShort(outputStream, (short) song.getLayersCount()); // song height

            writeMetadata(outputStream, song, nbsVersion);

            writeNotes(outputStream, song, nbsVersion, instrumentsCount);

            writeLayers(outputStream, song, nbsVersion);

            writeCustomInstruments(outputStream, song);

            outputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void writeShort(DataOutputStream stream, short num) throws IOException {
        byte[] bytes = new byte[2];
        bytes[0] = (byte)(num & 0xff);
        bytes[1] = (byte)((num >> 8) & 0xff);
        stream.writeByte(bytes[0]);
        stream.writeByte(bytes[1]);
    }

    private static void writeInt(DataOutputStream stream, int num) throws IOException {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(num & 0xff);
        bytes[1] = (byte)((num >> 8) & 0xff);
        bytes[2] = (byte)((num >> 16) & 0xff);
        bytes[3] = (byte)((num >> 24) & 0xff);
        stream.write(bytes);
    }

    private static void writeString(DataOutputStream stream, String text) throws IOException {
        byte[] textBytes = text.getBytes();
        writeInt(stream, textBytes.length);
        for (byte b : textBytes) {
            stream.writeByte(b);
        }
    }

    private static void writeHeader(DataOutputStream stream, Song song, int nbsVersion, int firstCustomInstrumentIndex) throws IOException {
        writeShort(stream, (short) 0);

        stream.writeByte(nbsVersion);
        stream.writeByte(firstCustomInstrumentIndex);
        if (nbsVersion >= 3)
            stream.writeShort(song.getSongLength());
    }

    private static void writeMetadata(DataOutputStream stream, Song song, int nbsVersion) throws IOException {
        SongMetadata metadata = song.getMetadata();
        writeString(stream, metadata.getTitle());
        writeString(stream, metadata.getAuthor());
        writeString(stream, metadata.getOriginalAuthor());
        writeString(stream, metadata.getDescription());

        writeShort(stream, (short) Math.round(song.getTempo(0) * 100));
        stream.writeBoolean(metadata.isAutoSave());
        stream.writeByte(metadata.getAutoSaveDuration());
        stream.writeByte(metadata.getTimeSignature()); //x/4ths

        writeInt(stream, metadata.getMinutesSpent());
        writeInt(stream, metadata.getLeftClicks());
        writeInt(stream, metadata.getRightClicks());
        writeInt(stream, metadata.getNoteBlocksAdded());
        writeInt(stream, metadata.getNoteBlocksRemoved());

        writeString(stream, metadata.getOriginalMidiFileName());

        if (nbsVersion >= 4) {
            stream.writeBoolean(metadata.isLoop());
            stream.writeByte(metadata.getLoopMaxCount());
            stream.writeShort(metadata.getLoopStartTick());
        }
    }

    private static void writeNotes(DataOutputStream stream, Song song, int nbsVersion, int instrumentsCount) throws IOException {
        int lastTick = -1;
        int tick = song.getNextNonEmptyTick(lastTick);
        while (tick != -1) {
            writeShort(stream, (short)(tick - lastTick)); //jump ticks

            int lastLayerIndex = -1;
            for (int layerIndex = 0; layerIndex < song.getLayersCount(); layerIndex++) {
                Note note = song.getLayer(layerIndex).getNote(tick);
                if (note == null)
                    continue;

                writeShort(stream, (short)(layerIndex - lastLayerIndex)); //jump layers

                if (note.isCustomInstrument())
                    stream.writeByte(instrumentsCount + note.getInstrument());
                else
                    stream.writeByte(note.getInstrument());

                stream.writeByte(note.getKey());

                if (nbsVersion >= 4){
                    stream.writeByte(note.getVolume());
                    stream.writeByte(100 - note.getPanning()); // 0 is right in nbs format
                    writeShort(stream, (short) note.getPitch());
                }

                lastLayerIndex = layerIndex;
            }
            writeShort(stream, (short) 0); //end of layers for this tick

            lastTick = tick;
            tick = song.getNextNonEmptyTick(tick);
        }
        writeShort(stream, (short) 0); //end of ticks
    }

    private static void writeLayers(DataOutputStream stream, Song song, int nbsVersion) throws IOException {
        for (int i = 0; i < song.getLayersCount(); i++) {
            Layer layer = song.getLayer(i);

            writeString(stream, layer.getName());

            if (nbsVersion >= 4)
                stream.writeBoolean(layer.isLocked());

            stream.writeByte(layer.getVolume());

            if (nbsVersion >= 2)
                stream.writeByte(100 - layer.getPanning()); // 0 is right in nbs format
        }
    }

    private static void writeCustomInstruments(DataOutputStream stream, Song song) throws IOException {
        stream.writeByte(song.getCustomInstrumentsCount()); //custom instruments count

        for (int i = 0; i < song.getCustomInstrumentsCount(); i++) {
            CustomInstrument customInstrument = song.getCustomInstrument(i);
            writeString(stream, customInstrument.getName());
            writeString(stream, customInstrument.getFileName());
            stream.writeByte(customInstrument.getKey());
            stream.writeBoolean(customInstrument.shouldPressKey());
        }
    }

    /**
     * If necessary increases the number of instruments to the closest number available in some of Minecraft version.
     * This may be necessary because of compatibility with other parsers.
     * @return Increased number of instruments or original number in case it is bigger than know Minecraft instrument counts.
     */
    private static int roundInstrumentCountToMinecraftVanillaCount(int count){
        //Minecraft 1.2+
        if (count <= 5)
            return 5;

        // Minecraft 1.12+
        if (count <= 10)
            return 10;

        // Minecraft 1.14+ or unknown+
        return Math.max(16, count);
    }
}
