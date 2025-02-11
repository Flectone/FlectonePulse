package net.flectone.pulse.module.message.op;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ListenerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.op.listener.OpPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class OpModule extends AbstractModuleMessage<Localization.Message.Op> {

    private final Message.Op message;
    private final Permission.Message.Op permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerManager listenerManager;
    private final FPlayerDAO fPlayerDAO;

    @Inject
    public OpModule(FileManager fileManager,
                    FPlayerManager fPlayerManager,
                    ListenerManager listenerManager,
                    FPlayerDAO fPlayerDAO) {
        super(localization -> localization.getMessage().getOp());

        this.fPlayerManager = fPlayerManager;
        this.listenerManager = listenerManager;
        this.fPlayerDAO = fPlayerDAO;

        message = fileManager.getMessage().getOp();
        permission = fileManager.getPermission().getMessage().getOp();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerManager.register(OpPacketListener.class);
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
        if (fTarget.isUnknown()) return;

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(Localization.Message.Op::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

}
