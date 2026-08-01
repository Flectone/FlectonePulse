package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
public class MinecraftSimpleVoiceModule implements ModuleSimple {

    private static MinecraftSimpleVoiceIntegration simpleVoiceIntegration;
    private final FileFacade fileFacade;

    @Inject
    public MinecraftSimpleVoiceModule(FileFacade fileFacade,
                                      MinecraftSimpleVoiceIntegration simpleVoiceIntegration) {
        this.fileFacade = fileFacade;

        MinecraftSimpleVoiceModule.simpleVoiceIntegration = simpleVoiceIntegration;
    }

    @Override
    public void onEnable() {
        if (simpleVoiceIntegration != null) {
            simpleVoiceIntegration.hook();
        }
    }

    @Override
    public void onDisable() {
        if (simpleVoiceIntegration != null) {
            simpleVoiceIntegration.unhook();
        }
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

    public static void onEntitySoundPacketEvent(Object event) {
        if (simpleVoiceIntegration != null) {
            simpleVoiceIntegration.onEntitySoundPacketEvent(event);
        }
    }

    public static void onMicrophonePacketEvent(Object event) {
        if (simpleVoiceIntegration != null) {
            simpleVoiceIntegration.onMicrophonePacketEvent(event);
        }
    }

}
