package net.flectone.pulse.processing.resolver.libby;

import com.alessiodp.libby.StandaloneLibraryManager;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import net.flectone.pulse.util.file.FileLoader;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;

public class HytaleLibbyResolver extends StandaloneLibraryManager {

    public HytaleLibbyResolver(@NonNull LogAdapter logAdapter, @NonNull Path dataDirectory, @NonNull String directoryName) {
        super(logAdapter, dataDirectory, directoryName);
    }

    @Override
    protected void addToClasspath(@NonNull Path path) {
        if (FileLoader.ADD_FILE_TO_CLASSPATH_PREDICATE.test(path)) {
            super.addToClasspath(path);
        }
    }

}
