package net.flectone.pulse.module.message.format.moderation.caps;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.caps.listener.CapsPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageFlag;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CapsModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(CapsPulseListener.class);
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().bypass());
    }

    @Override
    public Message.Format.Moderation.Caps config() {
        return fileFacade.message().format().moderation().caps();
    }

    @Override
    public Permission.Message.Format.Moderation.Caps permission() {
        return fileFacade.permission().message().format().moderation().caps();
    }

    public void format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (permissionChecker.check(sender, permission().bypass())) return;

        String contextMessage = messageContext.getMessage();
        if (StringUtils.isEmpty(contextMessage)) return;

        String formattedMessage = needApplyAntiCaps(contextMessage) ? contextMessage.toLowerCase() : contextMessage;
        messageContext.setMessage(formattedMessage);
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
