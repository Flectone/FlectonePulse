package net.flectone.pulse.module.integration.deepl;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.LibraryResolver;

@Singleton
public class DeeplModule extends AbstractModule {

    private final Integration.Deepl integration;
    private final Permission.Integration.Deepl permission;
    private final LibraryResolver libraryResolver;
    private final Injector injector;

    @Inject
    public DeeplModule(FileResolver fileResolver,
                       LibraryResolver libraryResolver,
                       Injector injector) {
        this.integration = fileResolver.getIntegration().getDeepl();
        this.permission = fileResolver.getPermission().getIntegration().getDeepl();
        this.libraryResolver = libraryResolver;
        this.injector = injector;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        try {
            Class.forName("com.deepl.api.DeepLClient");
        } catch (ClassNotFoundException e) {
            loadLibraries();
        }

        injector.getInstance(DeeplIntegration.class).hook();
    }

    @Override
    public void onDisable() {
        injector.getInstance(DeeplIntegration.class).unhook();
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
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String translate(FPlayer sender, String source, String target, String text) {
        if (isModuleDisabledFor(sender)) return text;

        return injector.getInstance(DeeplIntegration.class).translate(source, target, text);
    }
}
