package cz.koca2000.nbs4j;

public class CustomInstrument {

    private String name;
    private String fileName;
    private int pitch;

    public CustomInstrument withName(String name){
        this.name = name;
        return this;
    }

    public CustomInstrument withFileName(String fileName){
        this.fileName = fileName;
        return this;
    }

    public CustomInstrument withPitch(int pitch){
        this.pitch = pitch;
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
}
