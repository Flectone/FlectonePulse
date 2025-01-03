package net.flectone.pulse.module.message.tab.header;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.message.tab.header.ticker.HeaderTicker;

@Singleton
public class HeaderModule extends AbstractModuleListMessage<Localization.Message.Tab.Header> {

    private final Message.Tab.Header message;
    private final Permission.Message.Tab.Header permission;

    @Inject
    private HeaderTicker headerTicker;

    @Inject
    public HeaderModule(FileManager fileManager) {
        super(module -> module.getMessage().getTab().getHeader());

        message = fileManager.getMessage().getTab().getHeader();
        permission = fileManager.getPermission().getMessage().getTab().getHeader();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            headerTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = nextListMessage(fPlayer, message.isRandom(), resolveLocalization(fPlayer).getLists());
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
}
