package net.flectone.pulse.module.message.tab.playerlist;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
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
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerlistnameModule extends AbstractModuleLocalization<Localization.Message.Tab.Playerlistname> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        fPlayerService.getPlatformFPlayers().forEach(this::send);

        Ticker ticker = config().getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::send), ticker.getPeriod());
        }

        listenerRegistry.register(PlayerlistnamePulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.PLAYERLISTNAME;
    }

    @Override
    public Message.Tab.Playerlistname config() {
        return fileResolver.getMessage().getTab().getPlayerlistname();
    }

    @Override
    public Permission.Message.Tab.Playerlistname permission() {
        return fileResolver.getPermission().getMessage().getTab().getPlayerlistname();
    }

    @Override
    public Localization.Message.Tab.Playerlistname localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getTab().getPlayerlistname();
    }

    @Async
    public void update() {
        if (!isEnable()) return;

        fPlayerService.getPlatformFPlayers().forEach(this::send);
    }

    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (!platformPlayerAdapter.isOnline(fPlayer)) return;

        fPlayerService.getFPlayersWhoCanSee(fPlayer)
                .forEach(fReceiver -> updatePlayerlistname(fPlayer, fReceiver));
    }

    private void updatePlayerlistname(FPlayer fPlayer, FPlayer fReceiver) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        // 3 - offline client, 4 - official client
        boolean offlineClient = fReceiver.getUuid().version() == 3;
        Component name = messagePipeline.builder(fPlayer, fReceiver, localization(fReceiver).getFormat())
                .flag(MessageFlag.OBJECT_PLAYER_HEAD, offlineClient) // disable player_head for official client
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
