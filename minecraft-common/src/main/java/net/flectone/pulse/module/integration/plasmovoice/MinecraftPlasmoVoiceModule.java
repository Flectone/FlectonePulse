package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import net.flectone.pulse.module.ModuleSimple;
import su.plo.voice.api.server.PlasmoVoiceServer;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPlasmoVoiceModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final MinecraftPlasmoVoiceIntegration plasmoVoiceIntegration;
    private final FLogger fLogger;

    @Override
    public void onEnable() {
        try {
            PlasmoVoiceServer.getAddonsLoader().load(plasmoVoiceIntegration);
            plasmoVoiceIntegration.hook();
        } catch (Exception e) {
            plasmoVoiceIntegration.lohHookFailed(e);
        }
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
