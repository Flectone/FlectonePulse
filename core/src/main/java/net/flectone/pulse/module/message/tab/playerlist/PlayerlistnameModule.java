package net.flectone.pulse.module.message.tab.playerlist;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.tab.playerlist.listener.PlayerlistnamePulseListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SkinService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerlistnameModule extends AbstractModuleLocalization<Localization.Message.Tab.Playerlistname> {

    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> ADD_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME
    );

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final SkinService skinService;
    private final ProxyRegistry proxyRegistry;
    private final ScoreboardModule scoreboardModule;
    private final IntegrationModule integrationModule;

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

    public void add(UUID uuid) {
        if (!isProxyMode()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

        // platform players will be added automatically
        if (platformPlayerAdapter.isOnline(fPlayer)) return;

        UserProfile userProfile = createUserProfile(fPlayer);

        fPlayerService.getOnlineFPlayers().stream()
                .filter(fReceiver -> integrationModule.canSeeVanished(fPlayer, fReceiver))
                .forEach(fReceiver -> packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoUpdate(ADD_ACTIONS, createPlayerInfo(fPlayer, fReceiver, userProfile))));
    }

    public void remove(UUID uuid) {
        if (!isProxyMode()) return;

        platformPlayerAdapter.getOnlinePlayers().forEach(onlineUUID ->
                packetSender.send(onlineUUID, new WrapperPlayServerPlayerInfoRemove(uuid))
        );
    }

    private void updatePlayerlistname(FPlayer fPlayer, FPlayer fReceiver) {
        User user = packetProvider.getUser(fPlayer);
        if (user == null) return;

        Component name = buildFPlayerName(fPlayer, fReceiver);

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_4)) {
            if (isProxyMode()) {
                List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> proxyPlayersInfo = getProxyPlayerInfos(fReceiver);
                if (!proxyPlayersInfo.isEmpty()) {
                    packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoUpdate(ADD_ACTIONS, proxyPlayersInfo));
                }
            }

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

    public boolean isProxyMode() {
        return config().isProxyMode() && proxyRegistry.hasEnabledProxy();
    }

    private List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> getProxyPlayerInfos(FPlayer fReceiver) {
        List<UUID> currentServerPlayers = platformPlayerAdapter.getOnlinePlayers();

        return fPlayerService.findOnlineFPlayers()
                .stream()
                .filter(fPlayer -> !currentServerPlayers.contains(fPlayer.getUuid()))
                .filter(fPlayer -> integrationModule.canSeeVanished(fPlayer, fReceiver))
                .map(fPlayer -> createPlayerInfo(fPlayer, fReceiver, null))
                .toList();
    }

    private Component buildFPlayerName(FPlayer fPlayer, FPlayer fReceiver) {
        // 3 - offline client, 4 - official client
        boolean offlineClient = fPlayer.getUuid().version() == 3;
        return messagePipeline.builder(fPlayer, fReceiver, localization(fReceiver).getFormat())
                .flag(MessageFlag.OBJECT_PLAYER_HEAD, offlineClient) // disable player_head for official client
                .build();
    }

    private WrapperPlayServerPlayerInfoUpdate.PlayerInfo createPlayerInfo(FPlayer fPlayer, FPlayer fReceiver, @Nullable UserProfile userProfile) {
        if (userProfile == null) {
            userProfile = createUserProfile(fPlayer);
        }

        return new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                userProfile,
                true,
                -1,
                GameMode.SURVIVAL,
                buildFPlayerName(fPlayer, fReceiver),
                null
        );
    }

    private UserProfile createUserProfile(FPlayer fPlayer) {
        if (fPlayer.getSettingsText().isEmpty()) {
            fPlayerService.loadSettings(fPlayer);
        }

        if (!scoreboardModule.hasTeam(fPlayer)) {
            scoreboardModule.create(fPlayer, true);
        }

        PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(fPlayer);
        List<TextureProperty> textureProperties = List.of(new TextureProperty(profileProperty.name(), profileProperty.value(), profileProperty.signature()));
        return new UserProfile(fPlayer.getUuid(), fPlayer.getName(), textureProperties);
    }

}
