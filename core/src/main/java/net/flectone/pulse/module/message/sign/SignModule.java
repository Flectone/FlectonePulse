package net.flectone.pulse.module.message.sign;

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
public class SignModule extends AbstractModule {

    private final Message.Sign message;
    private final Permission.Message.Sign permission;

    private final MessageFormatter messageFormatter;

    @Inject
    public SignModule(FileManager fileManager,
                      MessageFormatter messageFormatter) {
        this.messageFormatter = messageFormatter;

        message = fileManager.getMessage().getSign();
        permission = fileManager.getPermission().getMessage().getSign();
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
