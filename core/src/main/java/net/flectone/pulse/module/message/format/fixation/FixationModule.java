package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.fixation.listener.FixationPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FixationModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(FixationPulseListener.class);
    }

    @Override
    public Message.Format.Fixation config() {
        return fileFacade.message().format().fixation();
    }

    @Override
    public Permission.Message.Format.Fixation permission() {
        return fileFacade.permission().message().format().fixation();
    }

    public void format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        if (contextMessage.isBlank()) return;

        if (config().endDot() && config().nonDotSymbols().stream().noneMatch(contextMessage::endsWith)) {
            contextMessage = contextMessage + ".";
        }

        if (config().firstLetterUppercase()) {
            contextMessage = Character.toUpperCase(contextMessage.charAt(0)) + contextMessage.substring(1);
        }

        messageContext.setMessage(contextMessage);
    }
}
