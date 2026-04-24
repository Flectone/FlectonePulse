package net.flectone.pulse.module.integration.plasmovoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.formatter.ModerationMessageFormatter;
import net.flectone.pulse.platform.render.ActionBarRender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.MuteChecker;
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

import java.util.Optional;
import java.util.UUID;

@Singleton
@Addon(id = "flectonepulse", scope = AddonLoaderScope.SERVER, version = BuildConfig.PROJECT_VERSION, authors = BuildConfig.PROJECT_AUTHOR)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPlasmoVoiceIntegration implements FIntegration, AddonInitializer {

    private final FPlayerService fPlayerService;
    private final ModerationMessageFormatter moderationMessageFormatter;
    private final MuteChecker muteChecker;
    private final ActionBarRender actionBarRender;
    private final MessagePipeline messagePipeline;
    private final TaskScheduler taskScheduler;
    @Getter private final FLogger fLogger;

    private boolean enable;

    @Override
    public String getIntegrationName() {
        return "PlasmoVoice";
    }

    @Override
    public void hook() {
        enable = true;
        logHook();
    }

    @Override
    public void unhook() {
        enable = false;
        logUnhook();
    }

    @EventSubscribe
    public void onServerSourceCreatedEvent(ServerSourceCreatedEvent event) {
        if (!enable) return;

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
        if (!enable) return;
        if (!(event.getPacket() instanceof PlayerAudioPacket)) return;

        UUID senderUUID = event.getConnection().getPlayer().getInstance().getUuid();
        FPlayer fPlayer = fPlayerService.getFPlayer(senderUUID);

        MuteChecker.Status status = muteChecker.check(fPlayer);
        if (status == MuteChecker.Status.NONE) return;

        event.setCancelled(true);

        taskScheduler.runRegion(fPlayer, () -> {
            Optional<MessageContext> messageContext = moderationMessageFormatter.createMuteContext(fPlayer, status);
            if (messageContext.isEmpty()) return;

            actionBarRender.render(fPlayer, messagePipeline.build(messageContext.get()));
        });
    }

    @Override
    public void onAddonInitialize() {}
}
