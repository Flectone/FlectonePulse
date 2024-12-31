package net.flectone.pulse.module.message.tab.header;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.message.tab.header.ticker.HeaderTicker;
import net.flectone.pulse.platform.PlatformSender;
import net.flectone.pulse.util.ComponentUtil;

@Singleton
public class HeaderModule extends AbstractModuleListMessage<Localization.Message.Tab.Header> {

    private final Message.Tab.Header message;
    private final Permission.Message.Tab.Header permission;

    private final PlatformSender platformSender;
    private final ComponentUtil componentUtil;

    @Inject
    private HeaderTicker headerTicker;

    @Inject
    public HeaderModule(FileManager fileManager,
                        PlatformSender platformSender,
                        ComponentUtil componentUtil) {
        super(module -> module.getMessage().getTab().getHeader());
        this.platformSender = platformSender;
        this.componentUtil = componentUtil;

        message = fileManager.getMessage().getTab().getHeader();
        permission = fileManager.getPermission().getMessage().getTab().getHeader();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Config.Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            headerTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String message = nextMessage(fPlayer, this.message.isRandom(), resolveLocalization(fPlayer).getValues());
        if (message == null) return;

        platformSender.sendPlayerListHeader(fPlayer, componentUtil.builder(fPlayer, message).build());
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
