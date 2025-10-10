package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlasmoVoiceModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final PlasmoVoiceIntegration plasmoVoiceIntegration;

    @Override
    public void onEnable() {
        super.onEnable();

        PlasmoVoiceServer.getAddonsLoader().load(plasmoVoiceIntegration);
        plasmoVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        plasmoVoiceIntegration.unhook();
    }

    @Override
    public Integration.Plasmovoice config() {
        return fileResolver.getIntegration().getPlasmovoice();
    }

    @Override
    public Permission.Integration.Plasmovoice permission() {
        return fileResolver.getPermission().getIntegration().getPlasmovoice();
    }

}
