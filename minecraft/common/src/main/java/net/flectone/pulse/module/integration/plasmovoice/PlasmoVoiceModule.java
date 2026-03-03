package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlasmoVoiceModule implements AbstractModule {

    private final FileFacade fileFacade;
    private final PlasmoVoiceIntegration plasmoVoiceIntegration;

    @Override
    public void onEnable() {
        PlasmoVoiceServer.getAddonsLoader().load(plasmoVoiceIntegration);
        plasmoVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
        plasmoVoiceIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_PLASMOVOICE;
    }

    @Override
    public Integration.Plasmovoice config() {
        return fileFacade.integration().plasmovoice();
    }

    @Override
    public Permission.Integration.Plasmovoice permission() {
        return fileFacade.permission().integration().plasmovoice();
    }

}
