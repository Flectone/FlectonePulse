package net.flectone.pulse.module.integration.yandex;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.resolver.LibraryResolver;

@Singleton
public class YandexModule extends AbstractModule {

    private final Integration.Yandex integration;
    private final Permission.Integration.Yandex permission;

    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public YandexModule(FileResolver fileResolver,
                        LibraryResolver libraryResolver,
                        Injector injector) {
        this.libraryResolver = libraryResolver;
        this.injector = injector;

        integration = fileResolver.getIntegration().getYandex();
        permission = fileResolver.getPermission().getIntegration().getYandex();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        loadLibraries();

        injector.getInstance(YandexIntegration.class).hook();
    }

    private void loadLibraries() {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}yandex{}cloud")
                .artifactId("java-sdk-services")
                .version(BuildConfig.YANDEXSDK_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String translate(FPlayer sender, String source, String target, String text) {
        if (checkModulePredicates(sender)) return text;

        return injector.getInstance(YandexIntegration.class).translate(source, target, text);
    }
}
