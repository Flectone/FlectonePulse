package net.flectone.pulse.module.message.tab.header;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
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
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HeaderModule extends AbstractModuleListLocalization<Localization.Message.Tab.Header> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final PacketSender packetSender;

    @Override
    public void onEnable() {
        fPlayerService.getPlatformFPlayers().forEach(this::send);

        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(HeaderPulseListener.class);
    }

    @Override
    public void onDisable() {
        // clear tab
        Destination.Type destinationType = config().destination().type();
        if (destinationType == Destination.Type.TAB_HEADER || destinationType == Destination.Type.TAB_FOOTER) {
            packetSender.send(new WrapperPlayServerPlayerListHeaderAndFooter(Component.empty(), Component.empty()));
        }
    }

    @Override
    public MessageType messageType() {
        return MessageType.HEADER;
    }

    @Override
    public Message.Tab.Header config() {
        return fileFacade.message().tab().header();
    }

    @Override
    public Permission.Message.Tab.Header permission() {
        return fileFacade.permission().message().tab().header();
    }

    @Override
    public Localization.Message.Tab.Header localization(FEntity sender) {
        return fileFacade.localization(sender).message().tab().header();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(localization(fPlayer).lists());
    }

    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        String format = getNextMessage(fPlayer, config().random());
        if (StringUtils.isEmpty(format)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .format(format)
                .destination(config().destination())
                .build()
        );
    }
}
