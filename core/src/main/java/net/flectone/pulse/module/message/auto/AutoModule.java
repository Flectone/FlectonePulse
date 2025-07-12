package net.flectone.pulse.module.message.auto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

import java.util.List;

@Singleton
public class AutoModule extends AbstractModuleListMessage<Localization.Message.Auto> {

    private final Message.Auto message;
    private final Permission.Message.Auto permission;
    private final TaskScheduler taskScheduler;
    private final FPlayerService fPlayerService;

    @Inject
    public AutoModule(FileResolver fileResolver,
                      TaskScheduler taskScheduler,
                      FPlayerService fPlayerService) {
        super(localization -> localization.getMessage().getAuto());

        this.message = fileResolver.getMessage().getAuto();
        this.permission = fileResolver.getPermission().getMessage().getAuto();
        this.taskScheduler = taskScheduler;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        message.getTypes().forEach((key, value) -> {
            Sound sound = createSound(value.getSound(), permission.getTypes().get(key));

            Ticker ticker = value.getTicker();
            if (ticker.isEnable()) {
                taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(fPlayer -> send(fPlayer, key, value, sound)), ticker.getPeriod());
            }
        });
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(FPlayer fPlayer, String name, Message.Auto.Type type, Sound sound) {
        if (checkModulePredicates(fPlayer)) return;
        if (!fPlayer.isSetting(FPlayer.Setting.AUTO)) return;

        List<String> messages = resolveLocalization(fPlayer).getTypes().get(name);
        if (messages == null) return;

        String format = getNextMessage(fPlayer, type.isRandom(), messages);
        if (format == null) return;

        builder(fPlayer)
                .destination(type.getDestination())
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.AUTO))
                .format(format)
                .sound(sound)
                .sendBuilt();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return List.of();
    }
}
