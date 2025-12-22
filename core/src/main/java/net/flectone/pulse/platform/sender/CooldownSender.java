package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.MessageSendEvent;
import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.util.checker.CooldownChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownSender {

    private final PermissionChecker permissionChecker;
    private final CooldownChecker cooldownChecker;
    private final MessagePipeline messagePipeline;
    private final TimeFormatter timeFormatter;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    public boolean sendIfCooldown(FEntity entity, Optional<Pair<Cooldown, PermissionSetting>> optionalCooldown) {
        return optionalCooldown
                .filter(pair -> sendIfCooldown(entity, pair))
                .isPresent();
    }

    public boolean sendIfCooldown(FEntity entity, Pair<Cooldown, PermissionSetting> cooldownPermission) {
        Cooldown cooldown = cooldownPermission.first();
        if (cooldown == null || !cooldown.enable()) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return false;

        if (permissionChecker.check(fPlayer, cooldownPermission.second())) return false;
        if (!cooldownChecker.check(fPlayer.getUuid(), cooldown)) return false;

        long timeLeft = cooldownChecker.getTimeLeft(fPlayer.getUuid(), cooldown);
        String cooldownMessage = timeFormatter.format(fPlayer, timeLeft, fileFacade.localization(entity).cooldown());
        Component component = messagePipeline.builder(fPlayer, cooldownMessage).build();

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
