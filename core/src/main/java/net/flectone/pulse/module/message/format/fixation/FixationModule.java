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

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FixationModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        listenerRegistry.register(FixationPulseListener.class);
    }

    @Override
    public Message.Format.Fixation config() {
        return fileResolver.getMessage().getFormat().getFixation();
    }

    @Override
    public Permission.Message.Format.Fixation permission() {
        return fileResolver.getPermission().getMessage().getFormat().getFixation();
    }

    public void format(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;

        String contextMessage = messageContext.getMessage();
        if (contextMessage.isBlank()) return;

        if (config().isEndDot() && config().getNonDotSymbols().stream().noneMatch(contextMessage::endsWith)) {
            contextMessage = contextMessage + ".";
        }

        if (config().isFirstLetterUppercase()) {
            contextMessage = Character.toUpperCase(contextMessage.charAt(0)) + contextMessage.substring(1);
        }

        messageContext.setMessage(contextMessage);
    }
}
