package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.CooldownChecker;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Cooldown;
import net.flectone.pulse.model.value.Pair;
import net.flectone.pulse.persistence.repository.CooldownRepository;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.service.SocialService;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownSenderImpl implements CooldownSender {

    private final PermissionChecker permissionChecker;
    private final CooldownChecker cooldownChecker;
    private final CooldownRepository cooldownRepository;
    private final TimeFormatter timeFormatter;
    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final MessageDispatcher messageDispatcher;

    @Override
    public boolean sendIfCooldown(FEntity entity, Optional<Pair<Cooldown, PermissionSetting>> optionalCooldown, String cooldownOwner) {
        return optionalCooldown
                .filter(pair -> sendIfCooldown(entity, pair, cooldownOwner))
                .isPresent();
    }

    @Override
    public boolean sendIfCooldown(FEntity entity, Pair<Cooldown, PermissionSetting> cooldownPermission, String cooldownOwner) {
        Cooldown cooldown = cooldownPermission.getLeft();
        if (cooldown == null || !cooldown.enable()) return false;

        // skip message for entities
        if (!(entity instanceof FPlayer fPlayer)) return false;

        if (permissionChecker.check(fPlayer, cooldownPermission.getRight())) return false;
        if (!cooldownChecker.check(fPlayer.uuid(), cooldown, cooldownOwner)) return false;

        long timeLeft = cooldownRepository.getTimeLeft(fPlayer.uuid(), cooldown, cooldownOwner);
        String cooldownMessage = timeFormatter.format(fPlayer, timeLeft, fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).cooldown());

        messageDispatcher.dispatch(ModuleName.ERROR, EventMetadata.builder()
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(cooldownMessage)
                        .build()
                )
                .build()
        );

        return true;
    }

}
