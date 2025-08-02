package net.flectone.pulse.module.command.stream.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

@Singleton
public class StreamPulseListener implements PulseListener {

    private final StreamModule streamModule;
    private final Permission.Message.Format formatPermission;
    private final PermissionChecker permissionChecker;

    @Inject
    public StreamPulseListener(StreamModule streamModule,
                               FileResolver fileResolver,
                               PermissionChecker permissionChecker) {
        this.streamModule = streamModule;
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.getPlayer();
        streamModule.setStreamPrefix(fPlayer, fPlayer.isSetting(FPlayer.Setting.STREAM));
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();

        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (!(sender instanceof FPlayer fPlayer)) return;
        if (streamModule.checkModulePredicates(fPlayer)) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.STREAM_PREFIX, (argumentQueue, context) -> {
            String streamPrefix = fPlayer.getSettingValue(FPlayer.Setting.STREAM_PREFIX);
            if (streamPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(streamPrefix);
        });
    }

}
