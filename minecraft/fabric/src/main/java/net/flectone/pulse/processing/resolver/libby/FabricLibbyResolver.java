package net.flectone.pulse.processing.resolver.libby;

import com.alessiodp.libby.FabricLibraryManager;
import net.flectone.pulse.util.file.FileLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public class FabricLibbyResolver extends FabricLibraryManager {

    public FabricLibbyResolver(@NotNull String modId, @NotNull Logger logger, @NotNull String directoryName) {
        super(modId, logger, directoryName);
    }

    @Override
    protected void addToClasspath(@NotNull Path path) {
        if (FileLoader.ADD_FILE_TO_CLASSPATH_PREDICATE.test(path)) {
            super.addToClasspath(path);
        }
    }

}
