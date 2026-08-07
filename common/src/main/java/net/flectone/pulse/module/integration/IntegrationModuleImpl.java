package net.flectone.pulse.module.integration;

import com.google.inject.Injector;
import com.google.inject.Provider;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.icu.ICUModule;
import net.flectone.pulse.module.integration.listener.PulseIntegrationListener;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class IntegrationModuleImpl implements IntegrationModule {

    private final FileFacade fileFacade;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final Injector injector;

    protected IntegrationModuleImpl(FileFacade fileFacade,
                                Provider<PlatformServerAdapter> platformServerAdapterProvider,
                                ListenerRegistry listenerRegistry,
                                ModuleController moduleController,
                                Injector injector) {
        this.fileFacade = fileFacade;
        this.platformServerAdapterProvider = platformServerAdapterProvider;
        this.listenerRegistry = listenerRegistry;
        this.moduleController = moduleController;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseIntegrationListener.class);
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(IntegrationModule.super.children());

        if (platformServerAdapterProvider.get().hasProject("LuckPerms")) {
            children.add(LuckPermsModule.class);
        }

        children.add(DeeplModule.class);
        children.add(DiscordModule.class);
        children.add(ICUModule.class);
        children.add(TelegramModule.class);
        children.add(TwitchModule.class);
        children.add(YandexModule.class);

        return Collections.unmodifiableSet(children);
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION;
    }

    @Override
    public Integration config() {
        return fileFacade.integration();
    }

    @Override
    public Permission.Integration permission() {
        return fileFacade.permission().integration();
    }

    @Override
    public abstract String checkMention(FEntity fPlayer, String message);

    @Override
    public abstract boolean isVanished(FEntity sender);

    @Override
    public abstract boolean hasVanishIntegration();

    @Override
    public abstract boolean hasSeeVanishPermission(FEntity sender);

    @Override
    public abstract boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message);

    @Override
    public abstract boolean isMuted(FPlayer fPlayer);

    @Override
    public abstract boolean isBedrockPlayer(FEntity fPlayer);

    @Override
    public abstract ExternalModeration getMute(FPlayer fPlayer);

    @Override
    public abstract String getTritonLocale(FPlayer fPlayer);

    @Override
    public boolean containsEnabledChild(ModuleName moduleName) {
        if (!moduleController.containsChild(this, moduleName)) return false;

        return moduleController.isEnable(moduleName);
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        if (!moduleController.isEnable(this)) return false;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return getInstance(LuckPermsModule.class).hasLuckPermission(fPlayer, permission);
        }

        return false;
    }

    @Override
    public boolean isAlwaysHaveTruePermission() {
        if (!moduleController.isEnable(this)) return false;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return getInstance(LuckPermsModule.class).isAlwaysHaveTrue();
        }

        return false;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return injector.getInstance(LuckPermsModule.class).getPrefix(fPlayer);
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return injector.getInstance(LuckPermsModule.class).getSuffix(fPlayer);
        }

        return null;
    }

    @Override
    public Set<String> getGroups() {
        if (!moduleController.isEnable(this)) return Set.of();

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return injector.getInstance(LuckPermsModule.class).getGroups();
        }

        return Set.of();
    }

    @Override
    public int getGroupWeight(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return 0;
        if (!containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) return 0;

        return injector.getInstance(LuckPermsModule.class).getGroupWeight(fPlayer);
    }

    @Override
    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (containsEnabledChild(ModuleName.INTEGRATION_DEEPL)) {
            return injector.getInstance(DeeplModule.class).translate(sender, source, target, text);
        }

        return text;
    }

    @Override
    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (containsEnabledChild(ModuleName.INTEGRATION_YANDEX)) {
            return injector.getInstance(YandexModule.class).translate(sender, source, target, text);
        }

        return text;
    }
}
