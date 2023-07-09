package cz.koca2000.nbs4j;

import org.jetbrains.annotations.NotNull;

public class CustomInstrument {

    private final String name;
    private final String fileName;
    private final int key;
    private final boolean shouldPressKey;

    private CustomInstrument(@NotNull Builder builder){
        name = builder.name;
        fileName = builder.fileName;
        key = builder.key;
        shouldPressKey = builder.shouldPressKey;
    }

    /**
     * Returns the name of this custom instrument.
     * @return name of the instrument
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns name of the file used in OpenNoteBlockStudio for this custom instrument.
     * @return file name
     */
    @NotNull
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the key of this custom instrument.
     * @return value in range [0; 87]; 0 is A0 and 87 is C8.
     */
    public int getKey() {
        return key;
    }

    /**
     * Returns whether OpenNoteBlockStudio should press key on piano when playing this instrument's note.
     * @return true if the key should be pressed; otherwise, false
     */
    public boolean shouldPressKey() {
        return shouldPressKey;
    }

    public static final class Builder {
        private String name = "";
        private String fileName = "";
        private int key = 0;
        private boolean shouldPressKey = false;

        /**
         * Sets the name of this custom instrument
         * @param name name of the custom instrument
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setName(@NotNull String name){
            this.name = name;
            return this;
        }

        /**
         * Sets the name of the file used in OpenNoteBlockStudio
         * @param fileName name of the file
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setFileName(@NotNull String fileName){
            this.fileName = fileName;
            return this;
        }

        /**
         * Key of this custom instrument.
         * @param key Value 0 is A0 and 87 is C8.
         * @return this instance of {@link Builder}
         * @throws IllegalArgumentException if the argument is not in range [0; 87] inclusive.
         */
        @NotNull
        public Builder setKey(int key){
            if (key < 0 || key > 87)
                throw new IllegalArgumentException("Key must be in range [0; 87].");
            this.key = key;
            return this;
        }

        /**
         * Sets whether OpenNoteBlockStudio should press key on piano when playing this instrument's note.
         * @param shouldPressKey true if the key should be pressed; otherwise, false
         * @return this instance of {@link Builder}
         */
        @NotNull
        public Builder setShouldPressKey(boolean shouldPressKey) {
            this.shouldPressKey = shouldPressKey;
            return this;
        }

        /**
         * Creates new instance of {@link CustomInstrument} based on data from this builder.
         * @return {@link CustomInstrument}
         */
        public CustomInstrument build() {
            return new CustomInstrument(this);
        }
    }
}
