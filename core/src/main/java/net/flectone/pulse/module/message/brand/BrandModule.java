package net.flectone.pulse.module.message.brand;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.module.message.brand.listener.BrandPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
public class BrandModule extends AbstractModuleListLocalization<Localization.Message.Brand> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BrandModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       TaskScheduler taskScheduler,
                       ListenerRegistry listenerRegistry) {
        super(MessageType.BRAND);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        Ticker ticker = config().getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(BrandPulseListener.class);
    }

    @Override
    public Message.Brand config() {
        return fileResolver.getMessage().getBrand();
    }

    @Override
    public Permission.Message.Brand permission() {
        return fileResolver.getPermission().getMessage().getBrand();
    }

    @Override
    public Localization.Message.Brand localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getBrand();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return localization(fPlayer).getValues();
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
