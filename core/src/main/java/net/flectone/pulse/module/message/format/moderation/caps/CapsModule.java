package net.flectone.pulse.module.message.format.moderation.caps;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.format.moderation.caps.listener.PulseCapsListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CapsModule implements ModuleLocalization<Localization.Message.Format.Moderation.Caps> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final ModerationService moderationService;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseCapsListener.class);
    }

    @Override
    public Localization.Message.Format.Moderation.Caps localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().moderation().caps();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_MODERATION_CAPS;
    }

    @Override
    public Message.Format.Moderation.Caps config() {
        return fileFacade.message().format().moderation().caps();
    }

    @Override
    public Permission.Message.Format.Moderation.Caps permission() {
        return fileFacade.permission().message().format().moderation().caps();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (permissionChecker.check(sender, permission().bypass())) return messageContext;

        String message = messageContext.message();
        if (StringUtils.isEmpty(message)) return messageContext;

        boolean needApplyAntiCaps = needApplyAntiCaps(message);
        if (needApplyAntiCaps && messageContext.isFlag(MessageFlag.VIOLATION_PROCESSING)
                && config().violationLimit() > 0 && messageContext.receiver().equals(messageContext.sender())) {
            moderationService.addViolation(sender.uuid(), this, config());
        }

        String formattedMessage = needApplyAntiCaps ? message.toLowerCase() : message;
        return messageContext.withMessage(formattedMessage);
    }

    public boolean isRestricted(UUID uuid) {
        return moduleController.isEnable(this) && moderationService.isViolationRestricted(uuid, this, config());
    }

    private boolean needApplyAntiCaps(String string) {
        int uppercaseCount = 0;
        int totalLetters = 0;

        for (char symbol : string.toCharArray()) {
            if (Character.isLetter(symbol)) {
                totalLetters++;
                if (Character.isUpperCase(symbol)) {
                    uppercaseCount++;
                }
            }
        }

        return totalLetters > 0 && ((double) uppercaseCount / totalLetters) > config().trigger();
    }

}
