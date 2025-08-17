package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.fixation.listener.FixationPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageFlag;

@Singleton
public class FixationModule extends AbstractModule {

    private final Message.Format.Fixation message;
    private final Permission.Message.Format.Fixation permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FixationModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getFixation();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getFixation();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(FixationPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        if (contextMessage.isBlank()) return;

        if (message.isEndDot() && message.getNonDotSymbols().stream().noneMatch(contextMessage::endsWith)) {
            contextMessage = contextMessage + ".";
        }

        if (message.isFirstLetterUppercase()) {
            contextMessage = Character.toUpperCase(contextMessage.charAt(0)) + contextMessage.substring(1);
        }

        messageContext.setMessage(contextMessage);
    }
}
