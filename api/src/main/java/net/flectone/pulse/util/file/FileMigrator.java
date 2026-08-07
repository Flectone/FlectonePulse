package net.flectone.pulse.util.file;

import net.flectone.pulse.model.file.FilePack;

/**
 * Rewrites config files whose layout changed between releases, so an existing setup keeps
 * working after an update. One method per version that needed a change.
 * @author TheFaser
 */
public interface FileMigrator {

    /**
     * Applies the changes introduced in 1.11.1.
     *
     * @param files the current files
     * @return the migrated files
     */
    FilePack migration_1_11_1(FilePack files);

    /**
     * Applies the changes introduced in 1.12.3.
     *
     * @param files the current files
     * @return the migrated files
     */
    FilePack migration_1_12_3(FilePack files);

}
