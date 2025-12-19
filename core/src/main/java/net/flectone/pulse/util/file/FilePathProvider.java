package net.flectone.pulse.util.file;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.*;
import net.flectone.pulse.util.constant.FilePath;

import java.nio.file.Path;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FilePathProvider {

    private final @Named("projectPath") Path projectPath;

    public Path get(Object file) {
        return switch (file) {
            case Command ignored -> projectPath.resolve(FilePath.COMMAND.getPath());
            case Config ignored -> projectPath.resolve(FilePath.CONFIG.getPath());
            case Integration ignored -> projectPath.resolve(FilePath.INTEGRATION.getPath());
            case Localization localization -> projectPath.resolve(FilePath.LOCALIZATION_FOLDER.getPath() + localization.language() + ".yml");
            case Message ignored -> projectPath.resolve(FilePath.MESSAGE.getPath());
            case Permission ignored -> projectPath.resolve(FilePath.PERMISSION.getPath());
            default -> throw new IllegalArgumentException("Incorrect file format: " + file);
        };
    }

}
