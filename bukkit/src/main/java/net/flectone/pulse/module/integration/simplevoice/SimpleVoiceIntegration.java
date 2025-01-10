package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.platform.MessageSender;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.TimeUtil;

import java.util.Optional;

@Singleton
public class SimpleVoiceIntegration implements FIntegration, VoicechatPlugin {

    private final FileManager fileManager;
    private final FPlayerManager fPlayerManager;
    private final MessageSender messageSender;
    private final ComponentUtil componentUtil;
    private final TimeUtil timeUtil;
    private final FLogger fLogger;

    @Inject
    public SimpleVoiceIntegration(FileManager fileManager,
                                  FPlayerManager fPlayerManager,
                                  MessageSender messageSender,
                                  ComponentUtil componentUtil,
                                  TimeUtil timeUtil,
                                  FLogger fLogger) {
        this.fileManager = fileManager;
        this.fPlayerManager = fPlayerManager;
        this.messageSender = messageSender;
        this.componentUtil = componentUtil;
        this.timeUtil = timeUtil;
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
        FPlayer fSender = fPlayerManager.get(sender.getUuid());

        Player receiver = event.getReceiverConnection().getPlayer();
        FPlayer fReceiver = fPlayerManager.get(receiver.getUuid());

        if (!fReceiver.isIgnored(fSender)) return;

        event.cancel();
    }

    private void onMicrophonePacketEvent(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;

        Player player = event.getSenderConnection().getPlayer();

        FPlayer fPlayer = fPlayerManager.get(player.getUuid());

        Optional<Moderation> optionalModeration = fPlayer.getMutes()
                .stream()
                .filter(moderation -> !moderation.isExpired() && moderation.isValid())
                .findFirst();

        if (optionalModeration.isEmpty()) return;

        event.cancel();

        Moderation mute = optionalModeration.get();

        Localization.Command.Mute localization = fileManager.getLocalization(fPlayer).getCommand().getMute();

        String formatPlayer = localization.getPlayer()
                .replace("<message>", localization.getReasons().getConstant(mute.getReason()));

        messageSender.sendActionBar(fPlayer, componentUtil.builder(fPlayer, timeUtil.format(fPlayer, mute.getRemainingTime(), formatPlayer)).build());
    }
}
