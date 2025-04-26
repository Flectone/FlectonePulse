package net.flectone.pulse.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Toast;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.PacketSerializer;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class FabricMessageSender extends MessageSender {

    @Inject
    public FabricMessageSender(TaskScheduler taskScheduler,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PacketSerializer packetSerializer,
                               PacketSender packetSender,
                               PacketProvider packetProvider,
                               FLogger fLogger) {
        super(taskScheduler, platformPlayerAdapter, packetSerializer, packetSender, packetProvider, fLogger);
    }

    @Override
    public void sendToast(FPlayer fPlayer, Component title, Toast toast) {

    }

}
