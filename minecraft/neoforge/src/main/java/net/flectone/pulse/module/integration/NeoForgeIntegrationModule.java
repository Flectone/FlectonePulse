package net.flectone.pulse.module.integration;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.ExternalModeration;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

@Singleton
public class NeoForgeIntegrationModule extends MinecraftIntegrationModule {

    private final Provider<PermissionChecker> permissionCheckerProvider;
    private final Provider<PlatformServerAdapter> platformServerAdapterProvider;
    private final Injector injector;

    @Inject
    public NeoForgeIntegrationModule(FileFacade fileManager,
                                     FLogger fLogger,
                                     Provider<PlatformServerAdapter> platformServerAdapterProvider,
                                     Provider<PermissionChecker> permissionCheckerProvider,
                                     ReflectionResolver reflectionResolver,
                                     ListenerRegistry listenerRegistry,
                                     ModuleController moduleController,
                                     Injector injector) {
        super(fileManager, fLogger, platformServerAdapterProvider, reflectionResolver, listenerRegistry, moduleController, injector);

        this.permissionCheckerProvider = permissionCheckerProvider;
        this.platformServerAdapterProvider = platformServerAdapterProvider;
        this.injector = injector;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return super.childrenBuilder();
    }

    @Override
    public String checkMention(FEntity fPlayer, String message) {
        return message;
    }

    @Override
    public boolean isVanished(FEntity sender) {
        return false;
    }

    @Override
    public boolean hasSeeVanishPermission(FEntity sender) {
        return permissionCheckerProvider.get().check(sender, "vanish.feature.view");
    }

    @Override
    public boolean sendMessageWithInteractiveChat(FEntity fReceiver, Component message) {
        return false;
    }

    @Override
    public boolean isMuted(FPlayer fPlayer) {
        return false;
    }

    @Override
    public ExternalModeration getMute(FPlayer fPlayer) {
        return null;
    }

    @Override
    public String getTritonLocale(FPlayer fPlayer) {
        return null;
    }
}
