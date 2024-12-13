package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Song {
    public static final float DEFAULT_TEMPO = 10;
    public static final long INITIAL_TEMPO_TICK = -1;

    private final @UnmodifiableView List<LayerInSong> layers;

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

    private Song(@NotNull Builder builder){
        metadata = builder.metadata;

        isStereo = builder.isStereo;
        songLength = builder.songLength;
        nonCustomInstrumentsCount = builder.nonCustomInstrumentsCount;
        customInstruments = Collections.unmodifiableList(new ArrayList<>(builder.customInstruments));
        nonEmptyTicks = new TreeSet<>(builder.nonEmptyTicks);
        tempoChanges = new TreeMap<>(builder.tempoChanges);
        lastTick = builder.lastTick;

        List<LayerInSong> layersInSong = new ArrayList<>();
        for (Layer layer : builder.layers) {
            layersInSong.add(new LayerInSong(this, layer));
        }
        this.layers = Collections.unmodifiableList(layersInSong);

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
     * Provides {@link LayerInSong} with the specified index.
     * @param index index of the layer to return
     * @throws IndexOutOfBoundsException if the index is out of range
     * @return {@link LayerInSong} with the specified index
     */
    @NotNull
    public LayerInSong getLayer(int index){
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
        long previousTick = 0;
        float lastTempo = getTempo(0);
        for (Map.Entry<Long, Float> tempo : tempoChanges.entrySet()) {
            long changeTick = tempo.getKey();
            if (changeTick < 0) {
                changeTick = 0;
            }
            if (changeTick > tick) {
                changeTick = tick;
            }

            length += (changeTick - previousTick) * (1f / lastTempo);
            lastTempo = tempo.getValue();
            previousTick = changeTick;

            if (changeTick == tick)
                break;
        }
        length += (tick - previousTick) * (1f / lastTempo);
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
        if (tick < INITIAL_TEMPO_TICK) {
            tick = INITIAL_TEMPO_TICK;
        }
        if (tempoChanges.isEmpty()) {
            return DEFAULT_TEMPO;
        }
        Map.Entry<Long, Float> floorEntry = tempoChanges.floorEntry(tick);
        return floorEntry != null ? floorEntry.getValue() : DEFAULT_TEMPO;
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
    public List<LayerInSong> getLayers(){
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

    @NotNull
    @UnmodifiableView
    public Map<Long, Float> getTempoChanges() {
        return Collections.unmodifiableMap(tempoChanges);
    }

    /**
     * Saves this song to the given stream using the given nbs version.
     * @param nbsVersion version of nbs data format
     * @param stream output stream the song will be written to
     * @throws UncheckedIOException if any IO operation fails
     */
    public void save(@NotNull NBSVersion nbsVersion, @NotNull OutputStream stream){
        try {
            NBSWriter.writeSong(this, nbsVersion.getVersionNumber(), stream);
        }
        catch (IOException ex) {
            throw new UncheckedIOException("There was an error during the saving operation", ex);
        }
    }

    /**
     * Saves this song to the given file using the given nbs version.
     * @param nbsVersion version of nbs data format
     * @param file file the song will be written to
     * @throws UncheckedIOException if the file can not be written or any IO operation fails
     */
    public void save(@NotNull NBSVersion nbsVersion, @NotNull File file) {
        try {
            NBSWriter.writeSong(this, nbsVersion.getVersionNumber(), Files.newOutputStream(file.toPath()));
        }
        catch (IOException ex) {
            throw new UncheckedIOException("There was an error during the saving operation", ex);
        }
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

    @NotNull
    public static Builder builder() {
        return builder(false);
    }

    @NotNull
    public static Builder builder(boolean isStrict) {
        return new Builder(isStrict);
    }

    @NotNull
    public static Builder builder(@NotNull Song song) {
        return builder(song, false);
    }

    @NotNull
    public static Builder builder(@NotNull Song song, boolean isStrict) {
        return new Builder(song, isStrict);
    }

    @NotNull
    public static Builder builder(@NotNull Song song, @NotNull SongMetadata songMetadata) {
        return builder(song, songMetadata, false);
    }

    @NotNull
    public static Builder builder(@NotNull Song song, @NotNull SongMetadata songMetadata, boolean isStrict) {
        return new Builder(song, songMetadata, isStrict);
    }

    public static final class Builder {
        private final boolean isStrict;
        private final InteractiveReplacer replacer = new InteractiveReplacer();

        private final List<Layer> layers = new ArrayList<>();
        private SongMetadata metadata;

        private boolean isStereo = false;
        private long songLength = 0;

        private int nonCustomInstrumentsCount = 0;
        private final List<CustomInstrument> customInstruments = new ArrayList<>();

        private final TreeSet<Long> nonEmptyTicks = new TreeSet<>();

        // <Tick, Tempo>
        private final TreeMap<Long, Float> tempoChanges = new TreeMap<>();
        private long lastTick = -1;

        private Builder(boolean isStrict) {
            this.isStrict = isStrict;
        }

        /**
         * Initialize the builder with data from the given song.
         * If your {@link Song} uses a custom metadata class derived from {@link SongMetadata}, use {@link #Builder(Song, SongMetadata, boolean)}.
         * @param originalSong song to be copied
         * @throws IllegalArgumentException if the song's metadata are derived class instead of {@link SongMetadata}
         */
        private Builder(@NotNull Song originalSong, boolean isStrict) {
            this(originalSong, cloneMetadata(originalSong), isStrict);
        }

        private Builder(@NotNull Song originalSong, @NotNull SongMetadata songMetadata, boolean isStrict) {
            this(isStrict);
            if (originalSong.metadata == songMetadata) {
                throw new IllegalArgumentException("Supplied SongMetadata are the same instance of SongMetadata as in the original song.");
            }
            metadata = songMetadata;

            isStereo = originalSong.isStereo;
            lastTick = originalSong.lastTick;
            songLength = originalSong.songLength;
            nonCustomInstrumentsCount = originalSong.nonCustomInstrumentsCount;

            for (CustomInstrument instrument : originalSong.customInstruments){
                customInstrument(instrument);
            }

            for (LayerInSong layer : originalSong.getLayers()){
                layer(layer.getLayerData());
            }

            for (Map.Entry<Long, Float> entry : originalSong.tempoChanges.entrySet()){
                tempoChange(entry.getKey(), entry.getValue());
            }
        }

        @NotNull
        private static SongMetadata cloneMetadata(@NotNull Song originalSong) {
            if (originalSong.metadata.getClass() != SongMetadata.class) {
                throw new IllegalArgumentException("Song uses a custom SongMetadata class that cannot be cloned automatically.");
            }
            return new SongMetadata(originalSong.metadata);
        }

        /**
         * Enables interactive way of replacing certain parts of the song for a single call.
         * @return {@link InteractiveReplacer}
         */
        public InteractiveReplacer replace() {
            return replacer;
        }

        /**
         * Adds the custom instrument to the end of the list with custom instruments of this song.
         * @param customInstrument {@link CustomInstrument} to be added.
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the layer is null
         * @return this instance of the {@link Builder}
         */
        @NotNull
        public Builder customInstrument(CustomInstrument customInstrument){
            if (customInstrument == null) {
                if (isStrict) {
                    throw new IllegalArgumentException("Can not add null as a custom instrument");
                }
                return this;
            }
            customInstruments.add(customInstrument);
            return this;
        }

        /**
         * Replaces the custom instrument at the given index or removes it if the given value is null.
         * If the index equals the count of custom instruments added to the builder, it is the same as calling {@link #customInstrument(CustomInstrument)}.
         * If you remove a custom instrument, indices of all subsequent custom instruments change which may cause issues with already added notes.
         * @param index Zero-based index of the layer to be replaced
         * @param customInstrument {@link CustomInstrument} to be set to the given index or null
         * @throws IndexOutOfBoundsException if the {@link Builder} is not in strict mode and the index is greater than the count of custom instrument
         *                                  or if the {@link Builder} is in strict mode and the index is out of bounds
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder customInstrument(int index, @Nullable CustomInstrument customInstrument) {
            return customInstrument(index, customInstrument, ItemChangeMode.SET);
        }

        /**
         * Replaces the custom instrument at the given index or removes it if the given value is null.
         * If the index equals the count of custom instruments added to the builder, it is the same as calling {@link #customInstrument(CustomInstrument)}.
         * If you remove a custom instrument, indices of all subsequent custom instruments change which may cause issues with already added notes.
         * @param index Zero-based index of the layer to be replaced
         * @param customInstrument {@link CustomInstrument} to be set to the given index or null
         * @param changeMode Type of requested change
         * @throws IndexOutOfBoundsException if the {@link Builder} is not in strict mode and the index is greater than the count of custom instrument
         *                                  or if the {@link Builder} is in strict mode and the index is out of bounds
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the custom instrument is null and the change mode is not {@link ItemChangeMode#SET}
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder customInstrument(int index, @Nullable CustomInstrument customInstrument, ItemChangeMode changeMode) {
            genericItemChange(index, customInstrument, changeMode, customInstruments,
                    () -> customInstrument(CustomInstrument.builder()
                            .setName("block.note_block.harp")
                            .setFileName("block.note_block.harp")
                            .build()),
                    c -> {},
                    () -> {});
            return this;
        }

        /**
         * Adds the given layer to the song.
         * @param layer {@link Layer} to be added to the end of the list
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the layer is null
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder layer(Layer layer){
            if (layer == null) {
                if (isStrict) {
                    throw new IllegalArgumentException("Can not add null as a layer");
                }
                return this;
            }
            layers.add(layer);
            updateSongStatisticData(layer);
            return this;
        }

        /**
         * Replaces the layer at the given index or removes it if the given value is null.
         * If the index equals the count of layers, it is the same as calling {@link #layer(Layer)}.
         * If you remove a layer, indices of all subsequent layers change.
         * @param index Zero-based index of the layer to be replaced
         * @param layer {@link Layer} to be set to the given index or null
         * @throws IndexOutOfBoundsException if the {@link Builder} is in strict mode and the index is out of bounds
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder layer(int index, @Nullable Layer layer) {
            return layer(index, layer, ItemChangeMode.SET);
        }

        /**
         * Sets or inserts the layer at the given index. Operation is decided by the given {@link ItemChangeMode}.
         * If the index equals the count of layers, it is the same as calling {@link #layer(Layer)}.
         * If you remove a layer, indices of all subsequent layers change.
         * @param index Zero-based index of the layer to be replaced or to which new layer should be inserted
         * @param layer {@link Layer} to be set to the given index or null
         * @param changeMode Type of requested change
         * @throws IndexOutOfBoundsException if the {@link Builder} is in strict mode and the index is out of bounds
         * @throws IllegalArgumentException if the {@link Builder} is in strict mode and the layer is null and the change mode is not {@link ItemChangeMode#SET}
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder layer(int index, @Nullable Layer layer, ItemChangeMode changeMode) {
            genericItemChange(index, layer, changeMode, layers,
                    () -> layer(Layer.builder().name("Empty layer").build()),
                    this::updateSongStatisticData,
                    this::recalculateSongStatisticData);
            return this;
        }

        private <T> void genericItemChange(int index, T item, ItemChangeMode changeMode, List<T> list, Runnable dummyItemCreator, Consumer<T> onAdd, Runnable onChange) {
            if (index < 0) {
                if (isStrict) {
                    throw new IndexOutOfBoundsException("Index can not be lower than 0");
                }
                index = 0;
            }

            if (item == null) {
                genericItemRemove(index, changeMode, list, onChange);
                return;
            }

            if (index > list.size()) {
                if (isStrict) {
                    throw new IndexOutOfBoundsException();
                }

                while (index > list.size()) {
                    dummyItemCreator.run();
                }
            }

            switch (changeMode) {
                case SET:
                    list.set(index, item);
                    onChange.run();
                    break;
                case INSERT:
                    list.add(index, item);
                    onAdd.accept(item);
                    break;
                default:
                    throw new IncompatibleClassChangeError("Unknown ItemChangeMode");
            }
        }

        private <T> void genericItemRemove(int index, ItemChangeMode changeMode, List<T> list, Runnable onChange) {
            if (index > list.size()) {
                if (isStrict) {
                    throw new IndexOutOfBoundsException("Index is out of bounds and null can not be added");
                }
                return;
            }

            if (changeMode != ItemChangeMode.SET && isStrict) {
                throw new IllegalArgumentException("Null can only be used with ItemChangeMode.SET");
            }

            list.remove(index);
            onChange.run();
        }

        private void recalculateSongStatisticData() {
            lastTick = -1;
            songLength = 0;
            isStereo = false;
            nonCustomInstrumentsCount = 0;
            nonEmptyTicks.clear();

            for (Layer layer : layers) {
                updateSongStatisticData(layer);
            }

            nonEmptyTicks.addAll(tempoChanges.keySet());
        }

        private void updateSongStatisticData(@NotNull Layer layer) {
            for (Map.Entry<Long, Note> noteEntry : layer.getNotes().entrySet()) {
                long tick = noteEntry.getKey();
                Note note = noteEntry.getValue();

                if (lastTick < tick) {
                    lastTick = tick;
                }

                if (songLength <= tick) {
                    songLength = tick + 1;
                }

                if (!note.isCustomInstrument() && nonCustomInstrumentsCount <= note.getInstrument()) {
                    nonCustomInstrumentsCount = note.getInstrument() + 1;
                }

                isStereo |= note.getPanning() != Note.NEUTRAL_PANNING;
                nonEmptyTicks.add(tick);
            }

            isStereo |= layer.getPanning() != Layer.NEUTRAL_PANNING;

            updateTempoChangeSongLength();
        }

        private void updateTempoChangeSongLength() {
            if (tempoChanges.isEmpty()) {
                return;
            }

            long lastTempoChangeTick = tempoChanges.lastKey();

            if (lastTick <= lastTempoChangeTick) {
                lastTick = lastTempoChangeTick;
            }

            if (songLength <= lastTempoChangeTick) {
                songLength = lastTempoChangeTick + 1;
            }
        }

        /**
         * Specifies the length of the song.
         * @param length new length of the song
         * @throws IllegalArgumentException if song with specified length would not contain all notes or tempo changes.
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder length(long length){
            if (lastTick >= length) {
                throw new IllegalArgumentException("Specified song length would not contain all notes or tempo changes.");
            }
            songLength = length;

            return this;
        }

        public Builder initialTempo(float tempo) {
            if (tempo <= 0) {
                tempo = DEFAULT_TEMPO;
            }

            tempoChanges.put(INITIAL_TEMPO_TICK, tempo);
            return this;
        }

        /**
         * Specifies the change of tempo in ticks per seconds on specific tick.
         * @param firstTick tick since the specified tempo is used.
         * @param tempo tempo in ticks per seconds to be used from this tick on or non-positive value if you want to remove existing change of tempo
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder tempoChange(long firstTick, float tempo){
            if (firstTick <= INITIAL_TEMPO_TICK) {
                initialTempo(tempo);
                return this;
            }

            if (tempo <= 0) {
                tempoChanges.remove(firstTick);
                recalculateSongStatisticData();
                return this;
            }

            nonEmptyTicks.add(firstTick);
            tempoChanges.put(firstTick, tempo);

            updateTempoChangeSongLength();

            return this;
        }

        public Builder metadata(SongMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Creates new instance of {@link Song} based on data from this builder.
         * @return {@link Song}
         */
        @NotNull
        public Song build() {
            if (metadata == null) {
                metadata = new SongMetadata();
            }
            return new Song(this);
        }

        @FunctionalInterface
        public interface Replacer<T, B> {
            T replace(B builder, T originalNote);
        }

        public enum ItemChangeMode {
            SET, INSERT
        }

        public class InteractiveReplacer {

            private InteractiveReplacer() {
            }

            /**
             * Replaces the layer at the given index by the result of {@link Replacer} callback.
             * If the callback returns null, layer at the given index is removed and indices of all subsequent layers change.
             * If the index equals the count of layers, layer is added.
             * @param index Zero-based index of the layer to be replaced
             * @param replacer {@link Replacer} that is given an existing layer at the given index or null if there is none
             *                                         and a builder initialized with that layer or empty builder if there is none.
             * @throws IndexOutOfBoundsException if the {@link Builder} is in strict mode and the index is out of bounds
             * @return this instance of {@link Builder}
             */
            @NotNull
            public Builder layer(int index, @NotNull Replacer<Layer, Layer.Builder> replacer) {
                return generic(index, layers, replacer, l -> Layer.builder(l, isStrict), () -> Layer.builder(isStrict), l -> Builder.this.layer(index, l, ItemChangeMode.SET));
            }

            @NotNull
            public Builder customInstrument(int index, Replacer<CustomInstrument, CustomInstrument.Builder> replacer) {
                if (index > customInstruments.size()) {
                    throw new IndexOutOfBoundsException("Index is out of bounds");
                }

                return generic(index, customInstruments, replacer, c -> CustomInstrument.builder(c, isStrict), () -> CustomInstrument.builder(isStrict), c -> Builder.this.customInstrument(index, c, ItemChangeMode.SET));
            }

            private <T, B> Builder generic(int index, List<T> list, Replacer<T, B> replacer, Function<T, B> builderFactory, Supplier<B> emptyBuilderFactory, Consumer<T> applyMethod) {
                if (index < 0) {
                    if (isStrict) {
                        throw new IndexOutOfBoundsException("Index can not be lower than 0");
                    }
                    index = 0;
                }

                T originalLayer;
                B builder;
                if (index < list.size()) {
                    originalLayer = list.get(index);
                    builder = builderFactory.apply(originalLayer);
                } else {
                    originalLayer = null;
                    builder = emptyBuilderFactory.get();
                }

                T modifiedLayer = replacer.replace(builder, originalLayer);

                applyMethod.accept(modifiedLayer);

                return Builder.this;
            }
        }
    }
}
