package net.flectone.pulse.module.message.tab.footer;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.List;

@Singleton
public class FooterModule extends AbstractModuleListMessage<Localization.Message.Tab.Footer> {

    private final Message.Tab.Footer message;
    private final Permission.Message.Tab.Footer permission;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final EventProcessRegistry eventProcessRegistry;
    private final PacketSender packetSender;

    @Inject
    public FooterModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        TaskScheduler taskScheduler,
                        EventProcessRegistry eventProcessRegistry,
                        PacketSender packetSender) {
        super(module -> module.getMessage().getTab().getFooter());

        this.message = fileResolver.getMessage().getTab().getFooter();
        this.permission = fileResolver.getPermission().getMessage().getTab().getFooter();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.eventProcessRegistry = eventProcessRegistry;
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::send), ticker.getPeriod());
        }

        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_LOAD, this::send);
    }

    @Override
    public void onDisable() {
        Destination.Type destinationType = message.getDestination().getType();
        if (destinationType == Destination.Type.TAB_HEADER || destinationType == Destination.Type.TAB_FOOTER) {
            packetSender.send(new WrapperPlayServerPlayerListHeaderAndFooter(Component.empty(), Component.empty()));
        }
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = getNextMessage(fPlayer, message.isRandom());
        if (format == null) return;

        builder(fPlayer)
                .format(format)
                .destination(message.getDestination())
                .sendBuilt();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(resolveLocalization(fPlayer).getLists());
    }
}
