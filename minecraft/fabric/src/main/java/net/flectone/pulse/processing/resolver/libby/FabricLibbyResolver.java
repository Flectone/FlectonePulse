package net.flectone.pulse.processing.resolver.libby;

import com.alessiodp.libby.FabricLibraryManager;
import net.flectone.pulse.util.file.FileLoader;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public class FabricLibbyResolver extends FabricLibraryManager {

    public FabricLibbyResolver(@NonNull String modId, @NonNull Logger logger, @NonNull String directoryName) {
        super(modId, logger, directoryName);
    }

    @Override
    protected void addToClasspath(@NonNull Path path) {
        if (FileLoader.ADD_FILE_TO_CLASSPATH_PREDICATE.test(path)) {
            super.addToClasspath(path);
        }
    }

}
