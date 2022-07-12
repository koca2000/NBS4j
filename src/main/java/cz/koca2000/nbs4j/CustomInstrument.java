package cz.koca2000.nbs4j;

public class CustomInstrument {

    private String name = "";
    private String fileName = "";
    private int pitch = 0;
    private boolean shouldPressKey = false;

    public CustomInstrument setName(String name){
        this.name = name;
        return this;
    }

    public CustomInstrument setFileName(String fileName){
        this.fileName = fileName;
        return this;
    }

    public CustomInstrument setPitch(int pitch){
        this.pitch = pitch;
        return this;
    }

    public CustomInstrument setShouldPressKey(boolean shouldPressKey) {
        this.shouldPressKey = shouldPressKey;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPitch() {
        return pitch;
    }

    public boolean shouldPressKey() {
        return shouldPressKey;
    }
}
