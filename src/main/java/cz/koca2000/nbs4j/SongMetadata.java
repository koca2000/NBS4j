package cz.koca2000.nbs4j;

public class SongMetadata {

    private String title = "";
    private String author = "";
    private String originalAuthor = "";
    private String description = "";

    public SongMetadata withTitle(String title){
        this.title = title;
        return this;
    }

    public SongMetadata withAuthor(String author){
        this.author = author;
        return this;
    }

    public SongMetadata withOriginalAuthor(String originalAuthor){
        this.originalAuthor = originalAuthor;
        return this;
    }

    public SongMetadata withDescription(String description){
        this.description = description;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getOriginalAuthor() {
        return originalAuthor;
    }

    public String getDescription() {
        return description;
    }
}
