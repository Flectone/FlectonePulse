package net.flectone.pulse.module.message.format.style;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class StyleModule extends AbstractModule implements MessageProcessor {

    private final Message.Format.Style message;
    private final Permission.Message.Format.Style permission;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public StyleModule(FileResolver fileResolver,
                       MessageProcessRegistry messageProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getStyle();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getStyle();
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        messageProcessRegistry.register(150, this);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;
        if (!(messageContext.getSender() instanceof FPlayer sender)) return;

        String style = sender.getSettingValue(FPlayer.Setting.STYLE);

        // bad practice, but only it works
        String processedMessage = messageContext.getMessage()
                .replace("<style>", style == null ? "" : style)
                .replace("</style>", "");

        messageContext.setMessage(processedMessage);
    }
}
