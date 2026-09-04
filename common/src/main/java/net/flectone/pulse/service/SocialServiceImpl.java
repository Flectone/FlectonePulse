package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.FColor;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.persistence.repository.SocialRepository;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.LazyInstance;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SocialServiceImpl implements SocialService {

    private final SocialRepository socialRepository;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final LazyInstance<FileFacade> fileFacade;
    private final LazyInstance<IntegrationModule> integrationModule;

    @Override
    public void invalidate() {
        socialRepository.invalidate();
    }

    @Override
    public void invalidate(UUID uuid) {
        socialRepository.invalidateColors(uuid);
        socialRepository.invalidateSettings(uuid);
        socialRepository.invalidateIgnores(uuid);
    }

    @Override
    public @Nullable String getSetting(@NonNull FPlayer fPlayer, @Nullable SettingText setting) {
        if (setting == null) return null;

        return getSetting(fPlayer, setting.name());
    }

    @Override
    public @Nullable String getSetting(@NonNull FPlayer fPlayer, @Nullable String setting) {
        if (setting == null) return null;

        return loadSettings(fPlayer).values().get(setting);
    }

    @Override
    public boolean isSetting(@NonNull FPlayer fPlayer, @Nullable ModuleName setting) {
        if (setting == null) return true;

        return isSetting(fPlayer, setting.name());
    }

    @Override
    public boolean isSetting(@NonNull FPlayer fPlayer, @Nullable String setting) {
        if (setting == null) return true;

        String value = loadSettings(fPlayer).values().get(setting);
        return value == null || "1".equals(value);
    }

    @Override
    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value) {
        saveSetting(fPlayer, setting.name(), value);
    }

    @Override
    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull String setting, @Nullable String value) {
        socialRepository.saveOrUpdateSetting(fPlayer, setting, value);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_SETTING);
        }
    }

    @Override
    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull String setting, boolean value) {
        socialRepository.saveOrUpdateSetting(fPlayer, setting, value);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_SETTING);
        }
    }

    @Override
    public SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer) {
        return loadSettings(fPlayer, true);
    }

    @Override
    public SocialRepository.@NonNull Settings loadSettings(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateSettings(fPlayer.uuid());
        }

        return socialRepository.loadSettings(fPlayer);
    }

    @Override
    @NonNull
    public Map<Integer, String> loadColors(@NonNull FPlayer fPlayer, FColor.@NonNull Type type) {
        Set<FColor> colors = loadColors(fPlayer).get(type);
        if (colors == null || colors.isEmpty()) return Map.of();

        Map<Integer, String> result = colors.stream()
                .collect(Collectors.toMap(
                        FColor::number,
                        FColor::name,
                        (v1, _) -> v1,
                        LinkedHashMap::new
                ));

        return Map.copyOf(result);
    }

    @Override
    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer) {
        return loadColors(fPlayer, true);
    }

    @Override
    @NonNull
    public Map<FColor.Type, Set<FColor>> loadColors(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateColors(fPlayer.uuid());
        }

        return socialRepository.loadColors(fPlayer);
    }

    @Override
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

    @Override
    public void saveColors(@NonNull FPlayer fPlayer, @NonNull Map<FColor.Type, Set<FColor>> colors) {
        socialRepository.saveColors(fPlayer, colors);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_COLOR);
        }
    }

    @Override
    public boolean isIgnored(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        return loadIgnores(fPlayer).stream().anyMatch(ignore -> ignore.target() == fTarget.id());
    }

    @Override
    @NonNull
    public List<Ignore> loadIgnores(FPlayer fPlayer) {
        return loadIgnores(fPlayer, true);
    }

    @Override
    @NonNull
    public List<Ignore> loadIgnores(FPlayer fPlayer, boolean cache) {
        if (!cache) {
            socialRepository.invalidateIgnores(fPlayer.uuid());
        }

        return socialRepository.loadIgnores(fPlayer);
    }

    @Override
    @NonNull
    public List<Mail> getReceiverMails(FPlayer fPlayer) {
        return socialRepository.getReceiverMails(fPlayer);
    }

    @Override
    @NonNull
    public List<Mail> getSenderMails(FPlayer fPlayer) {
        return socialRepository.getSenderMails(fPlayer);
    }

    @Override
    @NonNull
    public Optional<Ignore> saveIgnore(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget) {
        Optional<Ignore> ignore = socialRepository.saveIgnore(fPlayer, fTarget);
        if (ignore.isEmpty()) return Optional.empty();

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_IGNORE);
        }

        return ignore;
    }

    @Override
    @NonNull
    public Optional<Mail> saveMail(@NonNull FPlayer fPlayer, @NonNull FPlayer fTarget, @NonNull String message) {
        return socialRepository.saveMail(fPlayer, fTarget, message);
    }

    @Override
    public void deleteIgnore(@NonNull FPlayer fPlayer, @NonNull Ignore ignore) {
        socialRepository.deleteIgnore(fPlayer, ignore);

        if (proxyRegistry.hasEnabledProxy()) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_IGNORE);
        }
    }

    @Override
    public void deleteMail(@NonNull Mail mail) {
        socialRepository.deleteMail(mail);
    }

    @Override
    public boolean updateLocale(@NonNull FPlayer fPlayer, @NonNull String newLocale) {
        String locale = integrationModule.get().getTritonLocale(fPlayer);
        if (locale == null) {
            locale = newLocale;
        }

        SettingText settingName = SettingText.LOCALE;
        if (locale.equals(getSetting(fPlayer, settingName))) return false;
        if (fPlayer.isUnknown()) return false;

        saveSetting(fPlayer, settingName, locale);
        return true;
    }

    @Override
    public boolean hasVanishIntegration() {
        return integrationModule.get().hasVanishIntegration();
    }

    @Override
    public boolean isVanished(@NonNull FEntity fEntity) {
        if (fEntity instanceof FPlayer fPlayer) {
            FileFacade fileFacadeInstance = fileFacade.get();
            if (fileFacadeInstance.integration().supervanish().enable()
                    && fileFacadeInstance.integration().supervanish().proxySync()
                    && getSetting(fPlayer, SettingText.VANISH_STATUS) != null) {
                return true;
            }
        }

        return integrationModule.get().isVanished(fEntity);
    }

    @Override
    public boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer) {
        return canSeeVanished(fTarget, fViewer, isVanished(fTarget));
    }

    @Override
    public boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer, boolean targetVanished) {
        if (fTarget.equals(fViewer)) return true;
        if (fViewer instanceof FPlayer fPlayer && fPlayer.isConsole()) return true;

        return !targetVanished || integrationModule.get().hasSeeVanishPermission(fViewer);
    }

}