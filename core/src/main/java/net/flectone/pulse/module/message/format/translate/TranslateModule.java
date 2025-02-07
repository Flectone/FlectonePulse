package net.flectone.pulse.module.message.format.translate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
public class TranslateModule extends AbstractModuleMessage<Localization.Message.Format.Translate> {

    private final Message.Format.Translate message;
    private final Permission.Message.Format.Translate permission;

    @Inject
    private ComponentUtil componentUtil;

    @Inject
    public TranslateModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getFormat().getTranslate());

        message = fileManager.getMessage().getFormat().getTranslate();
        permission = fileManager.getPermission().getMessage().getFormat().getTranslate();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    public TagResolver translateTag(FEntity fPlayer, FEntity receiver) {
        if (checkModulePredicates(fPlayer)) return TagResolver.empty();

        return TagResolver.resolver("translateto", (argumentQueue, context) -> {
            if (!(receiver instanceof FPlayer fReceiver) || fReceiver.isUnknown()) return Tag.selfClosingInserting(Component.empty());
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            String firstLang;
            String secondLang;
            String text;

            Tag.Argument first = argumentQueue.pop();

            if (argumentQueue.hasNext()) {
                Tag.Argument second = argumentQueue.pop();

                if (argumentQueue.hasNext()) {
                    // translateto language language message
                    firstLang = first.value();
                    secondLang = second.value();
                    text = argumentQueue.pop().value();
                } else {
                    // translateto auto language message
                    firstLang = "auto";
                    secondLang = first.value();
                    text = second.value();
                }
            } else {
                // translate auto language message
                firstLang = "auto";
                secondLang = fReceiver.getLocale();
                text = first.value();
            }

            String action = resolveLocalization(receiver).getAction()
                    .replaceFirst("<language>", firstLang)
                    .replaceFirst("<language>", secondLang)
                    .replace("<message>", text);

            return Tag.selfClosingInserting(componentUtil.builder(fPlayer, receiver, action)
                    .interactiveChat(false)
                    .question(false)
                    .mention(false)
                    .translate(false)
                    .build()
            );
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
