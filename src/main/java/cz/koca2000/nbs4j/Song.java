package cz.koca2000.nbs4j;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Song {

    private final List<Layer> layers = new ArrayList<>();
    private boolean isSongFrozen = false;

    private final SongMetadata metadata = new SongMetadata();
    private boolean isStereo = false;
    private int songLength = 0;

    private int nonCustomInstrumentsCount = 0;
    private final List<CustomInstrument> customInstruments = new ArrayList<>();

    private final TreeSet<Integer> nonEmptyTicks = new TreeSet<>();

    // <Tick, Tempo>
    private final TreeMap<Integer, Float> tempoChanges = new TreeMap<>();
    private int lastTick = -1;

    /**
     * Adds the custom instrument to the end of the list with custom instruments of this song.
     * @param customInstrument {@link CustomInstrument} to be added.
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of the {@link Song}
     */
    public Song addCustomInstrument(CustomInstrument customInstrument){
        throwIfSongFrozen();

        customInstrument.setSong(this);

        customInstruments.add(customInstrument);
        return this;
    }

    /**
     * Adds the layer to the song.
     * @param layer layer to be added
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    public Song addLayer(Layer layer){
        return setLayer(layers.size(), layer);
    }

    /**
     * Sets the layer on the specified index. Existing layer on the same index is overwritten.
     * @param index index of the layer.
     * @param layer layer to be placed to the specified index.
     * @throws IndexOutOfBoundsException if the index is negative or there is a lower empty index.
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    public Song setLayer(int index, Layer layer){
        throwIfSongFrozen();

        if (index < 0)
            throw new IndexOutOfBoundsException("Index can not be negative.");

        if (index > layers.size())
            throw new IndexOutOfBoundsException("There are still missing layer information for lower indexes.");

        layer.setSong(this);
        if (index == layers.size())
            layers.add(layer);
        else
        {
            layers.get(index).removedFromSong();
            layers.set(index, layer);
        }

        if (layer.getPanning() != Layer.NEUTRAL_PANNING)
            isStereo = true;

        return this;
    }

    /**
     * Creates as many {@link Layer} as needed to have specified count of layers.
     * @param count amount of layers the song should contain
     * @throws IllegalArgumentException if specified count is lower than number of existing layers.
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    public Song setLayersCount(int count){
        throwIfSongFrozen();

        if (count < layers.size())
            throw new IllegalArgumentException("Layers can not be removed.");

        for (int i = layers.size(); i < count; i++){
            layers.add(new Layer().setSong(this));
        }

        return this;
    }

    /**
     * Adds the note to the song on specified tick and layer.
     * If the tick is higher than song's length, song is prolonged.
     * @param tick tick of the note
     * @param layerIndex index of layer on which the song should be placed
     * @param note Note to be added
     * @throws IllegalArgumentException of tick is negative.
     * @throws IndexOutOfBoundsException if layer index is negative or out of range.
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    public Song setNote(int tick, int layerIndex, Note note){
        throwIfSongFrozen();

        if (tick < 0)
            throw new IllegalArgumentException("Tick can not be negative.");

        if (layerIndex >= layers.size())
            throw new IndexOutOfBoundsException("Layer index is out of range.");

        Layer layer = layers.get(layerIndex);
        layer.setNote(tick, note);

        if (!note.isCustomInstrument())
            increaseNonCustomInstrumentsCountTo(note.getInstrument() + 1);

        nonEmptyTicks.add(tick);
        if (lastTick < tick)
            lastTick = tick;
        if (songLength <= tick)
            songLength = tick + 1;

        if (note.getPanning() != Note.NEUTRAL_PANNING)
            isStereo = true;

        return this;
    }

    /**
     * Specifies the length of the song.
     * @param length new length of the song
     * @throws IllegalArgumentException if song with specified length would not contain all notes or tempo changes.
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    public Song setLength(int length){
        throwIfSongFrozen();

        if (lastTick >= length)
            throw new IllegalArgumentException("Specified song length would not contain all notes or tempo changes.");
        songLength = length;
        return this;
    }

    /**
     * Specifies whether the song has notes or layers (notes or layers with panning).
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    Song setStereo(){
        throwIfSongFrozen();

        this.isStereo = true;
        return this;
    }

    Song increaseNonCustomInstrumentsCountTo(int nonCustomInstrumentsCount) {
        if (this.nonCustomInstrumentsCount < nonCustomInstrumentsCount)
            this.nonCustomInstrumentsCount = nonCustomInstrumentsCount;
        return this;
    }

    /**
     * Specifies the change of tempo in ticks per seconds on specific tick.
     * @param firstTick tick since the specified tempo is used.
     * @param tempo tempo in ticks per seconds to be used from this tick on
     * @throws IllegalStateException if the song is frozen and can not be modified.
     * @return this instance of {@link Song}
     */
    public Song setTempoChange(int firstTick, float tempo){
        throwIfSongFrozen();

        if (firstTick >= 0)
            nonEmptyTicks.add(firstTick);

        tempoChanges.put(firstTick, tempo);

        return this;
    }

    private void throwIfSongFrozen(){
        if (isSongFrozen)
            throw new IllegalStateException("Song is frozen and can not be modified.");
    }

    /**
     * Freezes the song, its layers and notes so only its metadata can be modified.
     * @return this instance of {@link Song}
     */
    public Song freezeSong(){
        if (isSongFrozen)
            return this;

        for (Layer layer : layers) {
            layer.freeze();
        }

        for (CustomInstrument instrument : customInstruments){
            instrument.freeze();
        }

        isSongFrozen = true;

        return this;
    }

    /**
     * Checks if the song is unmodifiable except the metadata.
     * @return true if the song is frozen; otherwise, false
     */
    public boolean isSongFrozen(){
        return isSongFrozen;
    }

    /**
     * Provides {@link SongMetadata} of the Song
     * @return {@link SongMetadata}
     */
    public SongMetadata getMetadata(){
        return metadata;
    }

    /**
     * Provides {@link Layer} with the specified index.
     * @param index index of the layer to return
     * @throws IndexOutOfBoundsException if the index is out of range
     * @return {@link Layer} with the specified index
     */
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
    public int getSongLength() {
        return songLength;
    }

    /**
     * Returns the tick greater than the given tick that contains note or tempo change.
     * @param fromTick tick after which the next tick should be searched
     * @return tick number if there is any note or tempo change left; otherwise, -1
     */
    public int getNextNonEmptyTick(int fromTick){
        NavigableSet<Integer> subset = nonEmptyTicks.tailSet(fromTick, false);
        if (subset.size() == 0)
            return -1;

        return subset.first();
    }

    /**
     * Returns song tempo in ticks per second on the given tick
     * @param tick tick for which the tempo is requested
     * @return tempo in ticks per second
     */
    public float getTempo(int tick){
        if (tempoChanges.size() == 0)
            return 10;
        return tempoChanges.floorKey(tick);
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
     * Saves this song to the given stream using the given nbs version.
     * @param nbsVersion version of nbs data format
     * @param stream output stream the song will be written to
     */
    public void save(NBSVersion nbsVersion, OutputStream stream){
        NBSWriter.writeSong(this, nbsVersion.getVersionNumber(), stream);
    }

    /**
     * Saves this song to the given file using the given nbs version.
     * @param nbsVersion version of nbs data format
     * @param file file the song will be written to
     * @throws IOException if the file can not be written
     */
    public void save(NBSVersion nbsVersion, File file) throws IOException{
        NBSWriter.writeSong(this, nbsVersion.getVersionNumber(), Files.newOutputStream(file.toPath()));
    }

    /**
     * Loads song from given file
     * @param file file to be loaded
     * @return loaded instance of {@link Song}
     * @throws IOException if file does not exist or can not be opened
     * @throws SongCorruptedException if an error occurred during the loading
     */
    public static Song fromFile(File file) throws IOException {
        return NBSReader.readSong(Files.newInputStream(file.toPath()));
    }

    /**
     * Loads song from the given stream
     * @param stream stream from which the song will be loaded
     * @return loaded instance of {@link Song}
     * @throws SongCorruptedException if an error occurred during the loading
     */
    public static Song fromStream(InputStream stream) {
        return NBSReader.readSong(stream);
    }
}
