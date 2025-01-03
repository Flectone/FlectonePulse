package net.flectone.pulse.module.message.tab.footer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.message.tab.footer.ticker.FooterTicker;

@Singleton
public class FooterModule extends AbstractModuleListMessage<Localization.Message.Tab.Footer> {

    private final Message.Tab.Footer message;
    private final Permission.Message.Tab.Footer permission;

    @Inject
    private FooterTicker footerTicker;

    @Inject
    public FooterModule(FileManager fileManager) {
        super(module -> module.getMessage().getTab().getFooter());

        message = fileManager.getMessage().getTab().getFooter();
        permission = fileManager.getPermission().getMessage().getTab().getFooter();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            footerTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = nextListMessage(fPlayer, message.isRandom(), resolveLocalization(fPlayer).getLists());
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
}
