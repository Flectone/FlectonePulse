package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialService {

    private final IntegrationModule integrationModule;
    private final SocialRepository socialRepository;

    public @NonNull String getSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName moduleName) {
        return getSetting(fPlayer, moduleName.name());
    }

    public @Nullable String getSetting(@NonNull FPlayer fPlayer, @Nullable SettingText settingText) {
        return loadSettings(fPlayer).texts().get(settingText);
    }

    public @NonNull String getSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName) {
        return isSetting(fPlayer, moduleName) ? "1" : "0";
    }

    public boolean isSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName messageType) {
        return isSetting(fPlayer, messageType.name());
    }

    public boolean isSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName) {
        Boolean value = loadSettings(fPlayer).booleans().get(moduleName);
        return value == null || value;
    }

    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value) {
        socialRepository.saveOrUpdateSetting(fPlayer, setting, value);
    }

    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value) {
        socialRepository.saveOrUpdateSetting(fPlayer, setting, value);
    }

    public SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer) {
        return loadSettings(fPlayer, true);
    }

    public SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateSettings(fPlayer.uuid());
        }

        return socialRepository.loadSettings(fPlayer);
    }

    @NonNull
    public Map<Integer, String> loadColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type) {
        Set<FColor> colors = loadColors(fPlayer).get(type);
        if (colors == null || colors.isEmpty()) return Map.of();

        Map<Integer, String> result = colors.stream()
                .collect(Collectors.toMap(
                        FColor::number,
                        FColor::name,
                        (v1, v2) -> v1,
                        Int2ObjectArrayMap::new
                ));

        return Map.copyOf(result);
    }

    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer) {
        return loadColors(fPlayer, true);
    }

    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateColors(fPlayer.uuid());
        }

        return socialRepository.loadColors(fPlayer);
    }

    public void saveColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type, @Nullable Set<FColor> newColors) {
        Map<FColor.Type, Set<FColor>> fColors = loadColors(fPlayer);

        boolean newFColorsEmpty = newColors == null || newColors.isEmpty();
        boolean oldFColorsEmpty = fColors.isEmpty();
        if (newFColorsEmpty && oldFColorsEmpty) {
            saveColors(fPlayer, Map.of(type, Set.of()));
            return;
        }

        Map<FColor.Type, Set<FColor>> fColorMap = oldFColorsEmpty
                ? new EnumMap<>(FColor.Type.class)
                : new EnumMap<>(fColors);

        if (newFColorsEmpty) {
            fColorMap.put(type, Set.of());
        } else {
            fColorMap.put(type, Set.copyOf(newColors));
        }

        saveColors(fPlayer, Map.copyOf(fColorMap));
    }

    public void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        socialRepository.saveColors(fPlayer, colors);
    }

    public boolean isIgnored(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        return loadIgnores(fPlayer).stream().anyMatch(ignore -> ignore.target() == fTarget.id());
    }

    @NonNull
    public List<Ignore> loadIgnores(FPlayer fPlayer) {
        return loadIgnores(fPlayer, true);
    }

    @NonNull
    public List<Ignore> loadIgnores(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateIgnores(fPlayer.uuid());
        }

        return socialRepository.loadIgnores(fPlayer);
    }

    @NonNull
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return socialRepository.getReceiverMails(fPlayer);
    }

    @NonNull
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return socialRepository.getSenderMails(fPlayer);
    }

    @NonNull
    public Optional<Ignore> saveIgnore(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        return socialRepository.saveIgnore(fPlayer, fTarget);
    }

    @NonNull
    public Optional<Mail> saveMail(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget, @NonNull String message) {
        return socialRepository.saveMail(fPlayer, fTarget, message);
    }

    public void deleteIgnore(@NonNull FPlayer fPlayer, @NonNull Ignore ignore) {
        socialRepository.deleteIgnore(fPlayer, ignore);
    }

    public void deleteMail(@NonNull Mail mail) {
        socialRepository.deleteMail(mail);
    }

    public boolean updateLocale(@NonNull FPlayer fPlayer, @NonNull String newLocale) {
        String locale = integrationModule.getTritonLocale(fPlayer);
        if (locale == null) {
            locale = newLocale;
        }

        SettingText settingName = SettingText.LOCALE;
        if (locale.equals(getSetting(fPlayer, settingName))) return false;
        if (fPlayer.isUnknown()) return false;

        saveSetting(fPlayer, settingName, locale);
        return true;
    }

}
