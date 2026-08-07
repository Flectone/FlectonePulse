package net.flectone.pulse.util.file;

import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.model.file.FilePack;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;

/**
 * Reads config files from disk and merges them over the defaults shipped in the jar, so a
 * user file only has to carry the keys it actually overrides.
 * @author TheFaser
 */
public interface FileLoader {

    /**
     * The defaults read from the jar.
     *
     * @return the default files
     */
    FilePack getDefaultFiles();

    /**
     * Reads the defaults out of the jar and writes any file that is missing on disk.
     */
    void init();

    /**
     * Reads every file from disk, merging each over the matching current one.
     *
     * @param currentFiles the files to merge over, or null to merge over the defaults
     * @return the loaded set
     */
    FilePack loadFiles(FilePack currentFiles);

    /**
     * Reads config.yml only, which is needed before the rest so the language is known.
     *
     * @param currentFiles the files to merge over, or null to merge over the defaults
     * @return the loaded config
     */
    Config loadAndMergeConfig(FilePack currentFiles);

    /**
     * Reads every localization file present on disk plus the shipped languages.
     *
     * @param config supplies the server default language
     * @param localizations the localizations to merge over
     * @param merge whether to merge over the defaults, or take the file as-is
     * @return the loaded localizations, keyed by locale name
     */
    Map<String, Localization> loadLocalizationFiles(Config config, Map<String, Localization> localizations, boolean merge);

    /**
     * Reads one file and merges it over the given default.
     *
     * @param path the file path inside the plugin folder
     * @param defaultFile the value to merge over
     * @param mergeOperator merges the default with what was read
     * @param <T> the file type
     * @return the merged value, or the default if the file is missing
     */
    <T> T loadOrDefault(String path, T defaultFile, BinaryOperator<T> mergeOperator);

    /**
     * Reads one file, either merging it over the given default or taking it as-is.
     *
     * @param path the file path inside the plugin folder
     * @param defaultFile the value to merge over
     * @param mergeOperator merges the default with what was read
     * @param merge whether to merge, or return the file unchanged
     * @param <T> the file type
     * @return the resulting value, or the default if the file is missing
     */
    <T> T loadOrDefault(String path, T defaultFile, BinaryOperator<T> mergeOperator, boolean merge);

    /**
     * Reads a file bundled in the jar.
     *
     * @param path the resource path
     * @param type the type to parse into
     * @param <T> the file type
     * @return the parsed file
     * @throws net.flectone.pulse.exception.FileLoadException if the resource is missing or malformed
     */
    <T> T loadFromResource(String path, Class<T> type);

    /**
     * Reads a single file from disk without merging.
     *
     * @param pathToFile the file to read
     * @param defaultFile supplies the type to parse into, and is rewritten if the file is empty
     * @param <T> the file type
     * @return the parsed file, or empty if it was blank
     */
    <T> Optional<T> load(Path pathToFile, T defaultFile);

}
