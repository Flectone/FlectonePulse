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

        if (!(translatableComponent.args().get(2) instanceof TranslatableComponent itemComponent)) return null;

        if (itemComponent.args().isEmpty()) return null;
        if (!(itemComponent.args().get(0) instanceof TextComponent itemTextComponent)) return null;

        if (itemTextComponent.children().isEmpty()) return null;
        if (!(itemTextComponent.children().get(0) instanceof TextComponent itemTextTextComponent)) return null;

        Item item;

        if (itemTextTextComponent.content().isEmpty()) {
            if (itemTextTextComponent.children().isEmpty()) return null;
            if (!(itemTextTextComponent.children().get(0) instanceof TextComponent itemTextTextTextComponent)) return null;

            item = new Item(itemTextTextTextComponent.content());

            // wait for fix from PacketEvents
            // item.setHoverEvent(itemComponent.hoverEvent());
        } else {
            item = new Item(itemTextTextComponent.content());

            // wait for fix from PacketEvents
            // item.setHoverEvent(itemComponent.hoverEvent());
        }

        item.setHoverEvent(HoverEvent.showText(Component.text(item.getName()).decorate(TextDecoration.ITALIC)));

        return item;
    }

    private Death getDeath(TranslatableComponent translatableComponent, int index) {
        if (translatableComponent.args().size() < index + 1) return null;

        if (translatableComponent.args().get(index) instanceof TranslatableComponent targetComponent) {
            HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
            if (hoverEvent == null) return null;
            if (!(hoverEvent.value() instanceof HoverEvent.ShowEntity showEntity)) return null;

            Death death = new Death(translatableComponent.key());
            death.setTargetName(targetComponent.key());
            death.setTargetUUID(showEntity.id());
            return death;
        }

        if (translatableComponent.args().get(index) instanceof TextComponent targetComponent) {
            String target = targetComponent.content();

            Death death = null;

            if (!target.isEmpty()) {
                death = new Death(translatableComponent.key());
                death.setTargetName(target);
                death.setPlayer(true);
            }

            HoverEvent<?> hoverEvent = targetComponent.hoverEvent();
            if (hoverEvent == null) return death;

            HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity) hoverEvent.value();
            if (!(showEntity.name() instanceof TextComponent hoverComponent)) return death;
            if (death != null && !showEntity.type().value().equalsIgnoreCase("player")) {
                death.setPlayer(false);
                death.setTargetUUID(showEntity.id());
                death.setTargetType(entityUtil.resolveEntityTranslationKey(showEntity.type().key().value()));
            }

            if (hoverComponent.children().isEmpty()) return death;
            if (!(hoverComponent.children().get(0) instanceof TextComponent)) return death;

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
            return death;
        }

        return null;
    }
}
