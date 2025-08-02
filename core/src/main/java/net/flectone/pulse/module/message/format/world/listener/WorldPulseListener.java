package net.flectone.pulse.module.message.format.world.listener;

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
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;

@Singleton
public class WorldPulseListener implements PulseListener {

    private final Permission.Message.Format formatPermission;
    private final WorldModule worldModule;
    private final PermissionChecker permissionChecker;

    @Inject
    public WorldPulseListener(FileResolver fileResolver,
                              WorldModule worldModule,
                              PermissionChecker permissionChecker) {
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.worldModule = worldModule;
        this.permissionChecker = permissionChecker;
    }


    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        FPlayer fPlayer = event.getPlayer();
        worldModule.update(fPlayer);
    }

    @Pulse(priority = Event.Priority.HIGH)
    public void onMessageProcessingEvent(MessageFormattingEvent event) {
        MessageContext messageContext = event.getContext();
        FEntity sender = messageContext.getSender();

        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;
        if (worldModule.checkModulePredicates(sender)) return;
        if (!(sender instanceof FPlayer fPlayer)) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.WORLD_PREFIX, (argumentQueue, context) -> {
            String worldPrefix = fPlayer.getSettingValue(FPlayer.Setting.WORLD_PREFIX);
            if (worldPrefix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(worldPrefix);
        });
    }
}
