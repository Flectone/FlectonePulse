package net.flectone.pulse.module.message.format.fixation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
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

    @Inject
    public FixationModule(FileResolver fileResolver,
                          MessageProcessRegistry messageProcessRegistry) {
        message = fileResolver.getMessage().getFormat().getFixation();
        permission = fileResolver.getPermission().getMessage().getFormat().getFixation();

        messageProcessRegistry.register(100, this);
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (!messageContext.isFixation()) return;
        if (!messageContext.isUserMessage()) return;

        String message = replace(messageContext.getSender(), messageContext.getMessage());
        messageContext.setMessage(message);
    }

    private String replace(@Nullable FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;
        if (message.isBlank()) return message;

        if (this.message.isEndDot()) {
            if (this.message.getNonDotSymbols().stream().noneMatch(message::endsWith)) {
                message = message + ".";
            }
        }

        if (this.message.isFirstLetterUppercase()) {
            message = Character.toUpperCase(message.charAt(0)) + message.substring(1);
        }

        return message;
    }
}
