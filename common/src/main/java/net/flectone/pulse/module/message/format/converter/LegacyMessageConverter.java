package net.flectone.pulse.module.message.format.converter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LegacyMessageConverter {

    private static final MessageFlag[] FORMATTER_FLAGS = new MessageFlag[]{
            MessageFlag.CAPS_MODULE,
            MessageFlag.DELETE_MODULE,
            MessageFlag.FIXATION_MODULE,
            MessageFlag.FLOOD_MODULE,
            MessageFlag.MENTION_MODULE,
            MessageFlag.PADDING_MODULE,
            MessageFlag.QUESTIONANSWER_MODULE,
            MessageFlag.SPAM_MODULE,
            MessageFlag.SWEAR_MODULE,
            MessageFlag.TRANSLATE_MODULE,
            MessageFlag.VIOLATION_PROCESSING,
            MessageFlag.LEGACY_COLOR_CONVERSION,
            MessageFlag.REMOVE_DISABLED_TAGS
    };

    private static final boolean[] FORMATTER_FLAG_VALUES = new boolean[13];

    private final LegacyColorConverter legacyColorConverter;
    private final MessagePipeline messagePipeline;

    public boolean needsParsing(@Nullable Component component) {
        if (component == null) return false;

        if (component instanceof TextComponent textComponent && needsParsing(textComponent.content())) {
            return true;
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            for (TranslationArgument argument : translatableComponent.arguments()) {
                if (argument.value() instanceof Component argumentComponent && needsParsing(argumentComponent)) {
                    return true;
                }
            }
        }

        for (Component child : component.children()) {
            if (needsParsing(child)) return true;
        }

        return false;
    }

    public @Nullable Component convert(@Nullable Component component, FPlayer receiver) {
        if (component == null) return null;

        List<Component> oldChildren = component.children();
        List<Component> newChildren = oldChildren.stream()
                .map(child -> convert(child, receiver))
                .toList();

        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            if (!needsParsing(content)) {
                return newChildren.equals(oldChildren) ? component : textComponent.children(newChildren);
            }

            return Component.empty()
                    .style(textComponent.style())
                    .append(messagePipeline.build(MessageContext.builder()
                            .module(ModuleName.MESSAGE_FORMAT_UNIFORM)
                            .sender(receiver)
                            .receiver(receiver)
                            .message(legacyColorConverter.convert(content))
                            .flags(FORMATTER_FLAGS, FORMATTER_FLAG_VALUES)
                            .build()
                    ))
                    .append(newChildren);
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            List<TranslationArgument> oldArguments = translatableComponent.arguments();
            List<TranslationArgument> newArguments = new ArrayList<>(oldArguments.size());
            boolean argumentsChanged = false;

            for (TranslationArgument argument : oldArguments) {
                if (argument.value() instanceof Component argumentComponent) {
                    Component convertedComponent = convert(argumentComponent, receiver);
                    if (convertedComponent != argumentComponent) {
                        newArguments.add(TranslationArgument.component(convertedComponent));
                        argumentsChanged = true;
                        continue;
                    }
                }

                newArguments.add(argument);
            }

            if (!argumentsChanged && newChildren.equals(oldChildren)) {
                return component;
            }

            TranslatableComponent convertedComponent = translatableComponent;
            if (argumentsChanged) {
                convertedComponent = convertedComponent.arguments(newArguments);
            }

            if (!newChildren.equals(oldChildren)) {
                convertedComponent = convertedComponent.children(newChildren);
            }

            return convertedComponent;
        }

        return newChildren.equals(oldChildren) ? component : component.children(newChildren);
    }

    private boolean needsParsing(String text) {
        return text.indexOf('<') >= 0 || text.indexOf('%') >= 0 || containsLegacyCodes(text);
    }

    private boolean containsLegacyCodes(String text) {
        for (int index = 0; index + 1 < text.length(); index++) {
            char symbol = text.charAt(index);
            if (symbol != '&' && symbol != '§') continue;

            char next = Character.toLowerCase(text.charAt(index + 1));
            if (next == '&' || next == '#' || next == '@' || next == 'x' || next == 'r'
                    || (next >= '0' && next <= '9')
                    || (next >= 'a' && next <= 'f')
                    || (next >= 'k' && next <= 'o')) {
                return true;
            }
        }

        return false;
    }

}
