package net.flectone.pulse.module.message.deop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.death.listener.DeathPacketListener;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

@Singleton
public class DeopModule extends AbstractModuleMessage<Localization.Message.Deop> {

    private final Message.Deop message;
    private final Permission.Message.Deop permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;
    private final Database database;
    private final FLogger fLogger;

    @Inject
    public DeopModule(FileManager fileManager,
                      FPlayerManager fPlayerManager,
                      ListenerManager listenerManager,
                      Database database,
                      FLogger fLogger) {
        super(localization -> localization.getMessage().getDeop());

        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;
        this.database = database;
        this.fLogger = fLogger;

        message = fileManager.getMessage().getDeop();
        permission = fileManager.getPermission().getMessage().getDeop();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerManager.register(DeathPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, @NotNull String target) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        try {
            FPlayer fTarget = database.getFPlayer(target);

            builder(fTarget)
                    .destination(message.getDestination())
                    .receiver(fPlayer)
                    .format(Localization.Message.Deop::getFormat)
                    .sound(getSound())
                    .sendBuilt();

        } catch (SQLException e) {
            fLogger.warning(e);
        }
    }

}
