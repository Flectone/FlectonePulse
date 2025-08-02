package net.flectone.pulse.module.message.format.world;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.world.listener.WorldPacketListener;
import net.flectone.pulse.module.message.format.world.listener.WorldPulseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

@Singleton
public class WorldModule extends AbstractModule {

    private final Message.Format.World message;
    private final Permission.Message.Format.World permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final PacketProvider packetProvider;

    @Inject
    public WorldModule(FileResolver fileResolver,
                       FPlayerService fPlayerService,
                       PlatformPlayerAdapter platformPlayerAdapter,
                       ListenerRegistry listenerRegistry,
                       TaskScheduler taskScheduler,
                       PacketProvider packetProvider) {
        this.message = fileResolver.getMessage().getFormat().getWorld();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getWorld();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.listenerRegistry = listenerRegistry;
        this.taskScheduler = taskScheduler;
        this.packetProvider = packetProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable() || packetProvider.getServerVersion().isOlderThan(ServerVersion.V_1_9)) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }

        listenerRegistry.register(WorldPacketListener.class);
        listenerRegistry.register(WorldPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void update(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        String newWorldPrefix = message.getMode() == Mode.TYPE
                ? message.getValues().get(platformPlayerAdapter.getWorldEnvironment(fPlayer))
                : message.getValues().get(platformPlayerAdapter.getWorldName(fPlayer));

        String fPlayerWorldPrefix = fPlayer.getSettingValue(FPlayer.Setting.WORLD_PREFIX);
        if (newWorldPrefix == null && fPlayerWorldPrefix == null) return;
        if (newWorldPrefix != null && newWorldPrefix.equalsIgnoreCase(fPlayerWorldPrefix)) return;

        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.WORLD_PREFIX, newWorldPrefix);
    }

    public enum Mode {
        TYPE,
        NAME
    }
}
