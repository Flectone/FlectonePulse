package net.flectone.pulse.processing.resolver.libby;

import com.alessiodp.libby.LibraryManager;
import com.alessiodp.libby.classloader.ClassLoaderHelper;
import com.alessiodp.libby.classloader.SystemClassLoaderHelper;
import com.alessiodp.libby.classloader.URLClassLoaderHelper;
import com.alessiodp.libby.logging.adapters.LogAdapter;
import net.flectone.pulse.platform.adapter.NeoforgeLogAdapter;
import net.flectone.pulse.util.file.FileLoader;
import net.neoforged.fml.loading.FMLPaths;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.net.URLClassLoader;
import java.nio.file.Path;

public class NeoForgeLibbyResolver extends LibraryManager {

    private final ClassLoaderHelper classLoaderHelper;

    public NeoForgeLibbyResolver(@NonNull String modId, @NonNull Logger logger, @NonNull String directoryName) {
        this(modId, new NeoforgeLogAdapter(logger), directoryName);
    }

    public NeoForgeLibbyResolver(@NonNull String modId, @NonNull LogAdapter logAdapter, @NonNull String directoryName) {
        super(logAdapter, FMLPaths.CONFIGDIR.get().resolve(modId), directoryName);

        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            classLoaderHelper = new URLClassLoaderHelper((URLClassLoader) classLoader, this);
        } else if (classLoader == ClassLoader.getSystemClassLoader()) {
            classLoaderHelper = new SystemClassLoaderHelper(classLoader, this);
        } else {
            throw new RuntimeException("Unsupported class loader: " + classLoader.getClass().getName());
        }
    }

    @Override
    protected void addToClasspath(@NonNull Path path) {
        if (FileLoader.ADD_FILE_TO_CLASSPATH_PREDICATE.test(path)) {
            classLoaderHelper.addToClasspath(path);
        }
    }
}