package net.flectone.pulse.module.message.advancement.extractor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.advancement.model.ChatAdvancement;
import net.flectone.pulse.module.message.advancement.model.CommandAdvancement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.type.tuple.Pair;
import org.incendo.cloud.type.tuple.Triplet;

import java.util.List;
import java.util.Optional;

@Singleton
public class AdvancementExtractor {

    @Inject
    public AdvancementExtractor() {
    }

    public Optional<ChatAdvancement> extractFromChat(TranslatableMessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();
        List<Component> translationArguments = translatableComponent.args();
        if (translationArguments.size() < 2) return Optional.empty();

        if (!(translationArguments.get(0) instanceof TextComponent targetComponent)) return Optional.empty();
        String target = targetComponent.content();

        Component achievementComp = translationArguments.get(1);
        Pair<String, String> pair = switch (achievementComp) {
            case TranslatableComponent titleComponent when titleComponent.key().equals("chat.square_brackets") && !titleComponent.args().isEmpty() ->
                    extractChatAdvancementComponent(titleComponent.args().get(0));
            case TextComponent textComp when textComp.content().equals("[") && !textComp.children().isEmpty() ->
                    extractChatAdvancementComponent(textComp.children().get(0));
            default -> null;
        };
        if (pair == null) return Optional.empty();

        String title = pair.first();
        String description = pair.second();

        if (description.isBlank() && title.contains(".title")) {
            description = title.replace(".title", ".description");
        }

        ChatAdvancement chatAdvancement = new ChatAdvancement(target, title, description, event.getKey());
        return Optional.of(chatAdvancement);
    }

    public Optional<CommandAdvancement> extractFromCommand(TranslatableMessageReceiveEvent event) {
        TranslatableComponent translatableComponent = event.getComponent();
        if (translatableComponent.args().size() < 2) return Optional.empty();

        Component argument;
        Component playerArgument;

        MinecraftTranslationKey type = event.getKey();
        if (type == MinecraftTranslationKey.COMMANDS_ACHIEVEMENT_GIVE_ONE
                || type == MinecraftTranslationKey.COMMANDS_ADVANCEMENT_GRANT_EVERYTHING_SUCCESS
                || type == MinecraftTranslationKey.COMMANDS_ADVANCEMENT_REVOKE_EVERYTHING_SUCCESS) {
            playerArgument = translatableComponent.args().get(0);
            argument = translatableComponent.args().get(1);
        } else {
            argument = translatableComponent.args().get(0);
            playerArgument = translatableComponent.args().get(1);
        }

        if (!(playerArgument instanceof TextComponent playerComponent)) return Optional.empty();

        String target = playerComponent.content();

        String content = null;
        ChatAdvancement chatAdvancement = null;
        AdvancementModule.Relation relation;

        switch (type) {
            case COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS,
                 COMMANDS_ACHIEVEMENT_TAKE_ONE, COMMANDS_ACHIEVEMENT_GIVE_ONE -> {
                String title;
                String description;
                MinecraftTranslationKey advancementType;

                switch (argument) {
                    case TranslatableComponent argumentIn when argumentIn.key().equals("chat.square_brackets") && !argumentIn.args().isEmpty() -> {
                        Triplet<String, String, MinecraftTranslationKey> triplet = extractCommandAdvancementComponent(argumentIn.args().get(0));
                        title = triplet.first();
                        description = triplet.second();
                        advancementType = triplet.third();
                    }
                    case TextComponent textComponent when textComponent.content().equals("[") && !textComponent.children().isEmpty() -> {
                        Triplet<String, String, MinecraftTranslationKey> triplet = extractCommandAdvancementComponent(textComponent.children().get(0));
                        title = triplet.first();
                        description = triplet.second();
                        advancementType = triplet.third();
                    }
                    case TextComponent textComponent -> {
                        content = textComponent.content();
                        relation = AdvancementModule.Relation.ONE_TO_ONE_TEXT;
                        CommandAdvancement commandAdvancement = new CommandAdvancement(relation, target, null, content);
                        return Optional.of(commandAdvancement);
                    }
                    default -> {
                        return Optional.empty();
                    }
                }

                chatAdvancement = new ChatAdvancement(target, title, description, advancementType);
                relation = AdvancementModule.Relation.ONE_TO_ONE_ADVANCEMENT;
            }
            case COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS,
                 COMMANDS_ACHIEVEMENT_TAKE_MANY, COMMANDS_ACHIEVEMENT_GIVE_MANY,
                 COMMANDS_ADVANCEMENT_GRANT_EVERYTHING_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_EVERYTHING_SUCCESS,
                 COMMANDS_ADVANCEMENT_REVOKE_ONLY_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_ONLY_SUCCESS -> {
                if (!(argument instanceof TextComponent textComponent)) return Optional.empty();

                content = textComponent.content();
                relation = type == MinecraftTranslationKey.COMMANDS_ADVANCEMENT_REVOKE_ONLY_SUCCESS
                        || type == MinecraftTranslationKey.COMMANDS_ADVANCEMENT_GRANT_ONLY_SUCCESS
                        ? AdvancementModule.Relation.ONE_TO_ONE_TEXT
                        : AdvancementModule.Relation.MANY_TO_ONE;
            }
            default -> {
                return Optional.empty();
            }
        }

        CommandAdvancement commandAdvancement = new CommandAdvancement(relation, target, chatAdvancement, content);
        return Optional.of(commandAdvancement);
    }

    private Pair<String, String> extractChatAdvancementComponent(Component component) {
        String title = "";
        String description = "";
        if (component instanceof TextComponent textComponent) {
            title = textComponent.content();
        } else if (component instanceof TranslatableComponent translatableComponent) {
            title = translatableComponent.key();
            HoverEvent<?> hoverEvent = translatableComponent.hoverEvent();
            if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
                Component hoverValue = (Component) hoverEvent.value();
                if (hoverValue.children().size() > 1) {
                    Component descComponent = hoverValue.children().get(1);
                    description = descComponent instanceof TranslatableComponent descTranslatable
                            ? descTranslatable.key()
                            : descComponent instanceof TextComponent descText
                            ? descText.content()
                            : "";
                }
            }
        }

        return Pair.of(title, description);
    }

    private Triplet<String, String, MinecraftTranslationKey> extractCommandAdvancementComponent(Component component) {
        String title = "";
        String description = "";
        MinecraftTranslationKey advancementType = MinecraftTranslationKey.CHAT_TYPE_ADVANCEMENT_TASK;

        if (component instanceof TextComponent textComponent) {
            title = textComponent.content();
        } else if (component instanceof TranslatableComponent titleComponent) {
            title = titleComponent.key();
            HoverEvent<?> hoverEvent = titleComponent.hoverEvent();
            if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
                Component hoverValue = (Component) hoverEvent.value();
                if (hoverValue.children().size() > 1) {
                    Component descComponent = hoverValue.children().get(1);

                    description = descComponent instanceof TranslatableComponent descTranslatable
                            ? descTranslatable.key()
                            : descComponent instanceof TextComponent descText
                            ? descText.content()
                            : "";

                    advancementType = NamedTextColor.DARK_PURPLE.equals(hoverValue.color())
                            ? MinecraftTranslationKey.CHAT_TYPE_ADVANCEMENT_CHALLENGE
                            : MinecraftTranslationKey.CHAT_TYPE_ADVANCEMENT_TASK;
                }
            } else {
                title = titleComponent.key();
                description = "";
            }
        }

        return Triplet.of(title, description, advancementType);
    }
}
