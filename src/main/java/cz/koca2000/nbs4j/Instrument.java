package cz.koca2000.nbs4j;

/**
 * Enumeration of Minecraft non-custom instruments
 */
public enum Instrument {
    /**
     * Block: Any than is not used by other instruments
     * Same as {@link #HARP}
     */
    PIANO(0),

    /**
     * Block: Any than is not used by other instruments
     * Same as {@link #PIANO}
     */
    HARP(0),

    /**
     * Block: Wood
     */
    BASS(1),

    /**
     * Block: Stone
     */
    BASS_DRUM(2),

    /**
     * Block: Sand
     */
    SNARE_DRUM(3),

    /**
     * Block: Glass
     */
    CLICK(4),

    /**
     * Block: Wool
     */
    GUITAR(5),

    /**
     * Block: Clay
     */
    FLUTE(6),

    /**
     * Block: Gold
     */
    BELL(7),

    /**
     * Block: Packed Ice
     */
    CHIME(8),

    /**
     * Block: Bone
     */
    XYLOPHONE(9),

    /**
     * Block: Iron
     */
    IRON_XYLOPHONE(10),

    /**
     * Block: Soul Sand
     */
    COW_BELL(11),

    /**
     * Block: Pumpkin
     */
    DIDGERIDOO(12),

    /**
     * Block: Emerald
     */
    BIT(13),

    /**
     * Block: Hay
     */
    BANJO(14),

    /**
     * Block: Glowstone
     */
    PLING(15);

    private final int id;

    Instrument(int id){
        this.id = id;
    }

    /**
     * Returns the numeric NBS id of the instrument.
     * @return id of the instrument
     */
    public int getId() {
        return id;
    }
}
