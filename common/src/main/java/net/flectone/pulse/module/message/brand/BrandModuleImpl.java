package net.flectone.pulse.module.message.brand;

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
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.message.brand.listener.PulseBrandListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BrandModuleImpl implements BrandModule {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
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

        listenerRegistry.register(PulseBrandListener.class);
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
    public Localization.Message.Brand localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().brand();
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

    @Override
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
}
