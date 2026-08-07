package net.flectone.pulse.simplevoice;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.NeoForgeFlectonePulseLoader;
import net.flectone.pulse.constant.HookType;

@ForgeVoicechatPlugin
public class NeoForgeSimpleVoiceIntegrationLoader implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(EntitySoundPacketEvent.class, event -> NeoForgeFlectonePulseLoader.getLoaderBootstrap().hook(HookType.SIMPLEVOICE_ENTITY_SOUND_PACKET, event));
        registration.registerEvent(MicrophonePacketEvent.class, event -> NeoForgeFlectonePulseLoader.getLoaderBootstrap().hook(HookType.SIMPLEVOICE_MICROPHONE_PACKET, event));
    }
    
}
