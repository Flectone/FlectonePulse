package net.flectone.pulse.module.integration.yandex;

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
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class YandexModule implements AbstractModule {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final Injector injector;

    @Override
    public void onEnable() {
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
    public ModuleName name() {
        return ModuleName.INTEGRATION_YANDEX;
    }

    @Override
    public Integration.Yandex config() {
        return fileFacade.integration().yandex();
    }

    @Override
    public Permission.Integration.Yandex permission() {
        return fileFacade.permission().integration().yandex();
    }

    public String translate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;

        return injector.getInstance(YandexIntegration.class).translate(source, target, text);
    }
}
