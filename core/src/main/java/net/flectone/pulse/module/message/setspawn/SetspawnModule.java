package net.flectone.pulse.module.message.setspawn;

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
import net.flectone.pulse.module.message.setspawn.listener.SetspawnPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;

import java.util.UUID;

@Singleton
public class SetspawnModule extends AbstractModuleMessage<Localization.Message.Setspawn> {

    private final Message.Setspawn message;
    private final Permission.Message.Setspawn permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SetspawnModule(FileManager fileManager,
                          FPlayerManager fPlayerManager,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSetspawn());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getSetspawn();
        permission = fileManager.getPermission().getMessage().getSetspawn();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SetspawnPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver) {
        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        builder(fPlayer)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(Localization.Message.Setspawn::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

}
