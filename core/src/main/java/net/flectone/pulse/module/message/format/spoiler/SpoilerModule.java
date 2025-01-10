package net.flectone.pulse.module.message.format.spoiler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@Singleton
public class SpoilerModule extends AbstractModuleMessage<Localization.Message.Format.Spoiler> {

    private final Message.Format.Spoiler message;
    private final Permission.Message.Format.Spoiler permission;

    @Inject private ComponentUtil componentUtil;

    @Inject
    public SpoilerModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getFormat().getSpoiler());

        message = fileManager.getMessage().getFormat().getSpoiler();
        permission = fileManager.getPermission().getMessage().getFormat().getSpoiler();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public TagResolver spoilerTag(FEntity sender, FEntity receiver, boolean userMessage) {
        if (checkModulePredicates(sender)) return TagResolver.empty();

        return TagResolver.resolver("spoiler", (argumentQueue, context) -> {
            Tag.Argument spoilerTag = argumentQueue.peek();
            if (spoilerTag == null) return Tag.selfClosingInserting(Component.empty());

            String spoilerText = spoilerTag.value();

            Component spoilerComponent = componentUtil.builder(sender, receiver, spoilerText)
                    .userMessage(userMessage)
                    .build();

            int length = PlainTextComponentSerializer.plainText().serialize(spoilerComponent).length();

            Localization.Message.Format.Spoiler localization = resolveLocalization(receiver);

            Component component = Component.text(localization.getSymbol().repeat(length))
                    .hoverEvent(componentUtil.builder(sender, receiver, localization.getHover())
                            .build()
                            .replaceText(TextReplacementConfig.builder().match("<message>")
                                    .replacement(spoilerComponent)
                                    .build()
                            )
                    )
                    .color(componentUtil.builder(sender, receiver, message.getColor())
                            .build()
                            .color()
                    );

            return Tag.selfClosingInserting(component);
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
