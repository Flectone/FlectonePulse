package net.flectone.pulse.module.message.tab.footer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

import java.util.List;

@Singleton
public class FooterModule extends AbstractModuleListMessage<Localization.Message.Tab.Footer> {

    private final Message.Tab.Footer message;
    private final Permission.Message.Tab.Footer permission;

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;

    @Inject
    public FooterModule(FileResolver fileResolver,
                        FPlayerService fPlayerService,
                        TaskScheduler taskScheduler) {
        super(module -> module.getMessage().getTab().getFooter());

        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;

        message = fileResolver.getMessage().getTab().getFooter();
        permission = fileResolver.getPermission().getMessage().getTab().getFooter();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::send), ticker.getPeriod());
        }
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = getNextMessage(fPlayer, message.isRandom());
        if (format == null) return;

        builder(fPlayer)
                .format(format)
                .destination(message.getDestination())
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
