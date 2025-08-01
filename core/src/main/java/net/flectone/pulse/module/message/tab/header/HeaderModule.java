package net.flectone.pulse.module.message.tab.header;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.Destination;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.module.message.tab.header.listener.HeaderPulseListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.List;

@Singleton
public class HeaderModule extends AbstractModuleListLocalization<Localization.Message.Tab.Header> {

    private final Message.Tab.Header message;
    private final Permission.Message.Tab.Header permission;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final PacketSender packetSender;

    @Inject
    public HeaderModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        TaskScheduler taskScheduler,
                        ListenerRegistry listenerRegistry,
                        PacketSender packetSender) {
        super(module -> module.getMessage().getTab().getHeader());

        this.message = fileResolver.getMessage().getTab().getHeader();
        this.permission = fileResolver.getPermission().getMessage().getTab().getHeader();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        fPlayerService.getPlatformFPlayers().forEach(this::send);

        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(HeaderPulseListener.class);
    }

    @Override
    public void onDisable() {
        // clear tab
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
                .destination(message.getDestination())
                .format(format)
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
