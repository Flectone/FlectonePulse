package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Singleton
public class PlasmoVoiceModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final PlasmoVoiceIntegration plasmoVoiceIntegration;

    @Inject
    public PlasmoVoiceModule(FileResolver fileResolver,
                             PlasmoVoiceIntegration plasmoVoiceIntegration) {
        this.fileResolver = fileResolver;
        this.plasmoVoiceIntegration = plasmoVoiceIntegration;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        PlasmoVoiceServer.getAddonsLoader().load(plasmoVoiceIntegration);
        plasmoVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
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
