package net.flectone.pulse.module.message.tab.playerlist;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.PotionUtil;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Range;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.scoreboard.MinecraftScoreboardModule;
import net.flectone.pulse.module.message.tab.playerlist.listener.MinecraftPacketPlayerlistnameListener;
import net.flectone.pulse.module.message.tab.playerlist.listener.MinecraftPulsePlayerlistnameListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.service.SocialService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

@Singleton
public class MinecraftPlayerlistnameModule extends PlayerlistnameModuleImpl {

    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> ADD_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER
    );

    private static final EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> UPDATE_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER
    );

    private final Map<UUID, Set<UUID>> proxyPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, PlayerlistnameEntry>> sentEntries = new ConcurrentHashMap<>();

    private volatile long lastRefreshTick = 0L;

    private final FPlayerService fPlayerService;
    private final SocialService socialService;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;
    private final MinecraftPacketSender packetSender;
    private final MinecraftPacketProvider packetProvider;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final MinecraftSkinService skinService;
    private final ProxyRegistry proxyRegistry;
    private final ModuleController moduleController;
    private final MinecraftScoreboardModule scoreboardModule;
    private final IntegrationModule integrationModule;
    private final RangeFilter rangeFilter;
    private final boolean isNewerThanOrEqualsV_1_19_3;

    @Inject
    public MinecraftPlayerlistnameModule(FileFacade fileFacade,
                                         SocialService socialService,
                                         FPlayerService fPlayerService,
                                         PlatformPlayerAdapter platformPlayerAdapter,
                                         MessagePipeline messagePipeline,
                                         MinecraftPacketSender packetSender,
                                         MinecraftPacketProvider packetProvider,
                                         TaskScheduler taskScheduler,
                                         ListenerRegistry listenerRegistry,
                                         MinecraftSkinService skinService,
                                         ProxyRegistry proxyRegistry,
                                         ModuleController moduleController,
                                         MinecraftScoreboardModule scoreboardModule,
                                         IntegrationModule integrationModule,
                                         RangeFilter rangeFilter,
                                         @Named("isNewerThanOrEqualsV_1_19_3") boolean isNewerThanOrEqualsV_1_19_3) {
        super(fileFacade, socialService);

        this.fPlayerService = fPlayerService;
        this.socialService = socialService;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.skinService = skinService;
        this.proxyRegistry = proxyRegistry;
        this.moduleController = moduleController;
        this.scoreboardModule = scoreboardModule;
        this.integrationModule = integrationModule;
        this.rangeFilter = rangeFilter;
        this.isNewerThanOrEqualsV_1_19_3 = isNewerThanOrEqualsV_1_19_3;
    }

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runAsyncTimer(this::update, ticker.period());
        }

        listenerRegistry.register(MinecraftPulsePlayerlistnameListener.class);
        listenerRegistry.register(MinecraftPacketPlayerlistnameListener.class);
    }

    @Override
    public void onDisable() {
        sentEntries.clear();
    }

    @Override
    public void update() {
        if (!moduleController.isEnable(this)) return;

        taskScheduler.runAsync(() -> {
            Map<UUID, Map<Integer, Component>> groupCache = isFullRefresh() ? null : new HashMap<>();

            List<FPlayer> onlinePlayers = fPlayerService.getOnlineFPlayers();

            for (FPlayer fReceiver : fPlayerService.getPlatformFPlayers()) {
                if (isNewerThanOrEqualsV_1_19_3) {
                    sendPlayerInfos(fReceiver, onlinePlayers, groupCache);
                } else {
                    sendPlayerData(fReceiver, onlinePlayers, groupCache);
                }
            }

            removeOfflineProxyPlayers();
        });
    }

    @Override
    public void update(FPlayer fSender) {
        if (!moduleController.isEnable(this)) return;

        Map<UUID, Map<Integer, Component>> groupCache = new HashMap<>();

        for (FPlayer fReceiver : fPlayerService.getPlatformFPlayers()) {
            List<FPlayer> onlinePlayers = fReceiver.equals(fSender)
                    ? fPlayerService.getOnlineFPlayers()
                    : List.of(fSender);

            if (isNewerThanOrEqualsV_1_19_3) {
                sendPlayerInfos(fReceiver, onlinePlayers, groupCache);
            } else {
                sendPlayerData(fReceiver, onlinePlayers, groupCache);
            }
        }
    }

    private void sendPlayerInfos(FPlayer fReceiver, List<FPlayer> onlinePlayers, @Nullable Map<UUID, Map<Integer, Component>> groupCache) {
        List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> addPlayerInfoList = new ArrayList<>();
        List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> updatePlayerInfoList = new ArrayList<>();
        List<UUID> removeProfileList = new ArrayList<>();

        for (FPlayer fSender : onlinePlayers) {
            if (!socialService.canSeeVanished(fSender, fReceiver)) {
                forgetEntry(fReceiver.uuid(), fSender.uuid());
                continue;
            }

            User user = packetProvider.getUser(fSender);
            UserProfile userProfile = createUserProfile(fSender, fReceiver, user);
            if (userProfile == null) {
                forgetEntry(fReceiver.uuid(), fSender.uuid());
                continue;
            }

            Predicate<FPlayer> listedFilter = rangeFilter.createFilter(fSender, config().range(), name());

            GameMode gameMode = GameMode.valueOf(platformPlayerAdapter.getGamemode(fSender));
            PlayerlistnameEntry sentEntry = getEntry(fReceiver, fSender);

            WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                    userProfile,
                    isListed(fSender, fReceiver, gameMode) && listedFilter.test(fReceiver),
                    platformPlayerAdapter.getPing(fSender),
                    gameMode,
                    buildFPlayerName(fSender, fReceiver, sentEntry, groupCache),
                    null,
                    gameMode != GameMode.SPECTATOR || config().spectatorListOrder() ? integrationModule.getSortWeight(fSender) : 0
            );

            PlayerlistnameEntry entry = PlayerlistnameEntry.of(playerInfo);
            rememberEntry(fReceiver, fSender, entry);

            boolean profileChanged = sentEntry == null || sentEntry.profile() != entry.profile();
            if (user == null && profileChanged) {
                removeProfileList.add(fSender.uuid());
                addPlayerInfoList.add(playerInfo);
                continue;
            }

            if (!entry.equals(sentEntry)) {
                updatePlayerInfoList.add(playerInfo);
            }
        }

        if (!removeProfileList.isEmpty()) {
            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoRemove(removeProfileList), true);
        }

        if (!addPlayerInfoList.isEmpty()) {
            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoUpdate(ADD_ACTIONS, addPlayerInfoList), true);
        }

        if (!updatePlayerInfoList.isEmpty()) {
            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoUpdate(UPDATE_ACTIONS, updatePlayerInfoList), true);
        }
    }

    private void sendPlayerData(FPlayer fReceiver, List<FPlayer> onlinePlayers, @Nullable Map<UUID, Map<Integer, Component>> groupCache) {
        List<WrapperPlayServerPlayerInfo.PlayerData> addPlayerDataList = new ArrayList<>();
        List<WrapperPlayServerPlayerInfo.PlayerData> updatePlayerDataList = new ArrayList<>();
        List<WrapperPlayServerPlayerInfo.PlayerData> removePlayerDataList = new ArrayList<>();

        for (FPlayer fSender : onlinePlayers) {
            if (!socialService.canSeeVanished(fSender, fReceiver)) {
                forgetEntry(fReceiver.uuid(), fSender.uuid());
                continue;
            }

            User user = packetProvider.getUser(fSender);
            UserProfile userProfile = createUserProfile(fSender, fReceiver, user);
            if (userProfile == null) {
                forgetEntry(fReceiver.uuid(), fSender.uuid());
                continue;
            }

            PlayerlistnameEntry sentEntry = getEntry(fReceiver, fSender);

            WrapperPlayServerPlayerInfo.PlayerData playerData = new WrapperPlayServerPlayerInfo.PlayerData(
                    buildFPlayerName(fSender, fReceiver, sentEntry, groupCache),
                    userProfile,
                    GameMode.valueOf(platformPlayerAdapter.getGamemode(fSender)),
                    platformPlayerAdapter.getPing(fSender)
            );

            PlayerlistnameEntry entry = PlayerlistnameEntry.of(playerData);
            rememberEntry(fReceiver, fSender, entry);

            boolean profileChanged = sentEntry == null || sentEntry.profile() != entry.profile();
            if (user == null && profileChanged) {
                removePlayerDataList.add(new WrapperPlayServerPlayerInfo.PlayerData(null, new UserProfile(fSender.uuid(), ""), null, 0));
                addPlayerDataList.add(playerData);
                continue;
            }

            if (!entry.equals(sentEntry)) {
                updatePlayerDataList.add(playerData);
            }
        }

        if (!removePlayerDataList.isEmpty()) {
            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, removePlayerDataList), true);
        }

        if (!addPlayerDataList.isEmpty()) {
            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, addPlayerDataList), true);
        }

        if (!updatePlayerDataList.isEmpty()) {
            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME, updatePlayerDataList), true);
        }
    }

    public void removeOfflineProxyPlayers() {
        if (proxyPlayers.isEmpty()) return;

        proxyPlayers.forEach((key, value) -> value.stream()
                .filter(uuid -> {
                    FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
                    FPlayer fReceiver = fPlayerService.getFPlayer(key);
                    return !fPlayer.isOnline() || !socialService.canSeeVanished(fPlayer, fReceiver);
                })
                .forEach(uuid -> {
                    packetSender.send(key, createRemovePacket(uuid));

                    forgetEntry(key, uuid);

                    value.remove(uuid);

                    if (value.isEmpty()) {
                        proxyPlayers.remove(key);
                    }
                })
        );
    }

    public void remove(UUID uuid) {
        if (!moduleController.isEnable(this)) return;

        if (isProxyMode()) {
            proxyPlayers.values().forEach(uuids -> uuids.removeIf(uuid::equals));
            scoreboardModule.remove(fPlayerService.getFPlayer(uuid));
        }

        sentEntries.values().forEach(entries -> entries.remove(uuid));

        platformPlayerAdapter.getOnlinePlayers().forEach(onlineUUID ->
                packetSender.send(onlineUUID, createRemovePacket(uuid))
        );
    }

    private PacketWrapper<?> createRemovePacket(UUID uuid) {
        return isNewerThanOrEqualsV_1_19_3
                ? new WrapperPlayServerPlayerInfoRemove(uuid)
                : new WrapperPlayServerPlayerInfo(WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER, new WrapperPlayServerPlayerInfo.PlayerData(null, new UserProfile(uuid, ""), null, 0));
    }

    public void clearPlayer(UUID uuid) {
        if (!moduleController.isEnable(this)) return;

        sentEntries.remove(uuid);
        sentEntries.values().forEach(entries -> entries.remove(uuid));

        if (isProxyMode()) {
            proxyPlayers.remove(uuid);
        }
    }

    public boolean isProxyMode() {
        return moduleController.isEnable(this) && config().range().is(Range.Type.PROXY) && proxyRegistry.hasEnabledProxy();
    }

    private void rememberEntry(FPlayer fReceiver, FPlayer fSender, PlayerlistnameEntry entry) {
        sentEntries.computeIfAbsent(fReceiver.uuid(), _ -> new ConcurrentHashMap<>())
                .put(fSender.uuid(), entry);
    }

    private @Nullable PlayerlistnameEntry getEntry(FPlayer fReceiver, FPlayer fSender) {
        Map<UUID, PlayerlistnameEntry> receiverEntries = sentEntries.get(fReceiver.uuid());
        if (receiverEntries == null) return null;

        return receiverEntries.get(fSender.uuid());
    }

    public void forgetEntry(UUID receiver, UUID sender) {
        Map<UUID, PlayerlistnameEntry> receiverEntries = sentEntries.get(receiver);
        if (receiverEntries != null) {
            receiverEntries.remove(sender);
        }
    }

    @Nullable
    private UserProfile createUserProfile(FPlayer fSender, FPlayer fReceiver, @Nullable User user) {
        if (user != null) {
            return user.getProfile();
        }

        if (!isProxyMode()) {
            return null;
        }

        proxyPlayers.computeIfAbsent(fReceiver.uuid(), _ -> new CopyOnWriteArraySet<>()).add(fSender.uuid());

        if (!scoreboardModule.hasTeam(fSender, fReceiver)) {
            scoreboardModule.createOrUpdate(fSender);
        }

        PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(fSender);
        List<TextureProperty> textureProperties = List.of(new TextureProperty(profileProperty.name(), profileProperty.value(), profileProperty.signature()));

        return new UserProfile(fSender.uuid(), fSender.name(), textureProperties);
    }

    private boolean isListed(FPlayer fPlayer, FPlayer fReceiver, GameMode gameMode) {
        if (config().hideInvisible() && platformPlayerAdapter.hasPotionEffect(fPlayer, PotionUtil.INVISIBILITY_POTION_NAME)) {
            return false;
        }

        if (config().hideSpectator() && gameMode == GameMode.SPECTATOR) {
            return false;
        }

        return socialService.canSeeVanished(fPlayer, fReceiver);
    }

    private Component buildFPlayerName(FPlayer fPlayer, FPlayer fReceiver, @Nullable PlayerlistnameEntry sentEntry, @Nullable Map<UUID, Map<Integer, Component>> groupCache) {
        if (groupCache == null || sentEntry == null) {
            return buildFPlayerName(fPlayer, fReceiver);
        }

        Map<Integer, Component> senderCache = groupCache.computeIfAbsent(fPlayer.uuid(), _ -> new HashMap<>());

        Component component = senderCache.get(sentEntry.displayName());
        if (component != null) return component;

        component = buildFPlayerName(fPlayer, fReceiver);
        senderCache.put(sentEntry.displayName(), component);

        return component;
    }

    private Component buildFPlayerName(FPlayer fPlayer, FPlayer fReceiver) {
        return messagePipeline.build(MessageContext.builder()
                .module(this.name())
                .sender(fPlayer)
                .receiver(fReceiver)
                .message(localization(fReceiver).format())
                // 3 - offline client, 4 - official client
                // disable for offline client
                .flag(MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, fReceiver.uuid().version() == 3)
                .build()
        );
    }

    private boolean isFullRefresh() {
        long refreshTicks = config().refresh();
        if (refreshTicks <= 0) return false;

        long currentTick = taskScheduler.getCurrentTick();
        if (currentTick - lastRefreshTick < refreshTicks) return false;

        lastRefreshTick = currentTick;

        return true;
    }

    private record PlayerlistnameEntry(
            int displayName,
            boolean listed,
            int listOrder,
            int profile
    ) {

        static PlayerlistnameEntry of(WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo) {
            return new PlayerlistnameEntry(
                    Objects.hashCode(playerInfo.getDisplayName()),
                    playerInfo.isListed(),
                    playerInfo.getListOrder(),
                    profileHash(playerInfo.getGameProfile())
            );
        }

        static PlayerlistnameEntry of(WrapperPlayServerPlayerInfo.PlayerData playerData) {
            return new PlayerlistnameEntry(
                    Objects.hashCode(playerData.getDisplayName()),
                    false,
                    0,
                    profileHash(playerData.getUserProfile())
            );
        }

        private static int profileHash(@Nullable UserProfile userProfile) {
            if (userProfile == null) return 0;

            int hash = Objects.hashCode(userProfile.getName());
            for (TextureProperty textureProperty : userProfile.getTextureProperties()) {
                hash = hash * 31 + Objects.hash(
                        textureProperty.getName(),
                        textureProperty.getValue(),
                        textureProperty.getSignature()
                );
            }

            return hash;
        }

    }

}
