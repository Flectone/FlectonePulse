package net.flectone.pulse.processing.resolver;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.elytrium.serializer.language.object.AbstractSerializable;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.*;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


@Singleton
public class FileResolver {

    @Getter private final Map<String, Localization> localizationMap = new HashMap<>();

    @Getter private final Path projectPath;
    @Getter private final Command command;
    @Getter private final Config config;
    @Getter private final Integration integration;
    @Getter private final Message message;
    @Getter private final Permission permission;
    private final FLogger fLogger;

    @Getter private String preInitVersion;

    private Localization defaultLocalization;

    @Inject
    public FileResolver(@Named("projectPath") Path projectPath,
                        FLogger fLogger) {
        this.projectPath = projectPath;
        this.command = new Command(projectPath);
        this.config = new Config(projectPath);
        this.integration = new Integration(projectPath);
        this.message = new Message(projectPath);
        this.permission = new Permission(projectPath);
        this.fLogger = fLogger;
    }

    public Localization getLocalization() {
        return getLocalization(FPlayer.UNKNOWN);
    }

    public Localization getLocalization(FEntity sender) {
        if (!config.isLanguagePlayer()) return defaultLocalization;
        if (!(sender instanceof FPlayer fPlayer)) return defaultLocalization;

        return localizationMap.getOrDefault(fPlayer.getSetting(SettingText.LOCALE), defaultLocalization);
    }

    public void reload() {
        config.reload();
        config.setLanguage(config.getLanguage());

        preInitVersion = config.getVersion();

        if (!preInitVersion.equals(BuildConfig.PROJECT_VERSION)) {
            // fix update permission name
            if (isVersionOlderThan(preInitVersion, "1.4.3")) {
                migration_1_4_3();
            }

            if (isVersionOlderThan(preInitVersion, "1.5.0")) {
                migration_1_5_0();
            }

            config.setVersion(BuildConfig.PROJECT_VERSION);
            config.save();
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
        Set<String> newLanguages = new HashSet<>(Set.of("ru_ru", "en_us"));
        newLanguages.add(config.getLanguage());

        try (Stream<Path> paths = Files.walk(projectPath.resolve(Localization.FOLDER_NAME))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                File localization = path.toFile();
                String localizationName = localization.getName();
                if (localizationName.endsWith(".yml")) {
                    newLanguages.add(Strings.CS.replace(localizationName, ".yml", ""));
                }
            });
        } catch (NoSuchFileException ignored) {
            // ignore first startup
        } catch (IOException e) {
            fLogger.warning(e);
        }

        newLanguages.forEach(this::loadLanguage);
    }

    private void loadLanguage(String language) {
        Localization localization = new Localization(projectPath, language);
        localization.reload(localization.getPath());
        localizationMap.put(language, localization);
    }

    public boolean isVersionOlderThan(String first, String second) {
        String[] subFirst = parseVersionNumbers(first);
        if (subFirst.length != 3) return false;

        String[] subSecond = parseVersionNumbers(second);
        if (subSecond.length != 3) return true;

        for (int i = 0; i < 3; i++) {
            int intFirst = Integer.parseInt(subFirst[i]);
            int intSecond = Integer.parseInt(subSecond[i]);

            if (intFirst < intSecond) {
                return true;
            }

            if (intFirst > intSecond) {
                return false;
            }
        }

        return false;
    }

    private String[] parseVersionNumbers(String string) {
        int endIndex = string.indexOf('-');
        return (endIndex == -1 ? string : string.substring(0, endIndex)).split("\\.");
    }

    private void migration_1_4_3() {
        permission.reload();

        Permission.Message.Update update = permission.getMessage().getUpdate();
        if (update.getName().equals("flectonepulse.module.message.op")) {
            update.setName("flectonepulse.module.message.update");
            update.setSound(new Permission.PermissionEntry("flectonepulse.module.message.update.sound", Permission.Type.TRUE));
            permission.save();
        }
    }

    private void migration_1_5_0() {
        String oldChatKey = "CHAT";
        String newChatKey = "CHAT_GLOBAL";

        integration.reload();

        Integration.Discord discord = integration.getDiscord();
        if (discord.getMessageChannel().containsKey(oldChatKey)) {
            discord.getMessageChannel().put(newChatKey, discord.getMessageChannel().remove(oldChatKey));
        }

        Integration.Telegram telegram = integration.getTelegram();
        if (telegram.getMessageChannel().containsKey(oldChatKey)) {
            telegram.getMessageChannel().put(newChatKey, telegram.getMessageChannel().remove(oldChatKey));
        }

        Integration.Twitch twitch = integration.getTwitch();
        if (twitch.getMessageChannel().containsKey(oldChatKey)) {
            twitch.getMessageChannel().put(newChatKey, twitch.getMessageChannel().remove(oldChatKey));
        }

        integration.save();

        reloadLanguages();

        localizationMap.values().forEach(localization -> {
            Localization.Integration.Discord localizationDiscord = localization.getIntegration().getDiscord();
            if (localizationDiscord.getMessageChannel().containsKey(oldChatKey)) {
                localizationDiscord.getMessageChannel().put(newChatKey, localizationDiscord.getMessageChannel().remove(oldChatKey));
            }

            Localization.Integration.Twitch localizationTwitch = localization.getIntegration().getTwitch();
            if (localizationTwitch.getMessageChannel().containsKey(oldChatKey)) {
                localizationTwitch.getMessageChannel().put(newChatKey, localizationTwitch.getMessageChannel().remove(oldChatKey));
            }

            Localization.Integration.Telegram localizationTelegram = localization.getIntegration().getTelegram();
            if (localizationTelegram.getMessageChannel().containsKey(oldChatKey)) {
                localizationTelegram.getMessageChannel().put(newChatKey, localizationTelegram.getMessageChannel().remove(oldChatKey));
            }

            localization.save();
        });
    }
}
