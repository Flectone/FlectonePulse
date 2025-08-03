package net.flectone.pulse.module.message.tab.playerlist;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.tab.playerlist.listener.PlayerlistnamePulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
public class PlayerlistnameModule extends AbstractModuleLocalization<Localization.Message.Tab.Playerlistname> {

    private final Message.Tab.Playerlistname message;
    private final Permission.Message.Tab.Playerlistname permission;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public PlayerlistnameModule(FPlayerService fPlayerService,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                FileResolver fileResolver,
                                MessagePipeline messagePipeline,
                                PacketSender packetSender,
                                PacketProvider packetProvider,
                                TaskScheduler taskScheduler,
                                ListenerRegistry listenerRegistry) {
        super(module -> module.getMessage().getTab().getPlayerlistname());

        this.message = fileResolver.getMessage().getTab().getPlayerlistname();
        this.permission = fileResolver.getPermission().getMessage().getTab().getPlayerlistname();
        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        fPlayerService.getPlatformFPlayers().forEach(this::send);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(PlayerlistnamePulseListener.class);
    }

    @Async(delay = 10)
    public void update() {
        if (!isEnable()) return;

        fPlayerService.getPlatformFPlayers().forEach(this::send);
    }

    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        fPlayerService.getFPlayers().stream()
                .filter(FPlayer::isOnline)
                .forEach(fReceiver -> updatePlayerlistname(fPlayer, fReceiver));
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    private void updatePlayerlistname(FPlayer fPlayer, FPlayer fReceiver) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        Component name = messagePipeline.builder(fPlayer, fReceiver, resolveLocalization(fReceiver).getFormat())
                .build();

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)) {
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                    user.getProfile(),
                    true,
                    fPlayerService.getPing(fPlayer),
                    platformPlayerAdapter.getGamemode(fPlayer),
                    name,
                    null
            );

            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoUpdate(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, playerInfo));
            return;
        }


        WrapperPlayServerPlayerInfo.PlayerData playerData = new WrapperPlayServerPlayerInfo.PlayerData(
                name,
                user.getProfile(),
                platformPlayerAdapter.getGamemode(fPlayer),
                fPlayerService.getPing(fPlayer)
        );

        packetSender.send(fReceiver, new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME, playerData));
    }
}
