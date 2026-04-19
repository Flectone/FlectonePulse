package net.flectone.pulse.module.integration.icu;

import com.alessiodp.libby.Library;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.integration.icu.listener.ICUMessagePulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.util.concurrent.ExecutionException;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ICUModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final Injector injector;
    private final ListenerRegistry listenerRegistry;
    private final FLogger fLogger;
    private final IntegrationModule integrationModule;
    private final @Named("icuMessage") Cache<String, String> messageCache;

    @Override
    public void onEnable() {
        reflectionResolver.hasClassOrElse("com.ibm.icu.text.ArabicShaping", this::loadLibraries);

        injector.getInstance(ICUIntegration.class).hook();
        listenerRegistry.register(ICUMessagePulseListener.class);
    }

    @Override
    public void onDisable() {
        injector.getInstance(ICUIntegration.class).unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_ICU;
    }

    @Override
    public Integration.Icu config() {
        return fileFacade.integration().icu();
    }

    @Override
    public Permission.Integration.Icu permission() {
        return fileFacade.permission().integration().icu();
    }

    public String process(FEntity sender, FPlayer receiver, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;
        if (moduleController.isDisabledFor(this, receiver)) return text;
        if (!receiver.isConsole() && !integrationModule.isBedrockPlayer(receiver)) return text;

        try {
            return messageCache.get(text, () -> injector.getInstance(ICUIntegration.class).process(text));
        } catch (ExecutionException e) {
            fLogger.warning(e);
        }

        return injector.getInstance(ICUIntegration.class).process(text);
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}ibm{}icu")
                .artifactId("icu4j")
                .version(BuildConfig.ICU4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }
}
