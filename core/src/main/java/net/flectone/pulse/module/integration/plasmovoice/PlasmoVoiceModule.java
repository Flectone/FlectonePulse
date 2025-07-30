package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Singleton
public class PlasmoVoiceModule extends AbstractModule {

    private final Integration.Plasmovoice config;
    private final Permission.Integration.Plasmovoice permission;
    private final PlasmoVoiceIntegration plasmoVoiceIntegration;

    @Inject
    public PlasmoVoiceModule(FileResolver fileResolver,
                             PlasmoVoiceIntegration plasmoVoiceIntegration) {
        this.config = fileResolver.getIntegration().getPlasmovoice();
        this.permission = fileResolver.getPermission().getIntegration().getPlasmovoice();
        this.plasmoVoiceIntegration = plasmoVoiceIntegration;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        PlasmoVoiceServer.getAddonsLoader().load(plasmoVoiceIntegration);
        plasmoVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
        plasmoVoiceIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

}
