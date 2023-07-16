package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public final class Song {

    private final List<Layer> layers;

    private final SongMetadata metadata;
    private final boolean isStereo;
    private final long songLength;
    private final double songLengthInSeconds;

    private final int nonCustomInstrumentsCount;
    private final List<CustomInstrument> customInstruments;

    private final TreeSet<Long> nonEmptyTicks;

    // <Tick, Tempo>
    private final TreeMap<Long, Float> tempoChanges;
    private final long lastTick;

    private Song(@NotNull Builder builder, List<Layer> layers){
        this.layers = layers;
        metadata = builder.metadata;

        isStereo = builder.isStereo;
        songLength = builder.songLength;
        nonCustomInstrumentsCount = builder.nonCustomInstrumentsCount;
        customInstruments = Collections.unmodifiableList(builder.customInstruments);
        nonEmptyTicks = builder.nonEmptyTicks;
        tempoChanges = builder.tempoChanges;
        lastTick = builder.lastTick;

        songLengthInSeconds = songLength != 0 ? calculateTimeInSecondsAtTick(songLength) : 0;
    }

    /**
     * Provides {@link SongMetadata} of the Song
     * @return {@link SongMetadata}
     */
    @NotNull
    public SongMetadata getMetadata(){
        return metadata;
    }

    /**
     * Provides {@link Layer} with the specified index.
     * @param index index of the layer to return
     * @throws IndexOutOfBoundsException if the index is out of range
     * @return {@link Layer} with the specified index
     */
    @NotNull
    public Layer getLayer(int index){
        return layers.get(index);
    }

    /**
     * Returns count of layers
     * @return count of layers
     */
    public int getLayersCount(){
        return layers.size();
    }

    /**
     * Returns whether the song was marked as containing notes or layers with panning.
     * @return true if marked as stereo; otherwise, false
     */
    public boolean isStereo() {
        return isStereo;
    }

    /**
     * Returns number of ticks of the song
     * @return song length in ticks
     */
    public long getSongLength() {
        return songLength;
    }

    /**
     * Returns the length of the song in seconds with all tempo changes applied.
     * @return length in seconds
     */
    public double getSongLengthInSeconds(){
        return songLengthInSeconds;
    }

    /**
     * Returns the time in seconds at the given tick with all tempo changes applied.
     * @param tick Tick in question
     * @return time in seconds
     */
    public double getTimeInSecondsAtTick(long tick){
        if (tick <= 0 || songLength == 0)
            return 0;

        if (tick >= songLength) // tick is zero-based
            return getSongLengthInSeconds();

        return calculateTimeInSecondsAtTick(tick);
    }

    private double calculateTimeInSecondsAtTick(long tick){
        double length = 0;
        long lastTick = 0;
        float lastTempo = getTempo(0);
        for (Map.Entry<Long, Float> tempo : tempoChanges.entrySet()){
            long changeTick = tempo.getKey();
            if (changeTick <= 0)
                continue;
            if (changeTick > tick)
                changeTick = tick;

            length += (changeTick - lastTick) * (1f / lastTempo);
            lastTempo = tempo.getValue();
            lastTick = changeTick;

            if (changeTick == tick)
                break;
        }
        length += (tick - lastTick) * (1f / lastTempo);
        return length;
    }

    /**
     * Returns the tick greater than the given tick that contains note or tempo change.
     * @param fromTick tick after which the next tick should be searched
     * @return tick number if there is any note or tempo change left; otherwise, -1
     */
    public long getNextNonEmptyTick(long fromTick){
        Long tick = nonEmptyTicks.higher(fromTick);
        if (tick == null)
            return -1;

        return tick;
    }

    /**
     * Returns song tempo in ticks per second on the given tick
     * @param tick tick for which the tempo is requested
     * @return tempo in ticks per second
     */
    public float getTempo(long tick){
        if (tempoChanges.size() == 0)
            return 10;
        Map.Entry<Long, Float> floorEntry = tempoChanges.floorEntry(tick);
        return floorEntry != null ? floorEntry.getValue() : 10;
    }

    /**
     * Returns number of non-custom instruments that the song may use
     * (e.g. if single instrument with index 5 is used, this value returns 6).
     * @return Number of instruments that has to be available to be able to play all notes of this song.
     */
    public int getNonCustomInstrumentsCount() {
        return nonCustomInstrumentsCount;
    }

    /**
     * Returns {@link CustomInstrument} with the specified index.
     * @param index index of the custom instrument to be returned
     * @throws IndexOutOfBoundsException if the index is not in range
     * @return {@link CustomInstrument} with specified index
     */
    @NotNull
    public CustomInstrument getCustomInstrument(int index){
        return customInstruments.get(index);
    }

    /**
     * Returns the count of custom instruments of this song.
     * @return count of custom instruments
     */
    public int getCustomInstrumentsCount(){
        return customInstruments.size();
    }

    /**
     * Returns unmodifiable {@link List} of song's layers.
     * @return unmodifiable {@link List} of layers
     */
    @NotNull
    public List<Layer> getLayers(){
        return layers;
    }

    /**
     * Returns unmodifiable {@link List} of song's custom instruments.
     * @return unmodifiable {@link List} of custom instruments
     */
    @NotNull
    public List<CustomInstrument> getCustomInstruments(){
        return customInstruments;
    }

    /**
     * Saves this song to the given stream using the given nbs version.
     * @param nbsVersion version of nbs data format
     * @param stream output stream the song will be written to
     */
    public void save(@NotNull NBSVersion nbsVersion, @NotNull OutputStream stream){
        NBSWriter.writeSong(this, nbsVersion.getVersionNumber(), stream);
    }

    /**
     * Saves this song to the given file using the given nbs version.
     * @param nbsVersion version of nbs data format
     * @param file file the song will be written to
     * @throws IOException if the file can not be written
     */
    public void save(@NotNull NBSVersion nbsVersion, @NotNull File file) throws IOException{
        NBSWriter.writeSong(this, nbsVersion.getVersionNumber(), Files.newOutputStream(file.toPath()));
    }

    /**
     * Loads song from given file
     * @param file file to be loaded
     * @return loaded instance of {@link Song}
     * @throws IOException if file does not exist or can not be opened
     * @throws SongCorruptedException if an error occurred during the loading
     */
    @NotNull
    public static Song fromFile(@NotNull File file) throws IOException {
        Song song = NBSReader.readSong(Files.newInputStream(file.toPath()));
        song.getMetadata().setSourceFile(file);
        return song;
    }

    /**
     * Loads song from the given stream
     * @param stream stream from which the song will be loaded
     * @return loaded instance of {@link Song}
     * @throws SongCorruptedException if an error occurred during the loading
     */
    @NotNull
    public static Song fromStream(@NotNull InputStream stream) {
        return NBSReader.readSong(stream);
    }

    public static class Builder {
        private final List<Layer.Builder> layerBuilders = new ArrayList<>();
        private final SongMetadata metadata;

        private boolean isStereo = false;
        private long songLength = 0;

        private int nonCustomInstrumentsCount = 0;
        private final List<CustomInstrument> customInstruments = new ArrayList<>();

        private final TreeSet<Long> nonEmptyTicks = new TreeSet<>();

        // <Tick, Tempo>
        private final TreeMap<Long, Float> tempoChanges = new TreeMap<>();
        private long lastTick = -1;

        public Builder() {
            metadata = new SongMetadata();
        }

        /**
         * Initialize the builder with data from the given song.
         * @param originalSong song to be copied
         */
        public Builder(@NotNull Song originalSong) {
            metadata = new SongMetadata(originalSong.metadata);

            isStereo = originalSong.isStereo;
            lastTick = originalSong.lastTick;
            songLength = originalSong.songLength;
            nonCustomInstrumentsCount = originalSong.nonCustomInstrumentsCount;

            for (CustomInstrument instrument : originalSong.customInstruments){
                addCustomInstrument(instrument);
            }

            for (int i = 0; i < originalSong.getLayersCount(); i++){
                addLayerCopy(originalSong.getLayer(i), l -> {});
            }

            for (Map.Entry<Long, Float> entry : originalSong.tempoChanges.entrySet()){
                setTempoChange(entry.getKey(), entry.getValue());
            }
        }

        /**
         * Adds the custom instrument to the end of the list with custom instruments of this song.
         * @param customInstrument {@link CustomInstrument} to be added.
         * @return this instance of the {@link Builder}
         */
        @NotNull
        public Song.Builder addCustomInstrument(@NotNull CustomInstrument customInstrument){
            customInstruments.add(customInstrument);
            return this;
        }

        /**
         * Adds a new layer to the song.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder addLayer(Consumer<Layer.Builder> layerBuilderCall){
            Layer.Builder layerBuilder = new Layer.Builder();
            layerBuilders.add(layerBuilder);

            layerBuilderCall.accept(layerBuilder);
            return this;
        }

        /**
         * Makes a copy of the given layer and adds it to the song.
         * @param layer Layer to be added
         * @param layerBuilderCall Consumer with {@link Layer.Builder} of the added {@link Layer}.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder addLayerCopy(@NotNull Layer layer, @NotNull Consumer<Layer.Builder> layerBuilderCall){
            Layer.Builder layerBuilder = new Layer.Builder(layer);
            layerBuilders.add(layerBuilder);

            for (Map.Entry<Long, Note.Builder> noteEntry : layerBuilder.getNotes().entrySet()){
                onNoteAdded(noteEntry.getKey(), noteEntry.getValue());
            }

            layerBuilderCall.accept(layerBuilder);
            return this;
        }

        /**
         * Returns the {@link Layer.Builder} of the layer at the given index
         * @param index Index of the layer
         * @param layerBuilderCall Consumer with {@link Layer.Builder} of the specified Layer
         * @return this instance of {@link Builder}
         * @throws IndexOutOfBoundsException if the index is out of bounds
         */
        @NotNull
        public Song.Builder getLayer(int index, @NotNull Consumer<Layer.Builder> layerBuilderCall) {
            if (index < 0 || index > layerBuilders.size()) {
                throw new IndexOutOfBoundsException("The index is out of bounds.");
            }

            layerBuilderCall.accept(layerBuilders.get(index));
            return this;
        }

        /**
         * Returns the {@link Layer.Builder} of the layer at the given index
         * @param index Index of the layer
         * @return {@link Layer.Builder} of the specified Layer
         */
        @NotNull
        public Layer.Builder getLayer(int index) {
            if (index < 0 || index > layerBuilders.size()) {
                throw new IndexOutOfBoundsException("The index is out of bounds.");
            }

            return layerBuilders.get(index);
        }

        /**
         * Creates as many {@link Layer} as needed to have specified count of layers.
         * @param count amount of layers the song should contain
         * @throws IllegalArgumentException if specified count is lower than number of existing layers.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder setLayersCount(int count){
            if (count < layerBuilders.size())
                throw new IllegalArgumentException("Layers can not be removed.");

            for (int i = layerBuilders.size(); i < count; i++){
                int index = i;
                addLayer(l -> l.setName("Layer #" + index));
            }

            return this;
        }

        /**
         * Adds the note to the song on specified tick and layer.
         * If the tick is higher than song's length, song is prolonged.
         * @param tick tick of the note
         * @param layerIndex index of layer on which the song should be placed
         * @param noteBuilderCall {@link Note.Builder} of the added note
         * @throws IllegalArgumentException of tick is negative.
         * @throws IndexOutOfBoundsException if layer index is negative.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder addNoteToLayerAtTick(int layerIndex, long tick, @NotNull Consumer<Note.Builder> noteBuilderCall){
            if (tick < 0)
                throw new IllegalArgumentException("Tick can not be negative.");

            Note.Builder noteBuilder = new Note.Builder();
            noteBuilderCall.accept(noteBuilder);

            addNoteToLayer(layerIndex, tick, noteBuilder);

            return this;
        }

        /**
         * Adds the copy of the note to the song on specified tick and layer.
         * If the tick is higher than song's length, song is prolonged.
         * @param tick tick of the note
         * @param layerIndex index of layer on which the song should be placed
         * @param note {@link Note} to be copied
         * @param noteBuilderCall {@link Note.Builder} of the added note
         * @throws IllegalArgumentException of tick is negative.
         * @throws IndexOutOfBoundsException if layer index is negative.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder addNoteCopyToLayerAtTick(int layerIndex, long tick, @NotNull Note note, @NotNull Consumer<Note.Builder> noteBuilderCall){
            if (tick < 0)
                throw new IllegalArgumentException("Tick can not be negative.");

            Note.Builder noteBuilder = new Note.Builder(note);
            noteBuilderCall.accept(noteBuilder);

            addNoteToLayer(layerIndex, tick, noteBuilder);

            return this;
        }

        private void addNoteToLayer(int layerIndex, long tick, @NotNull Note.Builder noteBuilder) {
            if (layerIndex >= layerBuilders.size())
                setLayersCount(layerIndex + 1);

            Layer.Builder layer = layerBuilders.get(layerIndex);

            layer.setNoteInternal(tick, noteBuilder);
            onNoteAdded(tick, noteBuilder);
        }

        private void onNoteAdded(long tick, @NotNull Note.Builder note){
            if (!note.isCustomInstrument())
                increaseNonCustomInstrumentsCountTo(note.getInstrument() + 1);

            nonEmptyTicks.add(tick);
            if (lastTick < tick)
                lastTick = tick;
            if (songLength <= tick)
                songLength = tick + 1;

            if (note.getPanning() != Note.NEUTRAL_PANNING)
                isStereo = true;
        }

        /**
         * Removes note on specific tick and layer.
         * @param tick tick on which is the note to be removed
         * @param layerIndex index of layer of the note
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder removeNote(long tick, int layerIndex){
            Layer.Builder layer = layerBuilders.get(layerIndex);
            layer.removeNoteInternal(tick);

            boolean otherNoteExists = false;
            for (int i = 0; i < layerBuilders.size(); i++){
                if (layer.getNote(tick) != null) {
                    otherNoteExists = true;
                    break;
                }
            }
            otherNoteExists |= tempoChanges.containsKey(tick);

            if (!otherNoteExists) {
                nonEmptyTicks.remove(tick);
                if (tick == lastTick){
                    lastTick = nonEmptyTicks.isEmpty() ? -1 : nonEmptyTicks.last();
                    if (tick == songLength - 1)
                        songLength = lastTick + 1;
                }
            }

            return this;
        }

        /**
         * Specifies the length of the song.
         * @param length new length of the song
         * @throws IllegalArgumentException if song with specified length would not contain all notes or tempo changes.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder setLength(long length){
            if (lastTick >= length) {
                throw new IllegalArgumentException("Specified song length would not contain all notes or tempo changes.");
            }
            songLength = length;

            return this;
        }

        /**
         * Specifies whether the song has notes or layers (notes or layers with panning).
         * @return this instance of {@link Builder}
         */
        @NotNull
        Song.Builder setStereo(){
            isStereo = true;
            return this;
        }

        @NotNull
        Song.Builder increaseNonCustomInstrumentsCountTo(int count) {
            if (nonCustomInstrumentsCount < count)
                nonCustomInstrumentsCount = count;
            return this;
        }

        /**
         * Specifies the change of tempo in ticks per seconds on specific tick.
         * @param firstTick tick since the specified tempo is used.
         * @param tempo tempo in ticks per seconds to be used from this tick on
         * @throws IllegalArgumentException if the tempo is not positive value
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Song.Builder setTempoChange(long firstTick, float tempo){
            if (tempo <= 0)
                throw new IllegalArgumentException("Tempo has to be positive value.");

            if (firstTick >= 0)
                nonEmptyTicks.add(firstTick);

            tempoChanges.put(firstTick, tempo);

            return this;
        }

        /**
         * Provides {@link SongMetadata} of the Song
         * @return {@link SongMetadata}
         */
        @NotNull
        public SongMetadata getMetadata(){
            return metadata;
        }

        /**
         * Returns count of layers
         * @return count of layers
         */
        public int getLayersCount(){
            return layerBuilders.size();
        }

        /**
         * Creates new instance of {@link Song} based on data from this builder.
         * @return {@link Song}
         */
        public Song build() {
            List<Layer> layers = new ArrayList<>();
            for (Layer.Builder layerBuilder : layerBuilders) {
                Layer layer = layerBuilder.build();
                layers.add(layer);

                if (layer.getPanning() != Layer.NEUTRAL_PANNING) {
                    setStereo();
                }

                if (!isStereo) {
                    for (Note note : layer.getNotes().values()) {
                        if (note.getPanning() != Note.NEUTRAL_PANNING) {
                            setStereo();
                            break;
                        }
                    }
                }
            }

            Song song = new Song(this, layers);

            for (Layer layer : layers) {
                layer.setSong(song);
            }

            return song;
        }
    }
}
