package net.flectone.pulse.module.message.tab.playerlist;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

@Singleton
public class PlayerlistnameModule extends AbstractModuleMessage<Localization.Message.Tab.Playerlistname> {

    private final Message.Tab.Playerlistname message;
    private final Permission.Message.Tab.Playerlistname permission;

    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final TaskScheduler taskScheduler;

    @Inject
    public PlayerlistnameModule(FPlayerService fPlayerService,
                                PlatformPlayerAdapter platformPlayerAdapter,
                                FileResolver fileResolver,
                                MessagePipeline messagePipeline,
                                PacketSender packetSender,
                                PacketProvider packetProvider,
                                TaskScheduler taskScheduler) {
        super(module -> module.getMessage().getTab().getPlayerlistname());

        this.fPlayerService = fPlayerService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.taskScheduler = taskScheduler;

        message = fileResolver.getMessage().getTab().getPlayerlistname();
        permission = fileResolver.getPermission().getMessage().getTab().getPlayerlistname();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::send), ticker.getPeriod());
        }
    }

    public void update() {
        if (!isEnable()) return;

        fPlayerService.getFPlayers().stream().filter(FPlayer::isOnline).forEach(this::send);
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
                .userMessage(false)
                .build();

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)) {
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
