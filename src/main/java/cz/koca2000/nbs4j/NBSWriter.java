package cz.koca2000.nbs4j;

import java.io.DataOutputStream;
import java.io.OutputStream;

class NBSWriter {

    public static void writeSong(Song song, int nbsVersion, OutputStream stream){
        DataOutputStream outputStream = new DataOutputStream(stream);
    }

}
