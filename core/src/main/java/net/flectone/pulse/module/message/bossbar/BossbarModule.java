package net.flectone.pulse.module.message.bossbar;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.bossbar.listener.BossbarPacketListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BossbarModule extends AbstractModuleLocalization<Localization.Message.Bossbar> {

    private static final String RAIDERS_REMAINING_KEY = "event.minecraft.raid.raiders_remaining";
    private static final String RAIDERS_PLACEHOLDER = "<raiders>";

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(BossbarPacketListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.BOSSBAR;
    }

    @Override
    public Message.Bossbar config() {
        return fileFacade.message().bossbar();
    }

    @Override
    public Permission.Message.Bossbar permission() {
        return fileFacade.permission().message().bossbar();
    }

    @Override
    public Localization.Message.Bossbar localization(FEntity sender) {
        return fileFacade.localization(sender).message().bossbar();
    }

    @Async
    public void send(UUID playerUUID, UUID bossbarUUID, String translationKey, boolean announce, Component oldTitle) {
        FPlayer fPlayer = fPlayerService.getFPlayer(playerUUID);
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

        Component title = messagePipeline.builder(fPlayer, message)
                .tagResolvers(raidersTag(fPlayer, raiders))
                .build();

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
                    .sound(messageAnnounce.sound())
                    .build()
            );
        }
    }

    private TagResolver raidersTag(FPlayer fPlayer, String raiders) {
        String tag = "raiders";
        if (StringUtils.isEmpty(raiders)) return MessagePipeline.ReplacementTag.empty(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String raidersRemaining = localization(fPlayer).types().get(RAIDERS_REMAINING_KEY);
            if (StringUtils.isEmpty(raidersRemaining)) return Tag.selfClosingInserting(Component.empty());

            return Tag.selfClosingInserting(messagePipeline
                    .builder(fPlayer, Strings.CS.replace(raidersRemaining, RAIDERS_PLACEHOLDER, raiders))
                    .build()
            );
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
