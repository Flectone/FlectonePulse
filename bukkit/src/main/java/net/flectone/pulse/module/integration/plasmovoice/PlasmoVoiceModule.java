package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Singleton
public class PlasmoVoiceModule extends AbstractModule {

    private final Integration.Plasmovoice config;
    private final Permission.Integration.Plasmovoice permission;

    private final PlasmoVoiceIntegration plasmoVoiceIntegration;

    @Inject
    public PlasmoVoiceModule(FileManager fileManager,
                             PlasmoVoiceIntegration plasmoVoiceIntegration) {
        this.plasmoVoiceIntegration = plasmoVoiceIntegration;

        config = fileManager.getIntegration().getPlasmovoice();
        permission = fileManager.getPermission().getIntegration().getPlasmovoice();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        PlasmoVoiceServer.getAddonsLoader().load(plasmoVoiceIntegration);
        plasmoVoiceIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return config.isEnable();
    }

}
