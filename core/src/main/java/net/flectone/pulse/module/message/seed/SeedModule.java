package net.flectone.pulse.module.message.seed;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.seed.listener.SeedPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class SeedModule extends AbstractModuleMessage<Localization.Message.Seed> {

    private final Message.Seed message;
    private final Permission.Message.Seed permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;

    @Inject
    public SeedModule(FileManager fileManager,
                      FPlayerManager fPlayerManager,
                      ListenerManager listenerManager) {
        super(localization -> localization.getMessage().getSeed());

        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;

        message = fileManager.getMessage().getSeed();
        permission = fileManager.getPermission().getMessage().getSeed();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerManager.register(SeedPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String seed) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .receiver(fPlayer)
                .format(s -> s.getFormat().replace("<seed>", seed))
                .sound(getSound())
                .sendBuilt();
    }

}
