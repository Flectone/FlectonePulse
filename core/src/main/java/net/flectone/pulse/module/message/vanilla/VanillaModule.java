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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class VanillaModule extends AbstractModuleLocalization<Localization.Message.Vanilla> {

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

        sendMessage(VanillaMetadata.<Localization.Message.Vanilla>builder()
                .parsedComponent(parsedComponent)
                .sender(fPlayer)
                .format(localization -> StringUtils.defaultString(localization.getTypes().get(parsedComponent.translationKey())))
                .tagResolvers(fResolver -> tagResolvers(fResolver, parsedComponent))
                .range(range)
                .destination(parsedComponent.vanillaMessage().getDestination())
                .build()
        );
    }

    private FEntity getDeathTarget(ParsedComponent parsedComponent) {
        Optional<?> target = parsedComponent.arguments().get(0);
        return (FEntity) target.filter(object -> object instanceof FEntity).orElse(null);
    }

    private TagResolver[] tagResolvers(FPlayer fResolver, ParsedComponent parsedComponent) {
        List<TagResolver> tags = new ArrayList<>();
        parsedComponent.arguments().forEach((index, replacement) -> {
            if (replacement.isEmpty()) {
                tags.add(TagResolver.resolver("arg_" + index, (argumentQueue, context) -> Tag.selfClosingInserting(Component.empty())));
                return;
            }

            switch (replacement.get()) {
                case FEntity fTarget -> tags.add(targetTag("arg_" + index, fResolver, fTarget));
                case Component component -> tags.add(TagResolver.resolver("arg_" + index, (argumentQueue, context) -> {

                    TextColor color = component.color();
                    if (NamedTextColor.GRAY.equals(component.color())
                            || NamedTextColor.WHITE.equals(component.color())
                            || NamedTextColor.AQUA.equals(component.color())) {
                        color = null;
                    }

                    return Tag.selfClosingInserting(component.color(color));
                }));
                default -> tags.add(TagResolver.resolver("arg_" + index, (argumentQueue, context) -> Tag.selfClosingInserting(Component.empty())));
            }
        });

        return tags.isEmpty() ? null : tags.toArray(new TagResolver[0]);
    }

    @Sync
    public void sendPersonalDeath(FPlayer fPlayer, Component component) {
        packetSender.send(fPlayer, new WrapperPlayServerDeathCombatEvent(fPlayerService.getEntityId(fPlayer), null, component));
    }
}
