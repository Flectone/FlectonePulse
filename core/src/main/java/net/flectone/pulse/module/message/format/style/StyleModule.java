package net.flectone.pulse.module.message.format.style;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;

@Singleton
public class StyleModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Style message;
    private final Permission.Message.Format.Style permission;

    @Inject
    public StyleModule(FileResolver fileResolver,
                       MessageProcessRegistry messageProcessRegistry) {

        message = fileResolver.getMessage().getFormat().getStyle();
        permission = fileResolver.getPermission().getMessage().getFormat().getStyle();

        messageProcessRegistry.register(150, this);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (messageContext.isUserMessage()) return;
        if (!(messageContext.getSender() instanceof FPlayer sender)) return;

        String style = sender.getSettingValue(FPlayer.Setting.STYLE);

        // only possible solution
        String message = messageContext.getMessage()
                .replace("<style>", style == null ? "" : style)
                .replace("</style>", "");

        messageContext.setMessage(message);
    }
}
