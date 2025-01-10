package net.flectone.pulse.module.message.auto;

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
import net.flectone.pulse.module.message.auto.ticker.AutoTicker;

import java.util.List;

@Singleton
public class AutoModule extends AbstractModuleListMessage<Localization.Message.Auto> {

    private final Message.Auto message;
    private final Permission.Message.Auto permission;

    @Inject
    private AutoTicker autoTicker;

    @Inject
    public AutoModule(FileManager fileManager) {
        super(localization -> localization.getMessage().getAuto());

        message = fileManager.getMessage().getAuto();
        permission = fileManager.getPermission().getMessage().getAuto();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            autoTicker.runTaskTimerAsync(ticker.getPeriod(), ticker.getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;
        if (!fPlayer.is(FPlayer.Setting.AUTO)) return;

        String format = getNextMessage(fPlayer, message.isRandom());
        if (format == null) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .format(format)
                .sound(getSound())
                .sendBuilt();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return resolveLocalization(fPlayer).getValues();
    }
}
