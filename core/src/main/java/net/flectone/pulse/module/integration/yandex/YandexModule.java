package net.flectone.pulse.module.integration.yandex;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;

@Singleton
public class YandexModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final ReflectionResolver reflectionResolver;
    private final Injector injector;

    @Inject
    public YandexModule(FileResolver fileResolver,
                        ReflectionResolver reflectionResolver,
                        Injector injector) {
        this.fileResolver = fileResolver;
        this.reflectionResolver = reflectionResolver;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        reflectionResolver.hasClassOrElse("yandex.cloud.sdk.auth.Auth", this::loadLibraries);

        injector.getInstance(YandexIntegration.class).hook();
    }

    @Override
    public void onDisable() {
        injector.getInstance(YandexIntegration.class).unhook();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}yandex{}cloud")
                .artifactId("java-sdk-services")
                .version(BuildConfig.YANDEXSDK_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    public Integration.Yandex config() {
        return fileResolver.getIntegration().getYandex();
    }

    @Override
    public Permission.Integration.Yandex permission() {
        return fileResolver.getPermission().getIntegration().getYandex();
    }

    public String translate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;

        return injector.getInstance(YandexIntegration.class).translate(source, target, text);
    }
}
