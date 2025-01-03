package net.flectone.pulse.module.message.brand;

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
import net.flectone.pulse.module.message.brand.ticker.BrandTicker;

@Singleton
public class BrandModule extends AbstractModuleListMessage<Localization.Message.Brand> {

    private final Message.Brand message;
    private final Permission.Message.Brand permission;

    @Inject
    private BrandTicker brandTicker;

    @Inject
    public BrandModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getBrand());

        message = fileManager.getMessage().getBrand();
        permission = fileManager.getPermission().getMessage().getBrand();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            brandTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String format = nextMessage(fPlayer, this.message.isRandom(), resolveLocalization(fPlayer).getValues());
        if (format == null) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .format(format)
                .sendBuilt();
    }
}
