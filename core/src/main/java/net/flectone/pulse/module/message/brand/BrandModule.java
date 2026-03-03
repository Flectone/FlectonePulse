package net.flectone.pulse.module.message.brand;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.module.message.brand.listener.BrandPulseListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BrandModule implements ModuleListLocalization<Localization.Message.Brand> {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final RandomUtil randomUtil;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::send, ticker.period());
        }

        listenerRegistry.register(BrandPulseListener.class);
    }

    @Override
    public void onDisable() {
        messageIndexMap.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_BRAND;
    }

    @Override
    public Message.Brand config() {
        return fileFacade.message().brand();
    }

    @Override
    public Permission.Message.Brand permission() {
        return fileFacade.permission().message().brand();
    }

    @Override
    public Localization.Message.Brand localization(FEntity sender) {
        return fileFacade.localization(sender).message().brand();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return localization(fPlayer).values();
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

        messageDispatcher.dispatch(this, EventMetadata.<Localization.Message.Brand>builder()
                .sender(fPlayer)
                .format(format)
                .destination(config().destination())
                .build()
        );
    }
}
