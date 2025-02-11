package net.flectone.pulse.module.message.spawnpoint;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.spawnpoint.listener.SpawnpointPacketListener;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Singleton
public class SpawnpointModule extends AbstractModuleMessage<Localization.Message.Spawnpoint> {

    private final Message.Spawnpoint message;
    private final Permission.Message.Spawnpoint permission;

    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SpawnpointModule(FileManager fileManager,
                            FPlayerManager fPlayerManager,
                            ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSpawnpoint());

        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;

        message = fileManager.getMessage().getSpawnpoint();
        permission = fileManager.getPermission().getMessage().getSpawnpoint();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SpawnpointPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(UUID receiver, String x, String y, String z, String angle, String world, @Nullable String target, @Nullable String count) {
        if (target == null && count == null) return;

        FPlayer fPlayer = fPlayerManager.get(receiver);
        if (checkModulePredicates(fPlayer)) return;

        FPlayer fTarget = fPlayer;

        if (target != null) {
            fTarget = fPlayerManager.getOnline(target);
            if (fTarget.isUnknown()) return;
        }

        builder(fTarget)
                .destination(message.getDestination())
                .receiver(fPlayer)
                .format(s -> (count == null ? s.getSingle() : s.getMultiple().replace("<count>", count))
                        .replace("<x>", x)
                        .replace("<y>", y)
                        .replace("<z>", z)
                        .replace("<angle>", angle)
                        .replace("<world>", world)
                )
                .sound(getSound())
                .sendBuilt();
    }

}
