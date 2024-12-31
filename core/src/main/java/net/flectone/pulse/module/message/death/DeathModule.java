package net.flectone.pulse.module.message.death;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.death.listener.DeathPacketListener;
import net.flectone.pulse.module.message.death.model.Death;
import net.flectone.pulse.module.message.death.model.Item;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.PacketEventsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.UUID;

@Singleton
public class DeathModule extends AbstractModuleMessage<Localization.Message.Death> {

    @Getter
    private final Message.Death message;
    private final Permission.Message.Death permission;

    private final ComponentUtil componentUtil;
    private final PacketEventsUtil packetEventsUtil;
    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;
    private final Gson gson;

    @Inject
    public DeathModule(FileManager fileManager,
                       ComponentUtil componentUtil,
                       PacketEventsUtil packetEventsUtil,
                       FPlayerManager fPlayerManager,
                       ListenerManager listenerManager,
                       IntegrationModule integrationModule,
                       Gson gson) {
        super(localization -> localization.getMessage().getDeath());

        this.componentUtil = componentUtil;
        this.packetEventsUtil = packetEventsUtil;
        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;
        this.gson = gson;

        message = fileManager.getMessage().getDeath();
        permission = fileManager.getPermission().getMessage().getDeath();

        addPredicate(integrationModule::isVanished);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerManager.register(DeathPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, Death death) {
        FEntity fTarget = convertDeath(death);
        if (fTarget == null) return;

        FPlayer fReceiver = fPlayerManager.get(receiver);

        if (!death.isPlayer()) {
            builder(fTarget)
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
                .tag(MessageTag.DEATH)
                .format(s -> s.getTypes().get(death.getKey()))
                .tagResolvers(fResolver -> new TagResolver[]{killerTag(fResolver, death.getKiller()), byItemTag(death.getItem())})
                .proxy(output -> output.writeUTF(gson.toJson(death)))
                .integration()
                .sound(getSound())
                .sendBuilt();

        if (!death.isPlayer()) return;

        Component component = componentUtil.builder(fTarget, fReceiver, resolveLocalization(fReceiver).getTypes().get(death.getKey()))
                .tagResolvers(killerTag(fReceiver, death.getKiller()), byItemTag(death.getItem()))
                .build();

        sendPersonalDeath(fReceiver, component);
    }

    @Sync
    public void sendPersonalDeath(FPlayer fPlayer, Component component) {
        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerDeathCombatEvent(fPlayerManager.getEntityId(fPlayer), null, component));
    }

    private FEntity convertDeath(Death death) {
        if (!death.isPlayer()) {
            return new FEntity(death.getTargetName(), death.getTargetUUID(), death.getTargetType() == null ? death.getTargetName() : death.getTargetType());
        }

        FPlayer fTarget = fPlayerManager.getOnline(death.getTargetName());
        if (fTarget.isUnknown()) return null;
        if (checkModulePredicates(fTarget)) return null;

        return fTarget;
    }

    public TagResolver byItemTag(Item item) {
        return TagResolver.resolver("by_item", (argumentQueue, context) -> {
            if (!isEnable()) return Tag.selfClosingInserting(Component.empty());
            if (item == null) return Tag.selfClosingInserting(Component.empty());

            Component component = Component.text(item.getName());
            if (item.getHoverEvent() != null) {
                component = component.hoverEvent(item.getHoverEvent());
            }

            return Tag.selfClosingInserting(component);
        });
    }

    public TagResolver killerTag(FPlayer receiver, Death killer) {
        return TagResolver.resolver("killer", (argumentQueue, context) -> {
            if (!isEnable()) return Tag.selfClosingInserting(Component.empty());

            FEntity entity = convertDeath(killer);
            if (entity == null) return Tag.inserting(Component.empty());

            return Tag.selfClosingInserting(componentUtil.builder(entity, receiver, "<display_name>").build());
        });
    }
}
