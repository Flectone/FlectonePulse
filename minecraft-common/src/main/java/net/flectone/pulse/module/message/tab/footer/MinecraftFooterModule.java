package net.flectone.pulse.module.message.tab.footer;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.dispatcher.MessageDispatcher;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.module.message.tab.footer.listener.MinecraftPulseFooterListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftFooterModule implements ModuleListLocalization {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final MinecraftPacketSender packetSender;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final RandomGenerator randomUtil;
    private final SocialService socialService;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::send, ticker.period());
        }

        listenerRegistry.register(MinecraftPulseFooterListener.class);
    }

    @Override
    public void onDisable() {
        messageIndexMap.clear();

        // clear tab
        Destination.Type destinationType = config().destination().type();
        if (destinationType == Destination.Type.TAB_HEADER || destinationType == Destination.Type.TAB_FOOTER) {
            packetSender.send(new WrapperPlayServerPlayerListHeaderAndFooter(Component.empty(), Component.empty()));
        }
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_TAB_FOOTER;
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
    public Localization.Message.Tab.Footer localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().tab().footer();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(localization(fPlayer).lists());
    }

    @Override
    public int getPlayerIndexOrDefault(int id, int defaultIndex) {
        return messageIndexMap.getOrDefault(id, defaultIndex);
    }

    @Override
    public int nextInt(int start, int end) {
        return randomUtil.nextInt(start, end);
    }

    @Override
    public void savePlayerIndex(int id, int playerIndex) {
        messageIndexMap.put(id, playerIndex);
    }

    public void send(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        String format = getNextMessage(fPlayer, config().random());
        if (StringUtils.isEmpty(format)) return;

        messageDispatcher.dispatch(this, EventMetadata.builder()
                .destination(config().destination())
                .messageContext(fResolver -> MessageContext.builder()
                        .sender(fPlayer)
                        .receiver(fResolver)
                        .message(format)
                        .build()
                )
                .build()
        );
    }

    public boolean isDisabledFor(FPlayer fPlayer) {
        return moduleController.isDisabledFor(this, fPlayer);
    }

}
