package cz.koca2000.nbs4j;

public enum NBSVersion {
    V1(1),
    V2(2),
    V3(3),
    V4(4),
    V5(5);

    private final int versionNumber;

    NBSVersion(int versionNumber){
        this.versionNumber = versionNumber;
    }

    int getVersionNumber() {
        return versionNumber;
    }
}
