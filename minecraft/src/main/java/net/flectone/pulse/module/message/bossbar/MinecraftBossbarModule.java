package net.flectone.pulse.module.message.bossbar;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.bossbar.listener.BossbarPacketListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.type.tuple.Pair;

import java.util.UUID;

@Singleton
public class MinecraftBossbarModule extends BossbarModule {

    private static final String RAIDERS_REMAINING_KEY = "event.minecraft.raid.raiders_remaining";
    private static final String RAIDERS_PLACEHOLDER = "<raiders>";

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MinecraftBossbarModule(FileFacade fileFacade,
                                  FPlayerService fPlayerService,
                                  ListenerRegistry listenerRegistry,
                                  MessagePipeline messagePipeline,
                                  PacketSender packetSender,
                                  TaskScheduler taskScheduler) {
        super(fileFacade);

        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BossbarPacketListener.class);
    }

    public void send(UUID playerUUID, UUID bossbarUUID, String translationKey, boolean announce, Component oldTitle) {
        FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);
        taskScheduler.runRegion(fPlayer, () -> {
            if (isModuleDisabledFor(fPlayer)) return;
            if (!fPlayer.isSetting(MessageType.BOSSBAR)) return;

            String message = localization(fPlayer).types().get(translationKey);
            if (StringUtils.isEmpty(message)) return;

            // it looks strange, but this is the only way to make normal color and message support
            // remaining_raiders fits into other messages under certain conditions,
            // so we need to add it here as well
            String raiders = extractRemainingRaiders(oldTitle);
            if (StringUtils.isNotEmpty(raiders)) {
                message = message + RAIDERS_PLACEHOLDER;
            }

            MessageContext messageContext = messagePipeline.createContext(fPlayer, message)
                    .addTagResolver(raidersTag(fPlayer, raiders));

            Component title = messagePipeline.build(messageContext);
            if (title.equals(oldTitle)) return;

            WrapperPlayServerBossBar wrapper = new WrapperPlayServerBossBar(bossbarUUID, WrapperPlayServerBossBar.Action.UPDATE_TITLE);
            wrapper.setTitle(title);

            packetSender.send(fPlayer, wrapper);

            Message.Bossbar.Announce messageAnnounce = config().announce().get(translationKey);
            if (announce && messageAnnounce != null) {
                sendMessage(metadataBuilder()
                        .sender(FPlayer.UNKNOWN)
                        .format(localization -> Strings.CS.replace(
                                StringUtils.defaultString(localization.announce().get(translationKey)),
                                RAIDERS_PLACEHOLDER,
                                raiders
                        ))
                        .filterPlayer(fPlayer)
                        .destination(messageAnnounce.destination())
                        .sound(Pair.of(messageAnnounce.sound(), permission().types().get(translationKey)))
                        .build()
                );
            }
        });
    }

    private TagResolver raidersTag(FPlayer fPlayer, String raiders) {
        String tag = "raiders";
        if (StringUtils.isEmpty(raiders)) return MessagePipeline.ReplacementTag.empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String raidersRemaining = localization(fPlayer).types().get(RAIDERS_REMAINING_KEY);
            if (StringUtils.isEmpty(raidersRemaining)) return Tag.selfClosingInserting(Component.empty());

            String replaced = Strings.CS.replace(raidersRemaining, RAIDERS_PLACEHOLDER, raiders);
            MessageContext tagContext = messagePipeline.createContext(fPlayer, replaced);
            return Tag.selfClosingInserting(messagePipeline.build(tagContext));
        });
    }

    private String extractRemainingRaiders(Component oldTitle) {
        if (oldTitle.children().isEmpty()) return "";

        for (Component child : oldTitle.children()) {
            if (child instanceof TranslatableComponent remaining
                    && remaining.key().equals(RAIDERS_REMAINING_KEY)
                    && !remaining.arguments().isEmpty()) {

                return String.valueOf(remaining.arguments().getFirst().value());
            }
        }

        return "";
    }

}
