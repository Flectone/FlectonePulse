package net.flectone.pulse.module.integration.deepl;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeeplModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;

    @Override
    public void onEnable() {
        super.onEnable();

        reflectionResolver.hasClassOrElse("com.deepl.api.DeepLClient", this::loadLibraries);

        injector.getInstance(DeeplIntegration.class).hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        injector.getInstance(DeeplIntegration.class).unhook();
    }

    @Override
    public Integration.Deepl config() {
        return fileResolver.getIntegration().getDeepl();
    }

    @Override
    public Permission.Integration.Deepl permission() {
        return fileResolver.getPermission().getIntegration().getDeepl();
    }

    public String translate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;

        return injector.getInstance(DeeplIntegration.class).translate(source, target, text);
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}deepl{}api")
                .artifactId("deepl-java")
                .version(BuildConfig.DEEPL_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }
}
