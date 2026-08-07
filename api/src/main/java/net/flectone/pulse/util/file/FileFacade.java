package net.flectone.pulse.util.file;

import net.flectone.pulse.config.*;
import net.flectone.pulse.model.file.FilePack;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * The single entry point to the loaded config files. Everything reads settings through here
 * so a reload can swap the whole set at once.
 * @author TheFaser
 */
public interface FileFacade {

    /**
     * Re-reads every file from disk and replaces the loaded set.
     *
     * @throws IOException if a file cannot be read
     */
    void reload() throws IOException;

    /**
     * The loaded command.yml.
     *
     * @return the command settings
     */
    Command command();

    /**
     * The loaded config.yml.
     *
     * @return the general settings
     */
    Config config();

    /**
     * The loaded integration.yml.
     *
     * @return the integration settings
     */
    Integration integration();

    /**
     * The loaded message.yml.
     *
     * @return the message settings
     */
    Message message();

    /**
     * The loaded permission.yml.
     *
     * @return the permission settings
     */
    Permission permission();

    /**
     * Every loaded localization, keyed by locale name.
     *
     * @return the localizations
     */
    Map<String, Localization> localizations();

    /**
     * The localization for the server default language.
     *
     * @return the default localization
     */
    Localization localization();

    /**
     * The localization for a locale, falling back to the server default when it is unknown.
     *
     * @param locale the locale name, may be null
     * @return the matching localization, or the default one
     */
    Localization localization(@Nullable String locale);

    /**
     * Writes the loaded files back to disk.
     */
    void saveFiles();

    /**
     * Runs the pending migrations and writes the results to disk.
     */
    void updateFiles();

    /**
     * Replaces the loaded set with a modified copy, used to correct a setting at runtime.
     *
     * @param filePackOperator produces the new set from the current one
     */
    void updateFilePack(UnaryOperator<FilePack> filePackOperator);

    /**
     * The plugin version the config files were written by, read before any migration ran.
     *
     * @return the previous version, or null if the files are new
     */
    String getPreInitVersion();

}
