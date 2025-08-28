package net.flectone.pulse.module.message.death;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.death.listener.DeathPacketListener;
import net.flectone.pulse.module.message.death.listener.DeathPulseListener;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.DeathMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class DeathModule extends AbstractModuleLocalization<Localization.Message.Death> implements PulseListener {

    private final Message.Death message;
    private final Permission.Message.Death permission;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Inject
    public DeathModule(FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       PacketSender packetSender,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry,
                       IntegrationModule integrationModule) {
        super(localization -> localization.getMessage().getDeath(), MessageType.DEATH);

        this.message = fileResolver.getMessage().getDeath();
        this.permission = fileResolver.getPermission().getMessage().getDeath();
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DeathPacketListener.class);
        listenerRegistry.register(DeathPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fReceiver, Death death) {
        FEntity fTarget = convertDeath(death);
        if (fTarget == null) return;

        if (!death.isPlayer()) {
            sendMessage(DeathMetadata.<Localization.Message.Death>builder()
                    .sender(fTarget)
                    .filterPlayer(fReceiver)
                    .format(s -> s.getTypes().get(death.getKey()))
                    .death(death)
                    .destination(message.getDestination())
                    .sound(getModuleSound())
                    .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.DEATH)
                            && integrationModule.canSeeVanished(fTarget, fPlayer)
                    )
                    .tagResolvers(fResolver -> new TagResolver[]{
                            killerTag(fResolver, death.getKiller()),
                            byItemTag(death.getItem())
                    })
                    .build()
            );

            return;
        }

        if (!fTarget.equals(fReceiver)) return;

        sendMessage(DeathMetadata.<Localization.Message.Death>builder()
                .sender(fTarget)
                .format(s -> s.getTypes().get(death.getKey()))
                .death(death)
                .range(message.getRange())
                .destination(message.getDestination())
                .sound(getModuleSound())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.DEATH)
                        && integrationModule.canSeeVanished(fTarget, fPlayer)
                )
                .tagResolvers(fResolver -> new TagResolver[]{
                        killerTag(fResolver, death.getKiller()),
                        byItemTag(death.getItem())
                })
                .proxy(dataOutputStream -> dataOutputStream.writeAsJson(death))
                .integration()
                .build()
        );

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
        if (isModuleDisabledFor(fTarget)) return null;

        return fTarget;
    }

    public TagResolver byItemTag(String itemName) {
        String tag = "by_item";
        if (!isEnable()) return empty(tag);
        if (StringUtils.isEmpty(itemName)) return empty(tag);

        Component itemComponent;
        try {
            itemComponent = miniMessage.deserialize(itemName);
        } catch (Exception ignored) {
            itemComponent = Component.text(itemName);
        }

        Component itemComponentWithHover = itemComponent.hoverEvent(HoverEvent.showText(
                itemComponent.decorate(TextDecoration.ITALIC))
        );

        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.selfClosingInserting(itemComponentWithHover)
        );
    }

    public TagResolver killerTag(FPlayer receiver, Death killer) {
        String tag = "killer";
        if (!isEnable()) return empty(tag);

        FEntity entity = convertDeath(killer);
        if (entity == null) return empty(tag);

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
}
