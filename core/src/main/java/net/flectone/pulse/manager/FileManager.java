package net.flectone.pulse.manager;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.elytrium.serializer.language.object.AbstractSerializable;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.TagType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Singleton
public class FileManager {

    @Getter private final Set<String> languages = Set.of("ru_ru", "en_us");
    private final Map<String, Localization> localizationMap = new HashMap<>();

    private final Path projectPath;

    @Getter private final Command command;
    @Getter private final Config config;
    @Getter private final Integration integration;
    @Getter private final Message message;
    @Getter private final Permission permission;

    private Localization defaultLocalization;

    @Inject
    public FileManager(@Named("projectPath") Path projectPath) {
        this.projectPath = projectPath;

        command = new Command(projectPath);
        command.reload();

        config = new Config(projectPath);
        config.reload();

        integration = new Integration(projectPath);
        integration.reload();

        message = new Message(projectPath);
        message.reload();

        permission = new Permission(projectPath);
        permission.reload();

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
        config.reload();
        config.setLanguage(config.getLanguage());

        String configVersion = config.getVersion();

        if (!configVersion.equals(BuildConfig.PROJECT_VERSION)) {
            config.setVersion(BuildConfig.PROJECT_VERSION);
            config.save();

            upgradeIfNewerThanV_0_1_0(configVersion);
        }

        command.reload();
        integration.reload();
        message.reload();
        permission.reload();

        reloadLanguages();

        defaultLocalization = localizationMap.get(config.getLanguage());
    }

    public void save() {
        command.save();
        config.save();
        integration.save();
        message.save();
        permission.save();
        localizationMap.values().forEach(AbstractSerializable::save);
    }

    private void reloadLanguages() {
        Set<String> newLanguages = new HashSet<>(languages);
        newLanguages.add(config.getLanguage());
        newLanguages.forEach(this::loadLanguage);
    }

    private void loadLanguage(String language) {
        Localization localization = new Localization(projectPath, language);
        localization.reload(localization.getPath());
        localizationMap.put(language, localization);
    }

    public void upgradeIfNewerThanV_0_1_0(String version) {
        if (isOlderThan("0.1.0", version)) return;

        Map<TagType, Permission.PermissionEntry> permissionMap = permission.getMessage().getFormat().getTags();
        if (permissionMap.containsKey(TagType.PRIDE)) return;

        permissionMap.put(TagType.PRIDE, new Permission.PermissionEntry("flectonepulse.module.message.format.pride", Permission.Type.OP));
        permissionMap.put(TagType.SHADOW_COLOR, new Permission.PermissionEntry("flectonepulse.module.message.format.shadow_color", Permission.Type.OP));

        Map<TagType, Message.Format.Tag> messageMap = message.getFormat().getTags();
        messageMap.put(TagType.PRIDE, new Message.Format.KyoriTag());
        messageMap.put(TagType.SHADOW_COLOR, new Message.Format.KyoriTag());

        save();
    }

    public boolean isOlderThan(String first, String second) {
        String[] subFirst = first.split("\\.");
        if (subFirst.length != 3) return false;

        String[] subSecond = second.split("\\.");
        if (subSecond.length != 3) return true;

        for (int i = 0; i < 3; i++) {
            int intFirst = Integer.parseInt(subFirst[i]);
            int intSecond = Integer.parseInt(subSecond[i]);

            if (intFirst < intSecond) {
                return true;
            }
        }

        return false;
    }
}
