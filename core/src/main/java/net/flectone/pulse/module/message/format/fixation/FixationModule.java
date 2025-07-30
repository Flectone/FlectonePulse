package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import org.jetbrains.annotations.Nullable;

@Singleton
public class FixationModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Fixation message;
    private final Permission.Message.Format.Fixation permission;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public FixationModule(FileResolver fileResolver,
                          MessageProcessRegistry messageProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getFixation();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getFixation();
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(100, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFlag(MessageFlag.FIXATION)) return;
        if (!messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        String processedMessage = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(processedMessage);
    }

    private String replace(@Nullable FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (message.isBlank()) return message;

        if (this.message.isEndDot() && this.message.getNonDotSymbols().stream().noneMatch(message::endsWith)) {
            message = message + ".";
        }

        if (this.message.isFirstLetterUppercase()) {
            message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
        }

        return message;
    }
}
