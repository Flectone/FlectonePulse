package net.flectone.pulse.module.message.sign;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.pipeline.MessagePipeline;
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
        this.messagePipeline = messagePipeline;

        message = fileResolver.getMessage().getSign();
        permission = fileResolver.getPermission().getMessage().getSign();
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
                    .userMessage(true)
                    .build()
                    .mergeStyle(deserialized);

            return LegacyComponentSerializer.legacySection().serialize(component);

        } catch (ParsingException ignored) {}

        return string;
    }
}
