package net.flectone.pulse.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public abstract class YamlFile {

    @JsonIgnore
    private final Path pathToFile;

    protected YamlFile(Path pathToFile) {
        this.pathToFile = pathToFile;
    }

}