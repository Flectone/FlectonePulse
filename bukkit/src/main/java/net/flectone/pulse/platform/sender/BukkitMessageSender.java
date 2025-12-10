package net.flectone.pulse.platform.sender;

import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.serializer.PacketSerializer;
import net.flectone.pulse.util.PaperItemStackUtil;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

@Singleton
public class BukkitMessageSender extends MessageSender {

    private final IntegrationModule integrationModule;
    private final FileResolver fileResolver;
    private final PaperItemStackUtil paperItemStackUtil;

    @Inject
    public BukkitMessageSender(PacketSerializer packetSerializer,
                               TaskScheduler taskScheduler,
                               PacketSender packetSender,
                               PacketProvider packetProvider,
                               IntegrationModule integrationModule,
                               FileResolver fileResolver,
                               PaperItemStackUtil paperItemStackUtil,
                               FLogger fLogger) {
        super(packetSerializer, taskScheduler, packetSender, packetProvider, integrationModule, fLogger);

        this.integrationModule = integrationModule;
        this.fileResolver = fileResolver;
        this.paperItemStackUtil = paperItemStackUtil;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component, boolean silent) {
        // use default sendMessage
        if (!fileResolver.getMessage().getFormat().getReplacement().isUsePaperDataComponents()) {
            super.sendMessage(fPlayer, component, silent);
            return;
        }

        // replace item mark
        if (fPlayer.isConsole() || silent) {
            super.sendMessage(fPlayer, replaceItemMarkToEmpty(component), silent);
            return;
        }

        // integration with InteractiveChat
        if (integrationModule.sendMessageWithInteractiveChat(fPlayer, replaceItemMarkToEmpty(component))) return;

        paperItemStackUtil.sendMessage(fPlayer, AdventureSerializer.serializer().gson().serialize(component));
    }

    private Component replaceItemMarkToEmpty(Component component) {
        return component.replaceText(TextReplacementConfig.builder()
                .match(PaperItemStackUtil.FLECTONEPULSE_ITEM_MARK_PATTERN)
                .replacement((matchResult, builder) -> Component.text(""))
                .build()
        );
    }
}
