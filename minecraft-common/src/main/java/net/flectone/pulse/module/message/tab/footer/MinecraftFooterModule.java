package net.flectone.pulse.module.message.tab.footer;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.module.message.tab.footer.listener.MinecraftPulseFooterListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;

@Singleton
public class MinecraftFooterModule extends FooterModuleImpl {

    private final ListenerRegistry listenerRegistry;
    private final MinecraftPacketSender packetSender;

    @Inject
    public MinecraftFooterModule(FileFacade fileFacade,
                                 TaskScheduler taskScheduler,
                                 MessageDispatcher messageDispatcher,
                                 ModuleController moduleController,
                                 RandomGenerator randomGenerator,
                                 SocialService socialService,
                                 ListenerRegistry listenerRegistry,
                                 MinecraftPacketSender packetSender) {
        super(fileFacade, taskScheduler, messageDispatcher, moduleController, randomGenerator, socialService);

        this.listenerRegistry = listenerRegistry;
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPulseFooterListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Destination.Type destinationType = config().destination().type();
        if (destinationType == Destination.Type.TAB_HEADER || destinationType == Destination.Type.TAB_FOOTER) {
            packetSender.send(new WrapperPlayServerPlayerListHeaderAndFooter(Component.empty(), Component.empty()));
        }
    }

}
