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
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.formatter.MessageFormatter;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class SimpleVoiceIntegration implements FIntegration, VoicechatPlugin {

    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MessageSender messageSender;
    private final MessageFormatter messageFormatter;
    private final FLogger fLogger;

    @Inject
    public SimpleVoiceIntegration(FPlayerService fPlayerService,
                                  ModerationMessageFormatter moderationMessageFormatter,
                                  MessageSender messageSender,
                                  MessageFormatter messageFormatter,
                                  FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.messageSender = messageSender;
        this.messageFormatter = messageFormatter;
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

        String message = moderationMessageFormatter.buildMuteMessage(fPlayer);
        messageSender.sendActionBar(fPlayer, messageFormatter.builder(fPlayer, message).build());
    }
}
