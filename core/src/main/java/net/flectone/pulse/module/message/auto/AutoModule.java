package net.flectone.pulse.module.message.auto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;

import java.util.List;

@Singleton
public class AutoModule extends AbstractModuleListMessage<Localization.Message.Auto> {

    private final Message.Auto message;
    private final Permission.Message.Auto permission;

    private final TaskScheduler taskScheduler;
    private final FPlayerManager fPlayerManager;

    @Inject
    public AutoModule(FileManager fileManager,
                      TaskScheduler taskScheduler,
                      FPlayerManager fPlayerManager) {
        super(localization -> localization.getMessage().getAuto());

        this.taskScheduler = taskScheduler;
        this.fPlayerManager = fPlayerManager;

        message = fileManager.getMessage().getAuto();
        permission = fileManager.getPermission().getMessage().getAuto();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        message.getTypes().forEach((key, value) -> {
            Sound sound = createSound(value.getSound(), permission.getTypes().get(key));

            Ticker ticker = value.getTicker();
            if (ticker.isEnable()) {
                taskScheduler.runAsyncTimer(() -> fPlayerManager.getFPlayers().forEach(fPlayer -> send(fPlayer, key, value, sound)),
                        ticker.getPeriod(), ticker.getPeriod()
                );
            }
        });
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, String name, Message.Auto.Type type, Sound sound) {
        if (checkModulePredicates(fPlayer)) return;
        if (!fPlayer.is(FPlayer.Setting.AUTO)) return;

        List<String> messages = resolveLocalization(fPlayer).getTypes().get(name);
        if (messages == null) return;

        String format = getNextMessage(fPlayer, type.isRandom(), messages);
        if (format == null) return;

        builder(fPlayer)
                .destination(type.getDestination())
                .format(format)
                .sound(sound)
                .sendBuilt();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return List.of();
    }
}
