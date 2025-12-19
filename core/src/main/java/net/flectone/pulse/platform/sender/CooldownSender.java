package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownSender {

    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final TimeFormatter timeFormatter;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    public boolean sendIfCooldown(FEntity entity, @Nullable Cooldown cooldown) {
        if (cooldown == null) return false;
        if (!cooldown.isEnable()) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return false;

        if (permissionChecker.check(fPlayer, cooldown.getPermissionBypass())) return false;
        if (!cooldown.isCooldown(fPlayer.getUuid())) return false;

        long timeLeft = cooldown.getTimeLeft(fPlayer);
        String cooldownMessage = timeFormatter.format(fPlayer, timeLeft, fileFacade.localization(entity).cooldown());
        Component component = messagePipeline.builder(fPlayer, cooldownMessage).build();

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
