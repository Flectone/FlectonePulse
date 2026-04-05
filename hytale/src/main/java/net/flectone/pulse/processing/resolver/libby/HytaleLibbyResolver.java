package net.flectone.pulse.processing.resolver.libby;

import com.alessiodp.libby.StandaloneLibraryManager;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import net.flectone.pulse.util.file.FileLoader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class HytaleLibbyResolver extends StandaloneLibraryManager {

    public HytaleLibbyResolver(@NotNull LogAdapter logAdapter, @NotNull Path dataDirectory, @NotNull String directoryName) {
        super(logAdapter, dataDirectory, directoryName);
    }

    @Override
    protected void addToClasspath(@NotNull Path path) {
        if (FileLoader.ADD_FILE_TO_CLASSPATH_PREDICATE.test(path)) {
            super.addToClasspath(path);
        }
    }

}
