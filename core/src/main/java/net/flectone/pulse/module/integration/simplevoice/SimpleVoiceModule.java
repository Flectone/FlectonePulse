package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SimpleVoiceModule extends AbstractModule {

    @Getter private static SimpleVoiceIntegration SIMPLE_VOICE_INTEGRATION;

    private final FileResolver fileResolver;

    @Inject
    public SimpleVoiceModule(FileResolver fileResolver,
                             SimpleVoiceIntegration simpleVoiceIntegration) {
        this.fileResolver = fileResolver;

        SimpleVoiceModule.SIMPLE_VOICE_INTEGRATION = simpleVoiceIntegration;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        SIMPLE_VOICE_INTEGRATION.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        SIMPLE_VOICE_INTEGRATION.unhook();
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
