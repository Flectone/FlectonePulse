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
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;

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
    private final FileResolver fileResolver;
    private final PacketProvider packetProvider;

    public void reload() {
        translationVanillaMessages.clear();

        List<Message.Vanilla.VanillaMessage> vanillaMessages = fileResolver.getMessage().getVanilla().getTypes();

        vanillaMessages.forEach(vanillaMessage -> vanillaMessage.getTranslationKeys()
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

    public Optional<ParsedComponent> extract(TranslatableComponent translatableComponent) {
        String translationKey = getOrLegacyMapping(translatableComponent.key());

        Map<String, String> localization = fileResolver.getLocalization().getMessage().getVanilla().getTypes();
        if (!localization.containsKey(translationKey)) return Optional.empty();

        Message.Vanilla.VanillaMessage vanillaMessage = translationVanillaMessages.getOrDefault(translationKey, new Message.Vanilla.VanillaMessage());

        Map<Integer, Optional<?>> parsedArguments = new HashMap<>();

        for (int i = 0; i < translatableComponent.arguments().size(); i++) {
            Optional<FEntity> entity = extractFEntity(translatableComponent, i);
            if (entity.isEmpty() || entity.get().isUnknown() && entity.get().getShowEntityName() == null) {
                Optional<Component> component = getComponent(translatableComponent, i);
                parsedArguments.put(i, component);
            } else {
                parsedArguments.put(i, entity);
            }
        }

        ParsedComponent parsedComponent = new ParsedComponent(translationKey, vanillaMessage, parsedArguments);
        return Optional.of(parsedComponent);
    }

    public Optional<FEntity> extractFEntity(TranslatableComponent translatableComponent, int index) {
        Optional<Component> component = getComponent(translatableComponent, index, Component.class);
        if (component.isEmpty()) return Optional.empty();

        return extractFEntity(component.get());
    }

    public Optional<FEntity> extractFEntity(Component component) {
        HoverEvent<?> hoverEvent = component.hoverEvent();

        // support legacy and InteractiveChat components
        if (hoverEvent == null && !component.children().isEmpty()) {
            hoverEvent = component.children().getFirst().hoverEvent();
        }

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
