package net.flectone.pulse.module.integration;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
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
import net.flectone.pulse.util.LazyInstance;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class IntegrationModuleImpl implements IntegrationModule {

    private final FileFacade fileFacade;
    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final LazyInstance<LuckPermsModule> luckPermsModule;
    private final LazyInstance<DeeplModule> deeplModule;
    private final LazyInstance<YandexModule> yandexModule;

    protected IntegrationModuleImpl(FileFacade fileFacade,
                                    LazyInstance<PlatformServerAdapter> platformServerAdapter,
                                    ListenerRegistry listenerRegistry,
                                    ModuleController moduleController,
                                    LazyInstance<LuckPermsModule> luckPermsModule,
                                    LazyInstance<DeeplModule> deeplModule,
                                    LazyInstance<YandexModule> yandexModule) {
        this.fileFacade = fileFacade;
        this.platformServerAdapter = platformServerAdapter;
        this.listenerRegistry = listenerRegistry;
        this.moduleController = moduleController;
        this.luckPermsModule = luckPermsModule;
        this.deeplModule = deeplModule;
        this.yandexModule = yandexModule;
    }

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseIntegrationListener.class);
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(IntegrationModule.super.children());

        if (platformServerAdapter.get().hasProject("LuckPerms")) {
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
    public boolean containsEnabledChild(ModuleName moduleName) {
        if (!moduleController.containsChild(this, moduleName)) return false;

        return moduleController.isEnable(moduleName);
    }

    @Override
    public boolean hasFPlayerPermission(FPlayer fPlayer, String permission) {
        if (!moduleController.isEnable(this)) return false;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return luckPermsModule.get().hasLuckPermission(fPlayer, permission);
        }

        return false;
    }

    @Override
    public boolean isAlwaysHaveTruePermission() {
        if (!moduleController.isEnable(this)) return false;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return luckPermsModule.get().isAlwaysHaveTrue();
        }

        return false;
    }

    @Override
    public String getPrefix(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return luckPermsModule.get().getPrefix(fPlayer);
        }

        return null;
    }

    @Override
    public String getSuffix(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return null;

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return luckPermsModule.get().getSuffix(fPlayer);
        }

        return null;
    }

    @Override
    public Set<String> getGroups() {
        if (!moduleController.isEnable(this)) return Set.of();

        if (containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) {
            return luckPermsModule.get().getGroups();
        }

        return Set.of();
    }

    @Override
    public int getGroupWeight(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return 0;
        if (!containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) return 0;

        return luckPermsModule.get().getGroupWeight(fPlayer);
    }

    @Override
    public int getSortWeight(FPlayer fPlayer) {
        if (!moduleController.isEnable(this)) return 0;
        if (!containsEnabledChild(ModuleName.INTEGRATION_LUCKPERMS)) return 0;

        return luckPermsModule.get().getSortWeight(fPlayer);
    }

    @Override
    public String deeplTranslate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (containsEnabledChild(ModuleName.INTEGRATION_DEEPL)) {
            return deeplModule.get().translate(sender, source, target, text);
        }

        return text;
    }

    @Override
    public String yandexTranslate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (containsEnabledChild(ModuleName.INTEGRATION_YANDEX)) {
            return yandexModule.get().translate(sender, source, target, text);
        }

        return text;
    }
}