package net.flectone.pulse.file;

import java.nio.file.Path;

/**
 * Works out where a loaded config object belongs on disk.
 * @author TheFaser
 */
public interface FilePathProvider {

    /**
     * The path a config object is written to.
     *
     * @param file the loaded config object
     * @return its path inside the plugin folder
     */
    Path get(Object file);

}
