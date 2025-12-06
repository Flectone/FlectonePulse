package net.flectone.pulse.module.message.vanilla;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.vanilla.extractor.Extractor;
import net.flectone.pulse.module.message.vanilla.listener.VanillaPulseListener;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaModule extends AbstractModuleLocalization<Localization.Message.Vanilla> {

    private static final String ARGUMENT = "argument";

    private final FileResolver fileResolver;
    private final Extractor extractor;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final PacketSender packetSender;

    @Override
    public void onEnable() {
        super.onEnable();

        extractor.reload();

        listenerRegistry.register(VanillaPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.VANILLA;
    }

    @Override
    public Message.Vanilla config() {
        return fileResolver.getMessage().getVanilla();
    }

    @Override
    public Permission.Message.Vanilla permission() {
        return fileResolver.getPermission().getMessage().getVanilla();
    }

    @Override
    public Localization.Message.Vanilla localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getVanilla();
    }

    @Async
    public void send(FPlayer fPlayer, ParsedComponent parsedComponent) {
        if (isModuleDisabledFor(fPlayer)) return;

        Range range = parsedComponent.vanillaMessage().getRange();
        if (parsedComponent.translationKey().startsWith("death.")) {
            FEntity target = getDeathTarget(parsedComponent);
            if (target instanceof FPlayer) {
                if (!target.equals(fPlayer)) {
                    if (parsedComponent.vanillaMessage().isMultiMessage()) return;
                } else {
                    String format = StringUtils.defaultString(localization(fPlayer).getTypes().get(parsedComponent.translationKey()));

                    Component component = messagePipeline.builder(fPlayer, format)
                            .tagResolvers(tagResolvers(fPlayer, parsedComponent))
                            .build();

                    sendPersonalDeath(fPlayer, component);
                }
            } else {
                range = Range.get(Range.Type.PLAYER);
            }
        } else if (parsedComponent.vanillaMessage().isMultiMessage()) {
            FEntity target = getDeathTarget(parsedComponent);
            if (target != null && !fPlayer.equals(target)) return;
        }

        String vanillaMessageName = parsedComponent.vanillaMessage().getName();

        sendMessage(VanillaMetadata.<Localization.Message.Vanilla>builder()
                .parsedComponent(parsedComponent)
                .sender(fPlayer)
                .format(localization -> StringUtils.defaultString(localization.getTypes().get(parsedComponent.translationKey())))
                .tagResolvers(fResolver -> tagResolvers(fResolver, parsedComponent))
                .range(range)
                .filter(fResolver -> vanillaMessageName.isEmpty() || fResolver.isSetting(vanillaMessageName))
                .destination(parsedComponent.vanillaMessage().getDestination())
                .integration()
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeString(parsedComponent.translationKey());
                    dataOutputStream.writeAsJson(parsedComponent.arguments());
                })
                .build()
        );
    }

    private FEntity getDeathTarget(ParsedComponent parsedComponent) {
        Object target = parsedComponent.arguments().get(0);
        return target instanceof FEntity fEntity ? fEntity : null;
    }

    public TagResolver[] tagResolvers(FPlayer fResolver, ParsedComponent parsedComponent) {
        List<TagResolver> tags = new ArrayList<>();

        tags.add(TagResolver.resolver(ARGUMENT, (argumentQueue, context) -> {
            if (!argumentQueue.hasNext()) return Tag.selfClosingInserting(Component.empty());

            OptionalInt numberArgument = argumentQueue.pop().asInt();
            if (numberArgument.isEmpty()) return Tag.selfClosingInserting(Component.empty());

            int number = numberArgument.getAsInt();
            if (number > parsedComponent.arguments().size()) return Tag.selfClosingInserting(Component.empty());

            Object replacement = parsedComponent.arguments().get(number);
            if (!argumentQueue.hasNext()) return argumentResolver(fResolver, replacement);
            if (!(replacement instanceof Component component)) return Tag.selfClosingInserting(Component.empty());

            String type = argumentQueue.pop().lowerValue();
            switch (type) {
                case "text" -> {
                    Optional<String> text = extractor.extractTextContentOrTranslatableKey(component);
                    if (text.isPresent()) {
                        return Tag.selfClosingInserting(Component.text(text.get()));
                    }
                }
                case "style" -> {
                    return Tag.styling(style -> style.merge(component.style()));
                }
                case "hover_text", "hover_style" -> {
                    HoverEvent<?> hoverEvent = findFirstHoverEvent(component);
                    if (hoverEvent != null) {
                        if (type.equals("hover_style")) return Tag.styling(style -> style.hoverEvent(hoverEvent));

                        return Tag.selfClosingInserting(switch (hoverEvent.value()) {
                            case Component hoverComponent -> Component.text(PlainTextComponentSerializer.plainText().serialize(hoverComponent));
                            case HoverEvent.ShowEntity showEntity -> showEntity.name() == null ? Component.empty() : showEntity.name();
                            case HoverEvent.ShowItem showItem -> Component.text(showItem.item().value());
                            default -> Component.empty();
                        });
                    }
                }
            }

            return Tag.selfClosingInserting(Component.empty());

        }));

        return tags.toArray(new TagResolver[0]);
    }


    private Tag argumentResolver(FPlayer fResolver, Object replacement) {
        return switch (replacement) {
            case FEntity fTarget -> {
                Localization.Message.Vanilla localization = localization(fResolver);
                String formatTarget = fTarget.getType().equals(FPlayer.TYPE)
                        ? localization.getFormatPlayer()
                        : localization.getFormatEntity();

                yield Tag.selfClosingInserting(messagePipeline.builder(fTarget, fResolver, formatTarget).build());
            }
            case Component component -> {
                TextColor color = component.color();
                if (NamedTextColor.GRAY.equals(component.color())
                        || NamedTextColor.WHITE.equals(component.color())
                        || NamedTextColor.AQUA.equals(component.color())) {
                    color = null;
                }

                // fix serialization issue [%s] for console and integration
                if (fResolver.isUnknown() && component instanceof TranslatableComponent translatableComponent
                        && translatableComponent.key().equals("chat.square_brackets")) {
                    yield Tag.selfClosingInserting(
                            Component.text("[").color(color)
                                    .append(extractor.getValueComponent(component))
                                    .append(Component.text("]"))
                    );
                }

                yield Tag.selfClosingInserting(component.color(color));
            }
            default -> Tag.selfClosingInserting(Component.empty());
        };
    }

    private HoverEvent<?> findFirstHoverEvent(Component component) {
        if (component.hoverEvent() != null) return component.hoverEvent();

        if (component instanceof TranslatableComponent translatableComponent) {
            for (TranslationArgument translationArgument : translatableComponent.arguments()) {
                HoverEvent<?> hoverEvent = findFirstHoverEvent(translationArgument.asComponent());
                if (hoverEvent != null) return hoverEvent;
            }
        }

        for (Component children : component.children()) {
            if (children.hoverEvent() != null) return component.hoverEvent();
        }

        return null;
    }


    @Sync
    public void sendPersonalDeath(FPlayer fPlayer, Component component) {
        packetSender.send(fPlayer, new WrapperPlayServerDeathCombatEvent(fPlayerService.getEntityId(fPlayer), null, component));
    }
}
