package net.flectone.pulse.module.message.tab.header;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.module.message.tab.header.listener.HeaderPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
public class HeaderModule extends AbstractModuleListLocalization<Localization.Message.Tab.Header> {

    private final FileResolver fileResolver;
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
        super(MessageType.HEADER);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        fPlayerService.getPlatformFPlayers().forEach(this::send);

        registerModulePermission(permission());

        Ticker ticker = config().getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(HeaderPulseListener.class);
    }

    @Override
    public void onDisable() {
        // clear tab
        Destination.Type destinationType = config().getDestination().getType();
        if (destinationType == Destination.Type.TAB_HEADER || destinationType == Destination.Type.TAB_FOOTER) {
            packetSender.send(new WrapperPlayServerPlayerListHeaderAndFooter(Component.empty(), Component.empty()));
        }
    }

    @Override
    public Message.Tab.Header config() {
        return fileResolver.getMessage().getTab().getHeader();
    }

    @Override
    public Permission.Message.Tab.Header permission() {
        return fileResolver.getPermission().getMessage().getTab().getHeader();
    }

    @Override
    public Localization.Message.Tab.Header localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getTab().getHeader();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(localization(fPlayer).getLists());
    }

    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        String format = getNextMessage(fPlayer, config().isRandom());
        if (StringUtils.isEmpty(format)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(format)
                .destination(config().getDestination())
                .build()
        );
    }
}
