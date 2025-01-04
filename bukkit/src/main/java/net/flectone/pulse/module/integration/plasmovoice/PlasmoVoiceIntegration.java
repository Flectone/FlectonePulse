package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.AddonLoaderScope;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.source.ServerAudioSource;
import su.plo.voice.api.server.event.audio.source.ServerSourceCreatedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.util.Optional;
import java.util.UUID;

@Singleton
@Addon(id = "flectonepulse", scope = AddonLoaderScope.SERVER, version = BuildConfig.PROJECT_VERSION, authors = BuildConfig.PROJECT_AUTHOR)
public class PlasmoVoiceIntegration implements FIntegration, AddonInitializer {

    private final FileManager fileManager;
    private final FPlayerManager fPlayerManager;
    private final PlatformSender platformSender;
    private final ComponentUtil componentUtil;
    private final TimeUtil timeUtil;
    private final FLogger fLogger;

    @Inject
    public PlasmoVoiceIntegration(FileManager fileManager,
                                  FPlayerManager fPlayerManager,
                                  PlatformSender platformSender,
                                  ComponentUtil componentUtil,
                                  TimeUtil timeUtil,
                                  FLogger fLogger) {
        this.fileManager = fileManager;
        this.fPlayerManager = fPlayerManager;
        this.platformSender = platformSender;
        this.componentUtil = componentUtil;
        this.timeUtil = timeUtil;
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
        FPlayer fSender = fPlayerManager.get(senderUUID);

        source.addFilter(voicePlayer -> {
            UUID receiverUUID = voicePlayer.getInstance().getUuid();
            FPlayer fReceiver = fPlayerManager.get(receiverUUID);

            return !fReceiver.isIgnored(fSender);
        });
    }

    @EventSubscribe
    public void onPlayerSpeakEvent(UdpPacketReceivedEvent event) {
        if (!(event.getPacket() instanceof PlayerAudioPacket)) return;

        UUID senderUUID = event.getConnection().getPlayer().getInstance().getUuid();

        FPlayer fPlayer = fPlayerManager.get(senderUUID);

        Optional<Moderation> optionalModeration = fPlayer.getMutes()
                .stream()
                .filter(moderation -> !moderation.isExpired() && moderation.isValid())
                .findFirst();

        if (optionalModeration.isEmpty()) return;

        event.setCancelled(true);

        Localization.Command.Mute localization = fileManager.getLocalization(fPlayer).getCommand().getMute();

        Moderation mute = optionalModeration.get();

        String formatPlayer = localization.getPlayer()
                .replace("<message>", localization.getReasons().getConstant(mute.getReason()));

        platformSender.sendActionBar(fPlayer, componentUtil.builder(fPlayer, timeUtil.format(fPlayer, mute.getRemainingTime(), formatPlayer)).build());
    }

    @Override
    public void onAddonInitialize() {}
}
