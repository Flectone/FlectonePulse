package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class SimpleVoiceModule extends AbstractModule {

    private final FileResolver fileResolver;

    @Getter private static SimpleVoiceIntegration simpleVoiceIntegration;

    @Inject
    public SimpleVoiceModule(FileResolver fileResolver,
                             SimpleVoiceIntegration simpleVoiceIntegration) {
        this.fileResolver = fileResolver;

        SimpleVoiceModule.simpleVoiceIntegration = simpleVoiceIntegration;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        simpleVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        simpleVoiceIntegration.unhook();
    }

    @Override
    public Integration.Simplevoice config() {
        return fileResolver.getIntegration().getSimplevoice();
    }

    @Override
    public Permission.Integration.Simplevoice permission() {
        return fileResolver.getPermission().getIntegration().getSimplevoice();
    }
}
