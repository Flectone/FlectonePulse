package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.data.repository.SocialRepository;
import net.flectone.pulse.model.FColor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.ignore.model.Ignore;
import net.flectone.pulse.module.command.mail.model.Mail;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
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

    @Inject
    private Provider<FileFacade> fileFacadeProvider;

    @Inject
    private Provider<IntegrationModule> integrationModuleProvider;

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
    public @NonNull String getSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName moduleName) {
        return getSetting(fPlayer, moduleName.name());
    }

    @Override
    public @Nullable String getSetting(@NonNull FPlayer fPlayer, @Nullable SettingText settingText) {
        return loadSettings(fPlayer).texts().get(settingText);
    }

    @Override
    public @NonNull String getSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName) {
        return isSetting(fPlayer, moduleName) ? "1" : "0";
    }

    @Override
    public boolean isSetting(@NonNull FPlayer fPlayer, @NonNull ModuleName messageType) {
        return isSetting(fPlayer, messageType.name());
    }

    @Override
    public boolean isSetting(@NonNull FPlayer fPlayer, @Nullable String moduleName) {
        Boolean value = loadSettings(fPlayer).booleans().get(moduleName);
        return value == null || value;
    }

    @Override
    public void saveSetting(@NonNull FPlayer fPlayer, @NonNull SettingText setting, @Nullable String value) {
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
        String locale = integrationModuleProvider.get().getTritonLocale(fPlayer);
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
    public boolean isVanished(@NonNull FEntity fEntity) {
        return isVanished(fEntity, false);
    }

    @Override
    public boolean isVanished(@NonNull FEntity fEntity, boolean checkVanishIntegration) {
        IntegrationModule integrationModule = integrationModuleProvider.get();
        if (checkVanishIntegration && !integrationModule.hasVanishIntegration()) return false;

        if (fEntity instanceof FPlayer fPlayer) {
            FileFacade fileFacade = fileFacadeProvider.get();
            if (fileFacade.integration().supervanish().enable()
                    && fileFacade.integration().supervanish().proxySync()
                    && getSetting(fPlayer, SettingText.VANISH_STATUS) != null) {
                return true;
            }
        }

        return integrationModule.isVanished(fEntity);
    }

    @Override
    public boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer) {
        return canSeeVanished(fTarget, fViewer, isVanished(fTarget));
    }

    @Override
    public boolean canSeeVanished(@NonNull FEntity fTarget, @NonNull FEntity fViewer, boolean targetVanished) {
        if (fTarget.equals(fViewer)) return true;
        if (fViewer instanceof FPlayer fPlayer && fPlayer.isConsole()) return true;

        return !targetVanished || integrationModuleProvider.get().hasSeeVanishPermission(fViewer);
    }

}
