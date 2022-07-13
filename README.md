# NBS4j

[![Releases](https://badgen.net/github/release/koca2000/NBS4j/stable)](https://github.com/koca2000/NBS4j/releases)
[![Status](https://badgen.net/github/status/koca2000/NBS4j)](https://github.com/koca2000/NBS4j/actions)
[![License](https://badgen.net/github/license/koca2000/NBS4j)](https://github.com/koca2000/NBS4j/blob/master/LICENSE)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/T6T7D1KVW)

NBS4j is library that allows Java applications to read, create and save files in NoteBlockSong format used 
by [OpenNoteBlockStudio](https://github.com/OpenNBS/OpenNoteBlockStudio).

Library supports reading of .nbs data format of versions from 0 to 5 and saving as versions from 1 to 5. 
Features unsupported in specified version are ignored.  

## Installation

Library can be used with Java 8+. Jitpack.io is used as a package repository.

### Maven
Add Jitpack.io repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

If the library will be provided as external dependency you can use following:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.koca2000</groupId>
        <artifactId>NBS4j</artifactId>
        <version>1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

In case you want to include the library in your jar file, use following:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.koca2000</groupId>
        <artifactId>NBS4j</artifactId>
        <version>1.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>

<build>
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.1.0</version>
        <configuration>
            <relocations>
                <relocation>
                    <pattern>cz.koca2000.nbs4j</pattern>
                    <shadedPattern>your.package.nbs4j</shadedPattern>
                </relocation>
            </relocations>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
</build>
```

### Gradle

Add Jitpack.io repository:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add NBS4j as a dependecy:

```groovy
dependencies {
    implementation 'com.github.koca2000:NBS4j:1.0'
}
```

## Usage

All library methods are using builder pattern which allows you to chain commands

### Getting Song instance 

You can load the nbs Song from file or stream:

```java
Song songFromFile = Song.fromFile(new File(<path to file>));
Song songFromStream = Song.fromStream(<your InputStream>);
```

Or you can create a new nbs song:

```java
Song song = new Song();
```

### Modifying the song

Following example shows how to add layers and notes to the song.

```java
Song song;
song.addLayer(new Layer()
        .setVolume(50)
        .setPanning(50))
    .setNote(4, 0, new Note() // tick, layer, note
        .setInstrument(5)
        .setKey(52)
        .setPitch(20));
```

### Saving the song

The created or modified song can also be saved to file or stream:

```java
Song song;
song.save(NBSVersion.LATEST, new File(<path>));
song.save(NBSVersion.V5, <your OutputStream>);
```

### Freezing the song

In order to prevent race conditions when using multiple threads you may want to make the song immutable. To freeze whole song except of its metadata you can use `Song.freeze()` method.

## Links

* [OpenNoteBlockStudio](https://github.com/OpenNBS/OpenNoteBlockStudio)
* [NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI)