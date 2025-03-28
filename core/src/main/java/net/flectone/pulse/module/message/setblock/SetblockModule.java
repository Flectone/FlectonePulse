package net.flectone.pulse.module.message.setblock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.setblock.listener.SetblockPacketListener;
import net.flectone.pulse.service.FPlayerService;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class SetblockModule extends AbstractModuleMessage<Localization.Message.Setblock> {

    private final Message.Setblock message;
    private final Permission.Message.Setblock permission;

    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SetblockModule(FileManager fileManager,
                          FPlayerService fPlayerService,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSetblock());

        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getSetblock();
        permission = fileManager.getPermission().getMessage().getSetblock();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SetblockPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String x, @NotNull String y, @NotNull String z) {
        FPlayer fPlayer = fPlayerService.getFPlayer(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> s.getFormat()
                        .replace("<x>", x)
                        .replace("<y>", y)
                        .replace("<z>", z)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
