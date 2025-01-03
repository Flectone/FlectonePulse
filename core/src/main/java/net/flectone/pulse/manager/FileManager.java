package net.flectone.pulse.manager;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.*;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.TagType;
import net.flectone.pulse.util.VersionUtil;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Singleton
public class FileManager {

    @Getter
    private final Set<String> languages = Set.of("ru_ru", "en_us");
    private final Map<String, Localization> localizationMap = new HashMap<>();

    private final Path pluginPath;

    @Inject
    private VersionUtil versionUtil;

    @Getter
    private final Command command;
    @Getter
    private final Config config;
    @Getter
    private final Integration integration;
    @Getter
    private final Message message;
    @Getter
    private final Permission permission;

    private Localization defaultLocalization;

    @Inject
    public FileManager(@Named("pluginPath") Path pluginPath) {
        this.pluginPath = pluginPath;

        command = new Command(pluginPath);
        command.reload(command.getPath());

        config = new Config(pluginPath);
        config.reload(config.getPath());

        integration = new Integration(pluginPath);
        integration.reload(integration.getPath());

        message = new Message(pluginPath);
        message.reload(message.getPath());

        permission = new Permission(pluginPath);
        permission.reload(permission.getPath());

        reloadLanguages();

        defaultLocalization = localizationMap.get(config.getLanguage());
    }

    public Localization getLocalization() {
        return getLocalization(FPlayer.UNKNOWN);
    }

    public Localization getLocalization(FEntity sender) {
        if (!config.isLanguagePlayer()) return defaultLocalization;
        if (!(sender instanceof FPlayer fPlayer)) return defaultLocalization;

        return localizationMap.getOrDefault(fPlayer.getLocale(), defaultLocalization);
    }

    public void reload() {
        config.reload(config.getPath());
        config.setLanguage(config.getLanguage());

        String configVersion = config.getVersion();

        if (!configVersion.equals(BuildConfig.PROJECT_VERSION)) {
            config.setVersion(BuildConfig.PROJECT_VERSION);
            config.save(config.getPath());

            upgradeIfNewerThanV_0_1_0(configVersion);
        }

        command.reload(command.getPath());
        integration.reload(integration.getPath());
        message.reload(message.getPath());
        permission.reload(permission.getPath());

        reloadLanguages();

        defaultLocalization = localizationMap.get(config.getLanguage());
    }

    public void save() {
        command.save(command.getPath());
        config.save(config.getPath());
        integration.save(integration.getPath());
        message.save(message.getPath());
        permission.save(permission.getPath());
        localizationMap.values().forEach(file -> file.save(file.getPath()));
    }

    private void reloadLanguages() {
        Set<String> newLanguages = new HashSet<>(languages);
        newLanguages.add(config.getLanguage());
        newLanguages.forEach(this::loadLanguage);
    }

    private void loadLanguage(String language) {
        Localization localization = new Localization(pluginPath, language);
        localization.reload(localization.getPath());
        localizationMap.put(language, localization);
    }

    public void upgradeIfNewerThanV_0_1_0(String version) {
        if (versionUtil.isOlderThan("0.1.0", version)) return;

        Map<TagType, Permission.PermissionEntry> permissionMap = permission.getMessage().getFormat().getTags();
        if (permissionMap.containsKey(TagType.PRIDE)) return;

        permissionMap.put(TagType.PRIDE, new Permission.PermissionEntry("flectonepulse.module.message.format.pride", Permission.Type.OP));
        permissionMap.put(TagType.SHADOW_COLOR, new Permission.PermissionEntry("flectonepulse.module.message.format.shadow_color", Permission.Type.OP));

        Map<TagType, Message.Format.Tag> messageMap = message.getFormat().getTags();
        messageMap.put(TagType.PRIDE, new Message.Format.KyoriTag());
        messageMap.put(TagType.SHADOW_COLOR, new Message.Format.KyoriTag());

        save();
    }
}
