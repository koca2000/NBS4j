package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

import java.util.List;

class SongUtils {

    private SongUtils() {
    }

    public static int findTempoChangerInstrumentIndex(@NotNull List<CustomInstrument> customInstruments) {
        for (int i = 0; i < customInstruments.size(); i++) {
            if (CustomInstrument.TEMPO_CHANGER_INSTRUMENT_NAME.equals(customInstruments.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }
}
