package net.flectone.pulse.module.message.tab.footer;

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
import net.flectone.pulse.module.message.tab.footer.listener.FooterPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FooterModule extends AbstractModuleListLocalization<Localization.Message.Tab.Footer> {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final PacketSender packetSender;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::send, ticker.period());
        }

        listenerRegistry.register(FooterPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // clear tab
        Destination.Type destinationType = config().destination().type();
        if (destinationType == Destination.Type.TAB_HEADER || destinationType == Destination.Type.TAB_FOOTER) {
            packetSender.send(new WrapperPlayServerPlayerListHeaderAndFooter(Component.empty(), Component.empty()));
        }
    }

    @Override
    public MessageType messageType() {
        return MessageType.FOOTER;
    }

    @Override
    public Message.Tab.Footer config() {
        return fileFacade.message().tab().footer();
    }

    @Override
    public Permission.Message.Tab.Footer permission() {
        return fileFacade.permission().message().tab().footer();
    }

    @Override
    public Localization.Message.Tab.Footer localization(FEntity sender) {
        return fileFacade.localization(sender).message().tab().footer();
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
