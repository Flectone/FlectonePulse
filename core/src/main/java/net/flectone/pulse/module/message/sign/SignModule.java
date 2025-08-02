package net.flectone.pulse.module.message.sign;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
public class SignModule extends AbstractModule {

    private final Message.Sign message;
    private final Permission.Message.Sign permission;
    private final MessagePipeline messagePipeline;

    @Inject
    public SignModule(FileResolver fileResolver,
                      MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getSign();
        this.permission = fileResolver.getPermission().getMessage().getSign();
        this.messagePipeline = messagePipeline;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public String format(FPlayer fPlayer, String string) {
        if (checkModulePredicates(fPlayer)) return null;

        try {
            Component deserialized = LegacyComponentSerializer.legacySection().deserialize(string);

            Component component = messagePipeline.builder(fPlayer, string.replace("§", "&"))
                    .flag(MessageFlag.USER_MESSAGE, true)
                    .build()
                    .mergeStyle(deserialized);

            return LegacyComponentSerializer.legacySection().serialize(component);

        } catch (ParsingException ignored) {
            // ignore problem string
        }

        return string;
    }
}
