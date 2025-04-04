package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.message.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class SimpleVoiceIntegration implements FIntegration, VoicechatPlugin {

    private final FPlayerService fPlayerService;
    private final ModerationUtil moderationUtil;
    private final MessageSender messageSender;
    private final ComponentUtil componentUtil;
    private final FLogger fLogger;

    @Inject
    public SimpleVoiceIntegration(FPlayerService fPlayerService,
                                  ModerationUtil moderationUtil,
                                  MessageSender messageSender,
                                  ComponentUtil componentUtil,
                                  FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.moderationUtil = moderationUtil;
        this.messageSender = messageSender;
        this.componentUtil = componentUtil;
        this.fLogger = fLogger;
    }

    @Override
    public String getPluginId() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacketEvent);
        registration.registerEvent(EntitySoundPacketEvent.class, this::onEntitySoundPacketEvent);
    }

    @Override
    public void hook() {
        fLogger.info("SimpleVoice hooked");
    }

    private void onEntitySoundPacketEvent(EntitySoundPacketEvent event) {
        if (event.getSenderConnection() == null) return;
        if (event.getReceiverConnection() == null) return;

        Player sender = event.getSenderConnection().getPlayer();
        FPlayer fSender = fPlayerService.getFPlayer(sender.getUuid());

        Player receiver = event.getReceiverConnection().getPlayer();
        FPlayer fReceiver = fPlayerService.getFPlayer(receiver.getUuid());

        if (!fReceiver.isIgnored(fSender)) return;

        event.cancel();
    }

    private void onMicrophonePacketEvent(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;

        Player player = event.getSenderConnection().getPlayer();

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUuid());
        if (!fPlayer.isMuted()) return;

        event.cancel();

        String message = moderationUtil.buildMuteMessage(fPlayer);
        messageSender.sendActionBar(fPlayer, componentUtil.builder(fPlayer, message).build());
    }
}
