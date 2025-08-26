package net.flectone.pulse.module.message.tab.footer;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Destination;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.module.message.tab.footer.listener.FooterPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
public class FooterModule extends AbstractModuleListLocalization<Localization.Message.Tab.Footer> {

    private final Message.Tab.Footer message;
    private final Permission.Message.Tab.Footer permission;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final PacketSender packetSender;

    @Inject
    public FooterModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        TaskScheduler taskScheduler,
                        ListenerRegistry listenerRegistry,
                        PacketSender packetSender) {
        super(module -> module.getMessage().getTab().getFooter(), MessageType.FOOTER);

        this.message = fileResolver.getMessage().getTab().getFooter();
        this.permission = fileResolver.getPermission().getMessage().getTab().getFooter();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(FooterPulseListener.class);
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
        if (isModuleDisabledFor(fPlayer)) return;

        String format = getNextMessage(fPlayer, message.isRandom());
        if (StringUtils.isEmpty(format)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(format)
                .destination(message.getDestination())
                .build()
        );
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
