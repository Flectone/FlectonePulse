package net.flectone.pulse.module.message.book;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
public class BookModule extends AbstractModule {

    private final Message.Book message;
    private final Permission.Message.Book permission;

    private final MessageFormatter messageFormatter;

    @Inject
    public BookModule(FileManager fileManager,
                      MessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;

        message = fileManager.getMessage().getBook();
        permission = fileManager.getPermission().getMessage().getBook();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public String format(FPlayer fPlayer, String string) {
        if (checkModulePredicates(fPlayer)) return null;

        try {
            Component deserialized = LegacyComponentSerializer.legacySection().deserialize(string);

            Component component = messageFormatter.builder(fPlayer, string.replace("§", "&"))
                    .userMessage(true)
                    .build()
                    .mergeStyle(deserialized);

            return LegacyComponentSerializer.legacySection().serialize(component);

        } catch (ParsingException ignored) {}

        return string;
    }
}

