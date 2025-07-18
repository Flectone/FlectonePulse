package net.flectone.pulse.module.message.brand;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

import java.util.List;

@Singleton
public class BrandModule extends AbstractModuleListMessage<Localization.Message.Brand> {

    private final Message.Brand message;
    private final Permission.Message.Brand permission;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public BrandModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       TaskScheduler taskScheduler,
                       EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getBrand());

        this.message = fileResolver.getMessage().getBrand();
        this.permission = fileResolver.getPermission().getMessage().getBrand();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.eventProcessRegistry = eventProcessRegistry;
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
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = getNextMessage(fPlayer, this.message.isRandom());
        if (format == null) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .format(format)
                .sendBuilt();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return resolveLocalization(fPlayer).getValues();
    }
}
