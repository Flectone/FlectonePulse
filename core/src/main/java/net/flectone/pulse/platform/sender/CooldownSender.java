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
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.checker.CooldownChecker;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.type.tuple.Pair;

import java.util.Optional;

/**
 * Sends cooldown messages to players and checks cooldown bypass permissions.
 *
 * <p><b>Usage example:</b>
 * <pre>{@code
 * CooldownSender cooldownSender = flectonePulse.get(CooldownSender.class);
 *
 * Cooldown cooldown = new Cooldown(5000, true); // 5 second cooldown
 * PermissionSetting permissionByPass = new PermissionSetting("myplugin.bypass", false);
 *
 * if (cooldownSender.sendIfCooldown(player, Pair.of(cooldown, permissionByPass))) {
 *     // Player is on cooldown, action should be blocked
 * }
 * }</pre>
 *
 * @since 1.6.0
 * @author TheFaser
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownSender {

    private final PermissionChecker permissionChecker;
    private final CooldownChecker cooldownChecker;
    private final MessagePipeline messagePipeline;
    private final TimeFormatter timeFormatter;
    private final EventDispatcher eventDispatcher;
    private final FileFacade fileFacade;

    /**
     * Checks if an entity is on cooldown and sends a cooldown message if applicable.
     * Only sends messages to players, not other entities.
     *
     * @param entity the entity to check
     * @param optionalCooldown optional pair of cooldown and permission settings
     * @return true if cooldown message was sent, false otherwise
     */
    public boolean sendIfCooldown(FEntity entity, Optional<Pair<Cooldown, PermissionSetting>> optionalCooldown) {
        return optionalCooldown
                .filter(pair -> sendIfCooldown(entity, pair))
                .isPresent();
    }

    /**
     * Checks if a player is on cooldown and sends a formatted cooldown message.
     *
     * @param entity the entity to check
     * @param cooldownPermission pair of cooldown settings and bypass permission
     * @return true if cooldown message was sent, false otherwise
     */
    public boolean sendIfCooldown(FEntity entity, Pair<Cooldown, PermissionSetting> cooldownPermission) {
        Cooldown cooldown = cooldownPermission.first();
        if (cooldown == null || !cooldown.enable()) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return false;

        if (permissionChecker.check(fPlayer, cooldownPermission.second())) return false;
        if (!cooldownChecker.check(fPlayer.getUuid(), cooldown)) return false;

        long timeLeft = cooldownChecker.getTimeLeft(fPlayer.getUuid(), cooldown);
        String cooldownMessage = timeFormatter.format(fPlayer, timeLeft, fileFacade.localization(entity).cooldown());
        MessageContext cooldownContext = messagePipeline.createContext(fPlayer, cooldownMessage);
        Component component = messagePipeline.build(cooldownContext);

        eventDispatcher.dispatch(new MessageSendEvent(MessageType.ERROR, fPlayer, component));

        return true;
    }

}
