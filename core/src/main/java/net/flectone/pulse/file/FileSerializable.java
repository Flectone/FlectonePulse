package net.flectone.pulse.file;

import lombok.Getter;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Transient;
import net.elytrium.serializer.language.object.YamlSerializable;

import java.nio.file.Path;

@Getter
public abstract class FileSerializable extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig
            .Builder()
            .setBackupOnErrors(true)
            .build();

    @Transient
    private final Path path;

    public FileSerializable(Path path) {
        super(CONFIG);
        this.path = path;
    }
}
