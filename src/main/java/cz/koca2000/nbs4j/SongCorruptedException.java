package cz.koca2000.nbs4j;

public class SongCorruptedException extends RuntimeException {

    public SongCorruptedException() {
        super("Song corrupted!");
    }

    public SongCorruptedException(Throwable cause) {
        super("Song corrupted!", cause);
    }
}
