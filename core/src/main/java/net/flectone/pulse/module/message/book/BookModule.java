package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.Strings;

@Singleton
public class BookModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MessagePipeline messagePipeline;

    @Inject
    public BookModule(FileResolver fileResolver,
                      MessagePipeline messagePipeline) {
        this.fileResolver = fileResolver;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public Message.Book config() {
        return fileResolver.getMessage().getBook();
    }

    @Override
    public Permission.Message.Book permission() {
        return fileResolver.getPermission().getMessage().getBook();
    }

    public String format(FPlayer fPlayer, String string) {
        if (isModuleDisabledFor(fPlayer)) return null;

        try {
            Component deserialized = LegacyComponentSerializer.legacySection().deserialize(string);

            Component component = messagePipeline.builder(fPlayer, Strings.CS.replace(string, "§", "&"))
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

