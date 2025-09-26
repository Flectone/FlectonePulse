package net.flectone.pulse.module.message.death;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDeathCombatEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.config.localization.Localization;
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
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import static net.flectone.pulse.execution.pipeline.MessagePipeline.ReplacementTag.empty;

@Singleton
public class DeathModule extends AbstractModuleLocalization<Localization.Message.Death> implements PulseListener {

    private final FileResolver fileResolver;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;

    @Inject
    public DeathModule(FileResolver fileResolver,
                       MessagePipeline messagePipeline,
                       PacketSender packetSender,
                       FPlayerService fPlayerService,
                       ListenerRegistry listenerRegistry,
                       IntegrationModule integrationModule) {
        super(MessageType.DEATH);

        this.fileResolver = fileResolver;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(DeathPacketListener.class);
        listenerRegistry.register(DeathPulseListener.class);
    }

    @Override
    public Message.Death config() {
        return fileResolver.getMessage().getDeath();
    }

    @Override
    public Permission.Message.Death permission() {
        return fileResolver.getPermission().getMessage().getDeath();
    }

    @Override
    public Localization.Message.Death localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getDeath();
    }

    @Async
    public void send(FPlayer fReceiver, MinecraftTranslationKey translationKey, Death death) {
        if (!(death.getTarget() instanceof FPlayer fTarget)) {
            sendMessage(DeathMetadata.<Localization.Message.Death>builder()
                    .sender(death.getTarget())
                    .filterPlayer(fReceiver)
                    .format(localization -> localization.getTypes().get(translationKey.toString()))
                    .death(death)
                    .translationKey(translationKey)
                    .destination(config().getDestination())
                    .sound(getModuleSound())
                    .filter(fPlayer -> integrationModule.canSeeVanished(death.getTarget(), fPlayer))
                    .tagResolvers(fResolver -> new TagResolver[]{
                            targetTag(fReceiver, death.getTarget()),
                            targetTag("killer", fResolver, death.getKiller()),
                            killerItemTag(death.getKillerItem())
                    })
                    .build()
            );

            return;
        }

        // send message only when fTarget is fReceiver
        // because death message is sent to each player and they will be duplicated if it is not checked
        if (!fTarget.equals(fReceiver)) return;

        sendMessage(DeathMetadata.<Localization.Message.Death>builder()
                .sender(fReceiver)
                .format(localization -> localization.getTypes().get(translationKey.toString()))
                .death(death)
                .translationKey(translationKey)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .filter(fPlayer -> integrationModule.canSeeVanished(fTarget, fPlayer))
                .tagResolvers(fResolver -> new TagResolver[]{
                        targetTag(fReceiver, death.getTarget()),
                        targetTag("killer", fResolver, death.getKiller()),
                        killerItemTag(death.getKillerItem())
                })
                .proxy(dataOutputStream -> {
                    dataOutputStream.writeUTF(translationKey.toString());
                    dataOutputStream.writeAsJson(death);
                })
                .integration()
                .build()
        );

        // personal death screen message
        if (fTarget.isSetting(MessageType.DEATH)) {
            String format = localization(fReceiver).getTypes().get(translationKey.toString());
            Component component = messagePipeline.builder(fReceiver, format)
                    .tagResolvers(
                            targetTag(fReceiver, death.getTarget()),
                            targetTag("killer", fReceiver, death.getKiller()),
                            killerItemTag(death.getKillerItem())
                    )
                    .build();

            sendPersonalDeath(fReceiver, component);
        }
    }

    @Sync
    public void sendPersonalDeath(FPlayer fPlayer, Component component) {
        packetSender.send(fPlayer, new WrapperPlayServerDeathCombatEvent(fPlayerService.getEntityId(fPlayer), null, component));
    }

    public TagResolver killerItemTag(Component itemName) {
        String tag = "killer_item";
        if (!isEnable() || itemName == null) return empty(tag);

        Component itemComponentWithHover = itemName.hoverEvent(HoverEvent.showText(itemName));

        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.selfClosingInserting(itemComponentWithHover)
        );
    }
}
