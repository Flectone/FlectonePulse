package net.flectone.pulse.module.message.tab.header;

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

import java.util.List;

@Singleton
public class HeaderModule extends AbstractModuleListMessage<Localization.Message.Tab.Header> {

    private final Message.Tab.Header message;
    private final Permission.Message.Tab.Header permission;

    private final TaskScheduler taskScheduler;
    private final IntegrationModule integrationModule;

    @Inject
    public HeaderModule(FileManager fileManager,
                        TaskScheduler taskScheduler,
                        IntegrationModule integrationModule) {
        super(module -> module.getMessage().getTab().getHeader());

        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;

        message = fileManager.getMessage().getTab().getHeader();
        permission = fileManager.getPermission().getMessage().getTab().getHeader();
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

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = getNextMessage(fPlayer, message.isRandom());
        if (format == null) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .format(format)
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
