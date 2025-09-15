package net.flectone.pulse.processing.extractor;

import com.google.inject.Inject;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class Extractor {

    @Inject private EntityUtil entityUtil;
    @Inject private FPlayerService fPlayerService;

    public Optional<FEntity> extractFEntity(TranslatableComponent translatableComponent, int index) {
        Optional<Component> component = getComponent(translatableComponent, index, Component.class);
        if (component.isEmpty()) return Optional.empty();

        return extractFEntity(component.get());
    }

    public Optional<FEntity> extractFEntity(Component component) {
        HoverEvent<?> hoverEvent = component.hoverEvent();
        if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_ENTITY) {
            HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();

            UUID uuid = showEntity.id();

            String rawType = showEntity.type().key().value();
            if (rawType.equals("player")) {
                return Optional.of(fPlayerService.getFPlayer(uuid));
            }

            String type = entityUtil.resolveEntityTranslationKey(rawType);

            FEntity fEntity = new FEntity(null, uuid, type);
            fEntity.setShowEntityName(showEntity.name());

            return Optional.of(fEntity);
        }

        Optional<String> optionalName = extractTextContentOrTranslatableKey(component);
        if (optionalName.isEmpty()) return Optional.empty();

        FPlayer fPlayer = fPlayerService.getFPlayer(optionalName.get());
        return Optional.of(fPlayer);
    }

    protected <T extends ComponentLike> Optional<T> parseComponent(Component component, Class<T> clazz) {
        if (clazz.isInstance(component)) {
            return Optional.of(clazz.cast(component));
        }

        return Optional.empty();
    }

    protected <T extends ComponentLike> Optional<T> getComponent(TranslatableComponent translatableComponent, int index, Class<T> clazz) {
        List<TranslationArgument> translationArguments = translatableComponent.arguments();
        if (index < translationArguments.size()) {
            return parseComponent(translationArguments.get(index).asComponent(), clazz);
        }

        return Optional.empty();
    }

    protected Optional<Component> getComponent(TranslatableComponent translatableComponent, int index) {
        return getComponent(translatableComponent, index, Component.class);
    }

    protected Optional<TextComponent> getTextComponent(TranslatableComponent translatableComponent, int index) {
        return getComponent(translatableComponent, index, TextComponent.class);
    }

    protected Optional<TranslatableComponent> getTranslatableComponent(TranslatableComponent translatableComponent, int index) {
        return getComponent(translatableComponent, index, TranslatableComponent.class);
    }

    protected Optional<Component> getValueComponent(TranslatableComponent translatableComponent, int index) {
        Optional<Component> component = getComponent(translatableComponent, index);
        return component.flatMap(value -> switch (value) {
            case TranslatableComponent valueTranslatableComponent when !valueTranslatableComponent.arguments().isEmpty() ->
                    Optional.of(valueTranslatableComponent.arguments().getFirst().asComponent());

            case TextComponent valueTextComponent when valueTextComponent.content().equals("[") && !valueTextComponent.children().isEmpty() ->
                    Optional.of(valueTextComponent.children().getFirst().asComponent());

            case TextComponent valueTextComponent -> Optional.of(valueTextComponent);

            default -> Optional.empty();
        });
    }

    protected Optional<String> extractTextContent(TranslatableComponent translatableComponent, int index) {
        return getTextComponent(translatableComponent, index).map(TextComponent::content);
    }

    protected Optional<String> extractTranslatableKey(TranslatableComponent translatableComponent, int index) {
        return getTranslatableComponent(translatableComponent, index).map(TranslatableComponent::key);
    }

    protected Optional<String> extractTextContentOrTranslatableKey(Component component) {
        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            if (!StringUtils.isEmpty(content)) return Optional.of(content);

            String insertion = textComponent.insertion();
            return insertion == null ? Optional.of("") : Optional.of(insertion);
        }

        if (component instanceof TranslatableComponent translatableComponent) {
            String key = translatableComponent.key();
            return Optional.of(key);
        }

        return Optional.empty();
    }
}
