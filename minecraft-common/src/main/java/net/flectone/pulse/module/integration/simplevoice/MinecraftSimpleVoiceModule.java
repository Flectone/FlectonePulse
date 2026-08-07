package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.ModuleSimple;

@Singleton
public class MinecraftSimpleVoiceModule implements ModuleSimple {

    private final MinecraftSimpleVoiceIntegration simpleVoiceIntegration;
    private final FileFacade fileFacade;

    @Inject
    public MinecraftSimpleVoiceModule(FileFacade fileFacade,
                                      MinecraftSimpleVoiceIntegration simpleVoiceIntegration) {
        this.fileFacade = fileFacade;

        this.simpleVoiceIntegration = simpleVoiceIntegration;
    }

    @Override
    public void onEnable() {
        simpleVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
        simpleVoiceIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_SIMPLEVOICE;
    }

    @Override
    public Integration.Simplevoice config() {
        return fileFacade.integration().simplevoice();
    }

    @Override
    public Permission.Integration.Simplevoice permission() {
        return fileFacade.permission().integration().simplevoice();
    }

    public void onEntitySoundPacketEvent(Object event) {
        simpleVoiceIntegration.onEntitySoundPacketEvent(event);
    }

    public void onMicrophonePacketEvent(Object event) {
        simpleVoiceIntegration.onMicrophonePacketEvent(event);
    }

}
