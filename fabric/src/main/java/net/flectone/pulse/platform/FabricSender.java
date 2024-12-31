package net.flectone.pulse.platform;

import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FPlayer;
import net.kyori.adventure.text.Component;

@Singleton
public class FabricSender extends PlatformSender {

//    private volatile FabricServerAudiences adventure;

    public FabricSender() {

    }

    public void init() {
//        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.adventure = FabricServerAudiences.of(server));
//        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.adventure = null);
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component) {

    }

    @Override
    public void sendActionBar(FPlayer fPlayer, Component component) {

    }

    @Override
    public void sendPlayerListFooter(FPlayer fPlayer, Component component) {

    }

    @Override
    public void sendPlayerListHeader(FPlayer fPlayer, Component component) {

    }
}
