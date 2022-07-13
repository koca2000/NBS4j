package cz.koca2000.nbs4j;

public enum NBSVersion {
    /**
     * Doesn't support following features:
     * loop metadata, layer panning, layer lock, note volume, note panning, note pitch, tempo change
     */
    V1(1),

    /**
     * <p>Adds following features: layer panning
     *
     * <p>Doesn't support following features:
     * loop metadata, layer lock, note volume, note panning, note pitch, tempo change
     */
    V2(2),

    /**
     * <p>Adds following features: layer panning, (song length)
     *
     * <p>Doesn't support following features:
     * loop metadata, layer lock, note volume, note panning, note pitch, tempo change
     */
    V3(3),

    /**
     * <p>Adds following features: loop metadata, layer lock, note volume, note panning, note pitch
     *
     * <p>Doesn't support following features:
     * tempo change
     */
    V4(4),

    /**
     * <p>Only some constraints of OpenNoteBlockStudio were changed. No changes in structure.
     *
     * <p>Doesn't support following features:
     * tempo change
     */
    V5(5),

    /**
     * Same as {@link #V5}.
     */
    LATEST(5);

    private final int versionNumber;

    NBSVersion(int versionNumber){
        this.versionNumber = versionNumber;
    }

    int getVersionNumber() {
        return versionNumber;
    }
}
