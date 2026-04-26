package net.flectone.pulse.module.integration.discord;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.relocation.Relocation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final ReflectionResolver reflectionResolver;
    private final ModuleController moduleController;
    private final Injector injector;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        reflectionResolver.hasClassOrElse("discord4j.core.DiscordClient", this::loadLibraries);

        try {
            injector.getInstance(DiscordIntegration.class).hook();
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    @Override
    public void onDisable() {
        injector.getInstance(DiscordIntegration.class).unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_DISCORD;
    }

    @Override
    public Integration.Discord config() {
        return fileFacade.integration().discord();
    }

    @Override
    public Permission.Integration.Discord permission() {
        return fileFacade.permission().integration().discord();
    }

    private void loadLibraries(LibraryResolver libraryResolver) {
        libraryResolver.loadLibrary(Library.builder()
                .groupId("com{}discord4j")
                .artifactId("discord4j-core")
                .version(BuildConfig.DISCORD4J_VERSION)
                .repository(BuildConfig.MAVEN_REPOSITORY)
                .resolveTransitiveDependencies(true)
                .relocate(Relocation.builder()
                        .pattern("io{}netty")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".discord.netty")
                        .build()
                )
                .relocate(Relocation.builder()
                        .pattern("com{}fasterxml")
                        .relocatedPattern(BuildConfig.RELOCATED_PATTERN + ".discord.fasterxml")
                        .build()
                )
                .build()
        );
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> discordString) {
        if (moduleController.isDisabledFor(this, sender)) return;

        injector.getInstance(DiscordIntegration.class).sendMessage(sender, messageName, discordString);
    }
}
