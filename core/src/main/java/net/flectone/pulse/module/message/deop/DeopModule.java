package net.flectone.pulse.module.message.deop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.deop.listener.DeopPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class DeopModule extends AbstractModuleMessage<Localization.Message.Deop> {

    private final Message.Deop message;
    private final Permission.Message.Deop permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerDAO fPlayerDAO;

    @Inject
    public DeopModule(FileManager fileManager,
                      FPlayerManager fPlayerManager,
                      ListenerRegistry listenerRegistry,
                      FPlayerDAO fPlayerDAO) {
        super(localization -> localization.getMessage().getDeop());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;
        this.fPlayerDAO = fPlayerDAO;

        message = fileManager.getMessage().getDeop();
        permission = fileManager.getPermission().getMessage().getDeop();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DeopPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String target) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerDAO.getFPlayer(target);

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(Localization.Message.Deop::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

}
