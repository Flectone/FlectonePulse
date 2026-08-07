package net.flectone.pulse.util.backup;

import net.flectone.pulse.config.Config;

import java.nio.file.Path;

/**
 * Copies config files and dumps the database before an upgrade rewrites anything.
 * @author TheFaser
 */
public interface BackupCreator {

    /**
     * Copies one file into the backup folder for the version being upgraded from.
     *
     * @param preInitVersion the version the files were written by
     * @param pathToFile the file to copy
     */
    void backup(String preInitVersion, Path pathToFile);

    /**
     * Dumps the database into the backup folder, using whichever tool the database type needs.
     *
     * @param preInitVersion the version the files were written by
     * @param database the database settings
     */
    void backup(String preInitVersion, Config.Database database);

}
