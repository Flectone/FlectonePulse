package net.flectone.pulse.module.integration.deepl;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.resolver.LibraryResolver;

@Singleton
public class DeeplModule extends AbstractModule {

    private final Integration.Deepl integration;
    private final Permission.Integration.Deepl permission;

    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public DeeplModule(FileManager fileManager,
                       LibraryResolver libraryResolver,
                       Injector injector) {
        this.libraryResolver = libraryResolver;
        this.injector = injector;

        integration = fileManager.getIntegration().getDeepl();
        permission = fileManager.getPermission().getIntegration().getDeepl();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        loadLibraries();

        injector.getInstance(DeeplIntegration.class).hook();
    }

    private void loadLibraries() {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}deepl{}api")
                .artifactId("deepl-java")
                .version(BuildConfig.DEEPL_VERSION)
                .resolveTransitiveDependencies(true)
                .build()
        );
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String translate(FPlayer sender, String source, String target, String text) {
        if (checkModulePredicates(sender)) return text;

        return injector.getInstance(DeeplIntegration.class).translate(source, target, text);
    }
}
