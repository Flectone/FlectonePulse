package net.flectone.pulse.module.message.format.moderation.flood;

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
import net.flectone.pulse.module.message.format.moderation.flood.listener.PulseFloodListener;
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
public class FloodModule implements ModuleLocalization<Localization.Message.Format.Moderation.Flood> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;
    private final ModerationService moderationService;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        listenerRegistry.register(PulseFloodListener.class);
    }

    @Override
    public Localization.Message.Format.Moderation.Flood localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().format().moderation().flood();
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_MODERATION_FLOOD;
    }

    @Override
    public Message.Format.Moderation.Flood config() {
        return fileFacade.message().format().moderation().flood();
    }

    @Override
    public Permission.Message.Format.Moderation.Flood permission() {
        return fileFacade.permission().message().format().moderation().flood();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (permissionChecker.check(sender, permission().bypass())) return messageContext;

        String message = messageContext.message();
        if (StringUtils.isEmpty(message)) return messageContext;

        message = replaceRepeatedWords(replaceRepeatedSymbols(message));

        if (messageContext.isFlag(MessageFlag.VIOLATION_PROCESSING) && config().violationLimit() > 0
                && messageContext.receiver().equals(messageContext.sender()) && !messageContext.message().trim().equals(message.trim())) {
            moderationService.addViolation(sender.uuid(), this, config());
        }

        return messageContext.withMessage(message);
    }

    public boolean isRestricted(UUID uuid) {
        return moduleController.isEnable(this) && moderationService.isViolationRestricted(uuid, this, config());
    }

    private String replaceRepeatedSymbols(String string) {
        if (StringUtils.isEmpty(string)) return string;

        StringBuilder stringBuilder = new StringBuilder();
        char prevChar = string.charAt(0);
        int count = 1;

        for (int i = 1; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            if (currentChar == prevChar) {
                count++;
                continue;
            }

            appendSymbol(stringBuilder, prevChar, count);
            prevChar = currentChar;
            count = 1;
        }

        appendSymbol(stringBuilder, prevChar, count);

        return stringBuilder.toString();
    }

    private void appendSymbol(StringBuilder stringBuilder, char symbol, int count) {
        int counts = count > config().maxRepeatedSymbols()
                ? config().trimToSingle() ? 1 : config().maxRepeatedSymbols()
                : count;

        stringBuilder.append(String.valueOf(symbol).repeat(counts));
    }

    private String replaceRepeatedWords(String text) {
        if (StringUtils.isEmpty(text)) return text;

        String[] words = text.split(" ");
        if (words.length == 0) return text;

        String prevWord = words[0];
        int count = 1;

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < words.length; i++) {
            String currentWord = words[i];
            if (currentWord.equalsIgnoreCase(prevWord)) {
                count++;
            } else {
                appendWord(stringBuilder, prevWord, count);
                prevWord = currentWord;
                count = 1;
            }
        }

        appendWord(stringBuilder, prevWord, count);

        return stringBuilder.toString().trim();
    }

    private void appendWord(StringBuilder stringBuilder, String word, int count) {
        int counts = count > config().maxRepeatedWords()
                ? config().trimToSingle() ? 1 : config().maxRepeatedWords()
                : count;

        while (counts > 0) {
            counts--;
            stringBuilder
                    .append(word)
                    .append(" ");
        }
    }
}
