package net.flectone.pulse.module.message.sleep;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.sleep.listener.SleepPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.util.MinecraftTranslationKeys;

import java.util.UUID;

@Singleton
public class SleepModule extends AbstractModuleMessage<Localization.Message.Sleep> {

    private final Message.Sleep message;
    private final Permission.Message.Sleep permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SleepModule(FileManager fileManager,
                       FPlayerManager fPlayerManager,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSleep());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getSleep();
        permission = fileManager.getPermission().getMessage().getSleep();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SleepPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, MinecraftTranslationKeys key, String sleepCount, String allCount) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(bed -> switch (key) {
                    case SLEEP_NOT_POSSIBLE -> bed.getNotPossible();
                    case SLEEP_PLAYERS_SLEEPING -> bed.getPlayersSleeping()
                            .replace("<sleep_count>", sleepCount)
                            .replace("<all_count>", allCount);
                    case SLEEP_SKIPPING_NIGHT -> bed.getSkippingNight();
                    default -> "";
                })
                .sound(getSound())
                .sendBuilt();
    }
}
