package net.flectone.pulse.service;

/**
 * Loads the vanilla language files so the plugin can render translatable components itself,
 * for example when a message has to be flattened to plain text.
 * @author TheFaser
 */
public interface TranslationService {

    /**
     * Re-reads the language files for every locale in use.
     */
    void reload();

    /**
     * Registers the loaded translations with adventure's global translator.
     */
    void initGlobalTranslator();

}
