package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.message.MessageSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ModerationUtil;
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
    private final ModerationUtil moderationUtil;
    private final MessageSender messageSender;
    private final ComponentUtil componentUtil;
    private final FLogger fLogger;

    @Inject
    public PlasmoVoiceIntegration(FPlayerService fPlayerService,
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

        if (!fPlayer.isMuted()) return;

        event.setCancelled(true);

        String message = moderationUtil.buildMuteMessage(fPlayer);
        messageSender.sendActionBar(fPlayer, componentUtil.builder(fPlayer, message).build());
    }

    @Override
    public void onAddonInitialize() {}
}
