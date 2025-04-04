package net.flectone.pulse.module.message.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.rightclick.listener.RightclickPacketListener;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;

import java.util.UUID;


@Singleton
public class BukkitRightclickModule extends RightclickModule {

    private final Message.Rightclick message;
    private final Permission.Message.Rightclick permission;

    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public BukkitRightclickModule(FileManager fileManager,
                                  FPlayerService fPlayerService,
                                  ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getRightclick());
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getRightclick();
        permission = fileManager.getPermission().getMessage().getRightclick();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        listenerRegistry.register(RightclickPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID uuid, int targetId) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayerService.getFPlayer(targetId);
        if (fTarget.isUnknown()) return;

        builder(fTarget)
                .receiver(fPlayer)
                .format(Localization.Message.Rightclick::getFormat)
                .destination(message.getDestination())
                .sound(getSound())
                .sendBuilt();
    }
}
