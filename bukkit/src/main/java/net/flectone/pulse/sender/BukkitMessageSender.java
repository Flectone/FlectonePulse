package net.flectone.pulse.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.PacketSerializer;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class BukkitMessageSender extends MessageSender {

    private final BukkitIntegrationModule integrationModule;

    @Inject
    public BukkitMessageSender(TaskScheduler taskScheduler,
                               BukkitIntegrationModule integrationModule,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PacketSerializer packetSerializer,
                               PacketSender packetSender,
                               PacketProvider packetProvider,
                               FLogger fLogger) {
        super(taskScheduler, platformPlayerAdapter, packetSerializer, packetSender, packetProvider, fLogger);

        this.integrationModule = integrationModule;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component) {
        if (integrationModule.sendMessageWithInteractiveChat(fPlayer, component)) return;

        super.sendMessage(fPlayer, component);
    }
}
