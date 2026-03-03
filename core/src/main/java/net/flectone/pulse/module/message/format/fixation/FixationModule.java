package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.fixation.listener.FixationPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FixationModule implements AbstractModule {

    private final FileFacade fileFacade;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        listenerRegistry.register(FixationPulseListener.class);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_FORMAT_FIXATION;
    }

    @Override
    public Message.Format.Fixation config() {
        return fileFacade.message().format().fixation();
    }

    @Override
    public Permission.Message.Format.Fixation permission() {
        return fileFacade.permission().message().format().fixation();
    }

    public MessageContext format(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;

        String contextMessage = messageContext.message();
        if (contextMessage.isBlank()) return messageContext;

        if (config().endDot() && config().nonDotSymbols().stream().noneMatch(contextMessage::endsWith)) {
            contextMessage = contextMessage + ".";
        }

        if (config().firstLetterUppercase()) {
            contextMessage = Character.toUpperCase(contextMessage.charAt(0)) + contextMessage.substring(1);
        }

        return messageContext.withMessage(contextMessage);
    }
}
