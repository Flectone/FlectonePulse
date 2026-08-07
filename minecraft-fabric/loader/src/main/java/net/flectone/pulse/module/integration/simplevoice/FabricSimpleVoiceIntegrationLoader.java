package net.flectone.pulse.module.integration.simplevoice;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.FabricFlectonePulseLoader;
import net.flectone.pulse.LoaderBootstrap;
import net.flectone.pulse.util.constant.HookType;

public class FabricSimpleVoiceIntegrationLoader implements VoicechatPlugin {

    private LoaderBootstrap loaderBootstrap;

    @Override
    public String getPluginId() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(EntitySoundPacketEvent.class, event -> FabricFlectonePulseLoader.getLoaderBootstrap().hook(HookType.SIMPLEVOICE_ENTITY_SOUND_PACKET, event));
        registration.registerEvent(MicrophonePacketEvent.class, event -> FabricFlectonePulseLoader.getLoaderBootstrap().hook(HookType.SIMPLEVOICE_MICROPHONE_PACKET, event));
    }

}
