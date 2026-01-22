package net.flectone.pulse.module.message.vanilla.extractor;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.message.vanilla.model.Mapping;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Extractor {

    private static final Map<String, Mapping> LEGACY_TRANSLATION_MAPPINGS = new HashMap<>() {
        {
            put("commands.setblock.success",
                    new Mapping(serverVersion -> serverVersion.isNewerThanOrEquals(ServerVersion.V_1_19_4), "commands.setblock.success",
                            new Mapping(serverVersion -> true, "commands.setblock.success.1_19_4", null)
            ));

            put("commands.setworldspawn.success",
                    new Mapping(serverVersion -> serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9), "commands.setworldspawn.success",
                            new Mapping(serverVersion -> serverVersion.isOlderThanOrEquals(ServerVersion.V_1_16), "commands.setworldspawn.success.1_16",
                                    new Mapping(serverVersion -> true, "commands.setworldspawn.success.1_21_8", null)
            )));

            put("commands.spawnpoint.success.multiple",
                    new Mapping(serverVersion -> serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9), "commands.spawnpoint.success.multiple",
                            new Mapping(serverVersion -> serverVersion.isOlderThanOrEquals(ServerVersion.V_1_14_2), "commands.spawnpoint.success.multiple.1_14_2",
                                    new Mapping(serverVersion -> true, "commands.spawnpoint.success.multiple.1_21_8", null)
            )));

            put("commands.spawnpoint.success.single",
                    new Mapping(serverVersion -> serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_9), "commands.spawnpoint.success.single",
                            new Mapping(serverVersion -> serverVersion.isOlderThanOrEquals(ServerVersion.V_1_14_2), "commands.spawnpoint.success.single.1_14_2",
                                    new Mapping(serverVersion -> true, "commands.spawnpoint.success.single.1_21_8", null)
            )));
        }
    };

    private final Map<String, Message.Vanilla.VanillaMessage> translationVanillaMessages = new HashMap<>();

    private final EntityUtil entityUtil;
    private final FPlayerService fPlayerService;
    private final FileFacade fileFacade;
    private final PacketProvider packetProvider;

    public void reload() {
        translationVanillaMessages.clear();

        List<Message.Vanilla.VanillaMessage> vanillaMessages = fileFacade.message().vanilla().types();

        vanillaMessages.forEach(vanillaMessage -> vanillaMessage.translationKeys()
                        .forEach(translationKey -> translationVanillaMessages.put(translationKey, vanillaMessage))
        );
    }

    public String getOrLegacyMapping(String translationKey) {
        Mapping mapping = LEGACY_TRANSLATION_MAPPINGS.get(translationKey);
        if (mapping == null) return translationKey;

        return recursiveGetTranslationKey(mapping);
    }

    private String recursiveGetTranslationKey(Mapping mapping) {
        ServerVersion currentServerVersion = packetProvider.getServerVersion();
        if (mapping.predicate().test(currentServerVersion)) return mapping.newTranslationKey();

        Mapping nextMapping = mapping.orElse();
        if (nextMapping == null) return "";

        return recursiveGetTranslationKey(mapping.orElse());
    }

    public Message.Vanilla.VanillaMessage getVanillaMessage(String translationKey) {
        return translationVanillaMessages.getOrDefault(translationKey, Message.Vanilla.VanillaMessage.builder().build());
    }

    public Optional<ParsedComponent> extract(TranslatableComponent translatableComponent) {
        String translationKey = getOrLegacyMapping(translatableComponent.key());

        Map<String, String> localization = fileFacade.localization().message().vanilla().types();
        if (!localization.containsKey(translationKey)) return Optional.empty();

        Message.Vanilla.VanillaMessage vanillaMessage = getVanillaMessage(translationKey);

        Map<Integer, Object> parsedArguments = new HashMap<>();

        for (int i = 0; i < translatableComponent.arguments().size(); i++) {
            Object argument = extractArgument(translatableComponent, i);
            parsedArguments.put(i, argument);
        }

        ParsedComponent parsedComponent = new ParsedComponent(translationKey, vanillaMessage, parsedArguments);
        return Optional.of(parsedComponent);
    }

    public @Nullable Object extractArgument(TranslatableComponent translatableComponent, int index) {
        return getComponent(translatableComponent, index).map(component -> {
            Optional<FEntity> firstFEntity = extractFEntity(component);

            if (component.children().isEmpty()) {
                FEntity fEntity = firstFEntity.orElse(null);
                return isValid(fEntity) ? fEntity : component;
            }

            Set<FEntity> entities = LinkedHashSet.newLinkedHashSet(component.children().size() + 1);

            firstFEntity.ifPresent(entities::add);

            for (Component child : component.children()) {
                extractFEntity(child).ifPresent(entities::add);
            }

            if (entities.stream().anyMatch(this::isValid)) {
                return entities.size() == 1 ? entities.iterator().next() : entities;
            }

            return component;
        }).orElse(Component.empty());
    }

    private boolean isValid(FEntity entity) {
        return entity != null && (!entity.isUnknown() || entity.getShowEntityName() != null);
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

            FEntity fEntity = new FEntity(FEntity.UNKNOWN_NAME, uuid, type);
            fEntity.setShowEntityName(showEntity.name());

            return Optional.of(fEntity);
        }

        Optional<String> optionalName = extractTextContentOrTranslatableKey(component);
        if (optionalName.isEmpty()) return Optional.empty();

        FEntity fPlayer = fPlayerService.getFPlayer(optionalName.get());

        return fPlayer.isUnknown() ? Optional.empty() : Optional.of(fPlayer);
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

    public Component getValueComponent(Component component) {
        Optional<Component> component1 = switch (component) {
            case TranslatableComponent valueTranslatableComponent when !valueTranslatableComponent.arguments().isEmpty() ->
                    Optional.of(valueTranslatableComponent.arguments().getFirst().asComponent());

            case TextComponent valueTextComponent when !valueTextComponent.children().isEmpty() ->
                    Optional.of(valueTextComponent.children().getFirst().asComponent());

            case TextComponent valueTextComponent -> Optional.of(valueTextComponent);

            default -> Optional.empty();
        };

        return component1.map(this::recursiveExtractValueComponent).orElseGet(Component::empty);
    }


    // support legacy and InteractiveChat components
    private Component recursiveExtractValueComponent(Component valueComponent) {
        if (!valueComponent.style().hasDecoration(TextDecoration.ITALIC)
                && valueComponent instanceof TextComponent valueTextComponent
                && valueTextComponent.content().isEmpty()
                && !valueTextComponent.children().isEmpty()) {
            return recursiveExtractValueComponent(valueTextComponent.children().getFirst());
        }

        if (valueComponent instanceof TranslatableComponent valueTranslatableComponent
                && valueTranslatableComponent.key().equals("chat.square_brackets")
                && !valueTranslatableComponent.arguments().isEmpty()) {

            return recursiveExtractValueComponent(valueTranslatableComponent.arguments().getFirst().asComponent());
        }

        return valueComponent;
    }

    public Optional<String> extractTextContentOrTranslatableKey(Component component) {
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
