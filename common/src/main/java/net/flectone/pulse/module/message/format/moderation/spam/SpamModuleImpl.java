package net.flectone.pulse.module.message.format.moderation.spam;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.moderation.spam.listener.PulseSpamListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpamModuleImpl implements SpamModule {

    private final Map<UUID, PlayerMessage> playerMessages = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final ModerationService moderationService;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseSpamListener.class);
    }

    @Override
    public void onDisable() {
        playerMessages.clear();
    }

    @Override
    public Localization.Message.Format.Moderation.Spam localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().moderation().spam();
    }

    @Override
    public Set<PermissionSetting> permissions() {
        Set<PermissionSetting> permissions = new LinkedHashSet<>(SpamModule.super.permissions());
        permissions.add(permission().bypass());
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_MODERATION_SPAM;
    }

    @Override
    public Message.Format.Moderation.Spam config() {
        return fileFacade.message().format().moderation().spam();
    }

    @Override
    public Permission.Message.Format.Moderation.Spam permission() {
        return fileFacade.permission().message().format().moderation().spam();
    }

    @Override
    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (permissionChecker.check(sender, permission().bypass())) return messageContext;

        String message = messageContext.message().trim();
        if (StringUtils.isEmpty(message)) return messageContext;

        if (!messageContext.isFlag(MessageFlag.VIOLATION_PROCESSING) || config().violationLimit() <= 0
                || !messageContext.receiver().equals(messageContext.sender())) return messageContext;

        long currentTime = System.currentTimeMillis();
        PlayerMessage previousMessage = playerMessages.put(sender.uuid(), new PlayerMessage(message, currentTime));

        if (previousMessage != null && currentTime - previousMessage.timestamp() < config().interval() * TimeFormatter.MULTIPLIER
                && (!config().sameMessage() || previousMessage.message().equalsIgnoreCase(message))) {
            moderationService.addViolation(sender.uuid(), this, config());
        }

        return messageContext.addFlag(MessageFlag.USE_CACHE, false);
    }

    @Override
    public boolean isRestricted(UUID uuid) {
        return moduleController.isEnable(this) && moderationService.isViolationRestricted(uuid, this, config());
    }

    @Override
    public void removeMessage(FPlayer fPlayer) {
        playerMessages.remove(fPlayer.uuid());
    }

    private record PlayerMessage(
            String message,
            long timestamp
    ) {
    }

}
