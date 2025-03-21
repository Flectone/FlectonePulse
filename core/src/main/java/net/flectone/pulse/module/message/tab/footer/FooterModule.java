package net.flectone.pulse.module.message.tab.footer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;

@Singleton
public class FooterModule extends AbstractModuleListMessage<Localization.Message.Tab.Footer> {

    private final Message.Tab.Footer message;
    private final Permission.Message.Tab.Footer permission;

    private final TaskScheduler taskScheduler;
    private final IntegrationModule integrationModule;

    @Inject
    public FooterModule(FileManager fileManager,
                        TaskScheduler taskScheduler,
                        IntegrationModule integrationModule) {
        super(module -> module.getMessage().getTab().getFooter());

        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;

        message = fileManager.getMessage().getTab().getFooter();
        permission = fileManager.getPermission().getMessage().getTab().getFooter();
    }

    @Override
    public void reload() {
        if (integrationModule.isOtherScoreboardEnabled()) return;

        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTicker(this::send, ticker.getPeriod());
        }
    }

    @Inject
    private FLogger fLogger;

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        fLogger.warning("3");

        String format = getNextMessage(fPlayer, message.isRandom());
        if (format == null) return;

        builder(fPlayer)
                .format(format)
                .destination(message.getDestination())
                .sendBuilt();
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(resolveLocalization(fPlayer).getLists());
    }
}
