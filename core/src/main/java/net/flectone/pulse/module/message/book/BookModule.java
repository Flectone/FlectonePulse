package net.flectone.pulse.module.message.book;

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
public class BookModule extends AbstractModule {

    private final Message.Book message;
    private final Permission.Message.Book permission;
    private final MessagePipeline messagePipeline;

    @Inject
    public BookModule(FileResolver fileResolver,
                      MessagePipeline messagePipeline) {
        this.message = fileResolver.getMessage().getBook();
        this.permission = fileResolver.getPermission().getMessage().getBook();
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
        if (isModuleDisabledFor(fPlayer)) return null;

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

