package net.flectone.pulse.module.message.death;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.death.listener.DeathPacketListener;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.Item;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.EntityUtil;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.UUID;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

@Singleton
public class DeathModule extends AbstractModuleMessage<Localization.Message.Death> {

    @Getter private final Message.Death message;
    private final Permission.Message.Death permission;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;
    private final Gson gson;
    private final EntityUtil entityUtil;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public DeathModule(FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       PacketSender packetSender,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry,
                       IntegrationModule integrationModule,
                       Gson gson,
                       EntityUtil entityUtil,
                       EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getDeath());

        this.message = fileResolver.getMessage().getDeath();
        this.permission = fileResolver.getPermission().getMessage().getDeath();
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;
        this.gson = gson;
        this.entityUtil = entityUtil;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DeathPacketListener.class);
        eventProcessRegistry.registerMessageHandler(event -> {
            if (!event.getKey().startsWith("death.")) return;

            TranslatableComponent translatableComponent = event.getComponent();
            Death death = getDeath(translatableComponent, 0);
            if (death == null) return;

            Death killer = getDeath(translatableComponent, 1);
            death.setKiller(killer);

            Item item = getItem(translatableComponent);
            death.setItem(item);

            event.cancel();
            send(event.getUserUUID(), death);
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Inject
    private FLogger fLogger;

    @Async
    public void send(UUID receiver, Death death) {
        FEntity fTarget = convertDeath(death);
        if (fTarget == null) return;

        FPlayer fReceiver = fPlayerService.getFPlayer(receiver);

        if (!death.isPlayer()) {
            builder(fTarget)
                    .destination(message.getDestination())
                    .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.DEATH))
                    .filter(fPlayer -> integrationModule.isVanishedVisible(fTarget, fPlayer))
                    .receiver(fReceiver)
                    .format(s -> s.getTypes().get(death.getKey()))
                    .tagResolvers(fResolver -> new TagResolver[]{killerTag(fResolver, death.getKiller()), byItemTag(death.getItem())})
                    .sound(getSound())
                    .sendBuilt();
            return;
        }

        if (!fTarget.equals(fReceiver)) return;

        builder(fTarget)
                .range(message.getRange())
                .destination(message.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.DEATH))
                .filter(fPlayer -> integrationModule.isVanishedVisible(fTarget, fPlayer))
                .tag(MessageTag.DEATH)
                .format(s -> s.getTypes().get(death.getKey()))
                .tagResolvers(fResolver -> new TagResolver[]{killerTag(fResolver, death.getKiller()), byItemTag(death.getItem())})
                .proxy(output -> output.writeUTF(gson.toJson(death)))
                .integration()
                .sound(getSound())
                .sendBuilt();

        if (!death.isPlayer()) return;
        if (fTarget instanceof FPlayer player && !player.isSetting(FPlayer.Setting.DEATH)) return;

        Component component = messagePipeline.builder(fTarget, fReceiver, resolveLocalization(fReceiver).getTypes().get(death.getKey()))
                .tagResolvers(killerTag(fReceiver, death.getKiller()), byItemTag(death.getItem()))
                .build();

        sendPersonalDeath(fReceiver, component);
    }

    @Sync
    public void sendPersonalDeath(FPlayer fPlayer, Component component) {
        packetSender.send(fPlayer, new WrapperPlayServerDeathCombatEvent(fPlayerService.getEntityId(fPlayer), null, component));
    }

    private FEntity convertDeath(Death death) {
        if (death == null) return null;
        if (!death.isPlayer()) {
            return new FEntity(death.getTargetName(), death.getTargetUUID(), death.getTargetType() == null ? death.getTargetName() : death.getTargetType());
        }

        FPlayer fTarget = fPlayerService.getFPlayer(death.getTargetName());
        if (fTarget.isUnknown()) return null;
        if (checkModulePredicates(fTarget)) return null;

        return fTarget;
    }

    public TagResolver byItemTag(Item item) {
        String tag = "by_item";
        if (!isEnable()) return emptyTagResolver(tag);
        if (item == null) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Component component = Component.text(item.getName());
            if (item.getHoverEvent() != null) {
                component = component.hoverEvent(item.getHoverEvent());
            }

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver killerTag(FPlayer receiver, Death killer) {
        String tag = "killer";
        if (!isEnable()) return emptyTagResolver(tag);

        FEntity entity = convertDeath(killer);
        if (entity == null) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            Localization.Message.Death message = resolveLocalization(receiver);

            Component component = messagePipeline.builder(entity, receiver, killer.isPlayer()
                            ? message.getKillerPlayer()
                            : message.getKillerEntity()
                    )
                    .build();

            return Tag.selfClosingInserting(component);
        });
    }

    private Item getItem(TranslatableComponent translatableComponent) {
        if (translatableComponent.args().size() < 3) return null;

        Component itemComp = translatableComponent.args().get(2);
        String itemName = switch (itemComp) {
            // format "chat.square_brackets"
            case TranslatableComponent transComp when transComp.key().equals("chat.square_brackets")
                    && !transComp.args().isEmpty() -> processItemComponent(transComp.args().get(0));
            // legacy format extra
            case TextComponent textComp when textComp.content().equals("[")
                    && !textComp.children().isEmpty() -> processItemComponent(textComp.children().get(0));
            default -> null;
        };

        if (itemName == null || itemName.isEmpty()) return null;

        Item item = new Item(itemName);
        item.setHoverEvent(HoverEvent.showText(Component.text(itemName).decorate(TextDecoration.ITALIC)));
        return item;
    }

    private String processItemComponent(Component component) {
        if (component instanceof TextComponent extraText && !extraText.children().isEmpty()) {
            Component itemText = extraText.children().get(0);
            if (itemText instanceof TextComponent itemTextComp) {
                return itemTextComp.content();
            }
        }

        return null;
    }

    private Death getDeath(TranslatableComponent translatableComponent, int index) {
        if (translatableComponent.args().size() < index + 1) return null;

        return switch (translatableComponent.args().get(index)) {
            case TranslatableComponent targetComponent -> {
                Death death = new Death(translatableComponent.key());
                death.setTargetName(targetComponent.key());
                death.setPlayer(false);

                String insertion = targetComponent.insertion();
                if (insertion != null && !insertion.isEmpty()) {
                    try {
                        death.setTargetUUID(UUID.fromString(insertion));
                    } catch (IllegalArgumentException e) {
                        // null
                    }
                }

                HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
                if (hoverEvent != null && hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity) {
                    death.setTargetUUID(showEntity.id());
                    death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
                    if (showEntity.type().value().equalsIgnoreCase("player")) {
                        death.setPlayer(true);
                    }
                } else {
                    death.setTargetType(targetComponent.key());
                }

                yield death;
            }
            case TextComponent targetComponent -> {
                String target = targetComponent.content();
                String insertion = targetComponent.insertion();

                Death death = null;

                if (!target.isEmpty()) {
                    death = new Death(translatableComponent.key());
                    death.setTargetName(target);
                    death.setPlayer(true);

                    if (insertion != null && !insertion.isEmpty()) {
                        try {
                            UUID uuid = UUID.fromString(insertion);
                            death.setTargetUUID(uuid);
                            death.setPlayer(false);
                            death.setTargetType(target);
                            yield death;
                        } catch (IllegalArgumentException e) {
                            // invalid UUID
                        }
                    }
                }

                HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
                if (hoverEvent == null) yield death;

                if (hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity) {
                    if (!(showEntity.name() instanceof TextComponent hoverComponent)) yield death;

                    if (death != null && !showEntity.type().value().equalsIgnoreCase("player")) {
                        death.setPlayer(false);
                        death.setTargetUUID(showEntity.id());
                        death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
                    }

                    if (!hoverComponent.children().isEmpty() && hoverComponent.children().get(0) instanceof TextComponent) {
                        death = new Death(translatableComponent.key());
                        StringBuilder targetNameBuilder = new StringBuilder();
                        for (int i = 0; i < hoverComponent.children().size(); i++) {
                            if (hoverComponent.children().get(i) instanceof TextComponent textComponent) {
                                targetNameBuilder.append(textComponent.content());
                            }
                        }

                        death.setTargetName(targetNameBuilder.toString());
                        death.setTargetUUID(showEntity.id());
                        death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
                        death.setPlayer(showEntity.type().value().equalsIgnoreCase("player"));
                    }
                }

                yield  death;
            }
            default -> null;
        };
    }
}
