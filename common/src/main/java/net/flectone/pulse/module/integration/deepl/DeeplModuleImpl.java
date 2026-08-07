package net.flectone.pulse.module.integration.deepl;

import com.alessiodp.libby.Library;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.util.LazyInstance;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DeeplModuleImpl implements DeeplModule {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final LazyInstance<DeeplIntegration> deeplIntegration;

    @Override
    public void onEnable() {
        reflectionResolver.hasClassOrElse("com.deepl.api.DeepLClient", this::loadLibraries);

        deeplIntegration.get().hook();
    }

    @Override
    public void onDisable() {
        deeplIntegration.get().unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_DEEPL;
    }

    @Override
    public Integration.Deepl config() {
        return fileFacade.integration().deepl();
    }

    @Override
    public Permission.Integration.Deepl permission() {
        return fileFacade.permission().integration().deepl();
    }

    @Override
    public String translate(FPlayer sender, String source, String target, String text) {
        if (moduleController.isDisabledFor(this, sender)) return text;

        return deeplIntegration.get().translate(source, target, text);
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
