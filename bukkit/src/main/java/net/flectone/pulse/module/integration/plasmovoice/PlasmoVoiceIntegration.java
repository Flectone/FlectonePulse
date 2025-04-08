package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.checker.MuteChecker;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.sender.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.formatter.ModerationMessageFormatter;
import net.flectone.pulse.util.logging.FLogger;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.util.UUID;

@Singleton
@Addon(id = "flectonepulse", scope = AddonLoaderScope.SERVER, version = BuildConfig.PROJECT_VERSION, authors = BuildConfig.PROJECT_AUTHOR)
public class PlasmoVoiceIntegration implements FIntegration, AddonInitializer {

    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MuteChecker muteChecker;
    private final MessageSender messageSender;
    private final MessagePipeline messagePipeline;
    private final FLogger fLogger;

    @Inject
    public PlasmoVoiceIntegration(FPlayerService fPlayerService,
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
    public void hook() {
        fLogger.info("PlasmoVoice hooked");
    }

    @EventSubscribe
    public void onServerSourceCreatedEvent(ServerSourceCreatedEvent event) {
        ServerAudioSource<?> source = event.getSource();
        if (!(source.getSourceInfo() instanceof PlayerSourceInfo sourceInfo)) return;

        UUID senderUUID = sourceInfo.getPlayerInfo().getPlayerId();
        FPlayer fSender = fPlayerService.getFPlayer(senderUUID);

        source.addFilter(voicePlayer -> {
            UUID receiverUUID = voicePlayer.getInstance().getUuid();
            FPlayer fReceiver = fPlayerService.getFPlayer(receiverUUID);

            return !fReceiver.isIgnored(fSender);
        });
    }

    @EventSubscribe
    public void onPlayerSpeakEvent(UdpPacketReceivedEvent event) {
        if (!(event.getPacket() instanceof PlayerAudioPacket)) return;

        UUID senderUUID = event.getConnection().getPlayer().getInstance().getUuid();
        FPlayer fPlayer = fPlayerService.getFPlayer(senderUUID);

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return;

        event.setCancelled(true);

        String message = moderationMessageFormatter.buildMuteMessage(fPlayer, status);
        messageSender.sendActionBar(fPlayer, messagePipeline.builder(fPlayer, message).build());
    }

    @Override
    public void onAddonInitialize() {}
}
