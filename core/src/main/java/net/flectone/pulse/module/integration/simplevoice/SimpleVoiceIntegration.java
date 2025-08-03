package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.sender.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.MuteChecker;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class SimpleVoiceIntegration implements FIntegration, VoicechatPlugin {

    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MuteChecker muteChecker;
    private final MessageSender messageSender;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    private boolean enable;

    // only for fabric support
    public SimpleVoiceIntegration() {
        fPlayerService = null;
        moderationMessageFormatter = null;
        muteChecker = null;
        messageSender = null;
        messagePipeline = null;
        fLogger = null;
    }

    @Inject
    public SimpleVoiceIntegration(FPlayerService fPlayerService,
                                  ModerationMessageFormatter moderationMessageFormatter,
                                  MuteChecker muteChecker,
                                  MessageSender messageSender,
                                  MessagePipeline messagePipeline,
                                  FLogger fLogger) {
        this.fPlayerService = fPlayerService;
        this.moderationMessageFormatter = moderationMessageFormatter;
        this.muteChecker = muteChecker;
        this.messageSender = messageSender;
        this.messagePipeline = messagePipeline;
        this.fLogger = fLogger;
    }

    @Override
    public String getPluginId() {
        return BuildConfig.PROJECT_NAME;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        SimpleVoiceIntegration simpleVoiceIntegration = SimpleVoiceModule.getSimpleVoiceIntegration();
        if (simpleVoiceIntegration == null) return;

        registration.registerEvent(MicrophonePacketEvent.class, simpleVoiceIntegration::onMicrophonePacketEvent);
        registration.registerEvent(EntitySoundPacketEvent.class, simpleVoiceIntegration::onEntitySoundPacketEvent);
    }

    @Override
    public void hook() {
        enable = true;
        fLogger.info("✔ SimpleVoice hooked");
    }

    @Override
    public void unhook() {
        enable = false;
        fLogger.info("✖ SimpleVoice unhooked");
    }

    public void onEntitySoundPacketEvent(EntitySoundPacketEvent event) {
        if (!enable) return;
        if (event.getSenderConnection() == null) return;
        if (event.getReceiverConnection() == null) return;

        Player sender = event.getSenderConnection().getPlayer();
        FPlayer fSender = fPlayerService.getFPlayer(sender.getUuid());

        Player receiver = event.getReceiverConnection().getPlayer();
        FPlayer fReceiver = fPlayerService.getFPlayer(receiver.getUuid());

        if (!fReceiver.isIgnored(fSender)) return;

        event.cancel();
    }

    public void onMicrophonePacketEvent(MicrophonePacketEvent event) {
        if (!enable) return;
        if (event.isCancelled()) return;
        if (event.getSenderConnection() == null) return;

        Player player = event.getSenderConnection().getPlayer();

        FPlayer fPlayer = fPlayerService.getFPlayer(player.getUuid());
        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return;

        event.cancel();

        String message = moderationMessageFormatter.buildMuteMessage(fPlayer, status);
        messageSender.sendActionBar(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }
}
