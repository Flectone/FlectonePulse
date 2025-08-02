package net.flectone.pulse.module.message.death;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.death.listener.DeathPacketListener;
import net.flectone.pulse.module.message.death.listener.DeathPulseListener;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.Item;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

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
    private final Gson gson;

    @Inject
    public DeathModule(FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       PacketSender packetSender,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry,
                       IntegrationModule integrationModule,
                       Gson gson) {
        super(localization -> localization.getMessage().getDeath());

        this.message = fileResolver.getMessage().getDeath();
        this.permission = fileResolver.getPermission().getMessage().getDeath();
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;
        this.gson = gson;
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
                .tag(MessageType.DEATH)
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
        if (!isEnable()) return empty(tag);
        if (item == null) return empty(tag);

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
