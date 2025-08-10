package net.flectone.pulse.module.message.afk.listener;

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
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class AfkPulseListener implements PulseListener {

    private final Permission.Message.Format formatPermission;
    private final AfkModule afkModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public AfkPulseListener(AfkModule afkModule,
                            FileResolver fileResolver,
                            PermissionChecker permissionChecker) {
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.afkModule = afkModule;
        this.permissionChecker = permissionChecker;
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.getPlayer();
        afkModule.remove("", fPlayer);
    }


    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageFormattingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();

        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (afkModule.isModuleDisabledFor(sender)) return;
        if (!(sender instanceof FPlayer fPlayer)) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.AFK_SUFFIX, (argumentQueue, context) -> {
            String afkSuffix = fPlayer.getSettingValue(FPlayer.Setting.AFK_SUFFIX);
            if (StringUtils.isEmpty(afkSuffix)) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(afkSuffix);
        });
    }
}
