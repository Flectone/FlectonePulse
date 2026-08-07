package net.flectone.pulse.file;

import net.flectone.pulse.model.file.FilePack;

import java.nio.file.Path;

/**
 * Writes config files to disk, keeping the comments and key order of the shipped defaults.
 * @author TheFaser
 */
public interface FileWriter {

    /**
     * Stamped onto files the plugin writes itself, so a file the user never touched can be told apart from an edited one.
     */
    long LAST_MODIFIED_TIME = System.currentTimeMillis();

    /**
     * Writes a whole set of files.
     *
     * @param files the files to write
     * @param checkExist whether to skip files that already exist
     * @param checkLastModifiedTime whether to stamp the files so they are recognized as untouched later
     */
    void save(FilePack files, boolean checkExist, boolean checkLastModifiedTime);

    /**
     * Writes a single file.
     *
     * @param pathToFile where to write
     * @param fileResource the object to serialize
     * @param checkLastModifiedTime whether to stamp the file so it is recognized as untouched later
     */
    void save(Path pathToFile, Object fileResource, boolean checkLastModifiedTime);

}
