package cz.koca2000.nbs4j;

/**
 * Exception used if the loaded song is corrupted (can not be properly loaded).
 * May contain nested exception with details.
 */
public class SongCorruptedException extends RuntimeException {

    public SongCorruptedException() {
        super("Song corrupted!");
    }

    public SongCorruptedException(Throwable cause) {
        super("Song corrupted!", cause);
    }
}
