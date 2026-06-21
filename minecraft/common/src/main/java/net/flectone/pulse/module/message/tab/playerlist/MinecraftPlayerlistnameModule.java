package net.flectone.pulse.module.message.tab.playerlist;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.scoreboard.MinecraftScoreboardModule;
import net.flectone.pulse.module.message.tab.playerlist.listener.MinecraftPulsePlayerlistnameListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.filter.RangeFilter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MinecraftSkinService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.PotionUtil;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftPlayerlistnameModule implements ModuleLocalization<Localization.Message.Tab.Playerlistname> {

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

    private final FileFacade fileFacade;
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
    private final @Named("isNewerThanOrEqualsV_1_19_4") boolean isNewerThanOrEqualsV_1_19_4;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runAsyncTimer(this::update, ticker.period());
        }

        listenerRegistry.register(MinecraftPulsePlayerlistnameListener.class);
    }

    @Override
    public ImmutableSet.Builder<PermissionSetting> permissionBuilder() {
        return ModuleLocalization.super.permissionBuilder()
                .add(permission().hideInvisible(), permission().hideSpectator());
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_TAB_PLAYERLISTNAME;
    }

    @Override
    public Message.Tab.Playerlistname config() {
        return fileFacade.message().tab().playerlistname();
    }

    @Override
    public Permission.Message.Tab.Playerlistname permission() {
        return fileFacade.permission().message().tab().playerlistname();
    }

    @Override
    public Localization.Message.Tab.Playerlistname localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().tab().playerlistname();
    }

    public void update() {
        if (!moduleController.isEnable(this)) return;

        taskScheduler.runAsync(() -> {
            fPlayerService.getOnlineFPlayers().forEach(this::update);

            if (proxyPlayers.isEmpty()) return;

            proxyPlayers.forEach((key, value) -> value.stream()
                    .filter(uuid -> {
                        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
                        FPlayer fReceiver = fPlayerService.getFPlayer(key);
                        return !fPlayer.isOnline() || !socialService.canSeeVanished(fPlayer, fReceiver);
                    })
                    .forEach(uuid -> {
                        packetSender.send(key, new WrapperPlayServerPlayerInfoRemove(uuid));

                        value.remove(uuid);

                        if (value.isEmpty()) {
                            proxyPlayers.remove(key);
                        }
                    })
            );
        });
    }

    public void update(FPlayer fSender) {
        if (moduleController.isDisabledFor(this, fSender)) return;

        Predicate<FPlayer> listedFilter = rangeFilter.createFilter(fSender, config().range());

        fPlayerService.getPlatformFPlayers().stream()
                .filter(viewer -> socialService.canSeeVanished(fSender, viewer))
                .forEach(fReceiver -> update(fSender, fReceiver, listedFilter));
    }

    public void update(FPlayer fSender, FPlayer fReceiver, Predicate<FPlayer> listedFilter) {
        User user = packetProvider.getUser(fSender);

        UserProfile userProfile;

        boolean proxyPlayer = false;
        if (user == null) {
            if (!isProxyMode()) {
                return;
            }

            Set<UUID> forPlayers = proxyPlayers.getOrDefault(fReceiver.uuid(), new CopyOnWriteArraySet<>());
            if (!forPlayers.contains(fSender.uuid())) {
                forPlayers.add(fSender.uuid());
                proxyPlayer = true;
            }

            proxyPlayers.put(fReceiver.uuid(), forPlayers);

            userProfile = createUserProfile(fSender, fReceiver);
        } else {
            userProfile = user.getProfile();
        }

        Component name = buildFPlayerName(fSender, fReceiver);

        if (isNewerThanOrEqualsV_1_19_4) {
            GameMode gameMode = GameMode.valueOf(platformPlayerAdapter.getGamemode(fSender));

            WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                    userProfile,
                    isListed(fSender, fReceiver, gameMode) && listedFilter.test(fReceiver),
                    platformPlayerAdapter.getPing(fSender),
                    gameMode,
                    name,
                    null,
                    gameMode != GameMode.SPECTATOR || config().spectatorListOrder() ? integrationModule.getGroupWeight(fSender) : 0
            );

            packetSender.send(fReceiver, new WrapperPlayServerPlayerInfoUpdate(proxyPlayer ? ADD_ACTIONS : UPDATE_ACTIONS, playerInfo));
            return;
        }

        WrapperPlayServerPlayerInfo.PlayerData playerData = new WrapperPlayServerPlayerInfo.PlayerData(
                name,
                userProfile,
                GameMode.valueOf(platformPlayerAdapter.getGamemode(fSender)),
                platformPlayerAdapter.getPing(fSender)
        );

        packetSender.send(fReceiver, new WrapperPlayServerPlayerInfo(proxyPlayer ? WrapperPlayServerPlayerInfo.Action.ADD_PLAYER : WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME, playerData));
    }

    public void remove(UUID uuid) {
        if (isProxyMode()) {
            proxyPlayers.values().forEach(uuids -> uuids.removeIf(uuid::equals));
            scoreboardModule.remove(fPlayerService.getFPlayer(uuid));
        }

        platformPlayerAdapter.getOnlinePlayers().forEach(onlineUUID ->
                packetSender.send(onlineUUID, new WrapperPlayServerPlayerInfoRemove(uuid))
        );
    }

    public void clearProxyPlayers(UUID uuid) {
        if (isProxyMode()) {
            proxyPlayers.remove(uuid);
        }
    }

    public boolean isProxyMode() {
        return moduleController.isEnable(this) && config().range().is(Range.Type.PROXY) && proxyRegistry.hasEnabledProxy();
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

    private Component buildFPlayerName(FPlayer fPlayer, FPlayer fReceiver) {
        return messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .receiver(fReceiver)
                .message(localization(fReceiver).format())
                // 3 - offline client, 4 - official client
                // disable for offline client
                .flag(MessageFlag.OBJECT_PLAYER_HEAD_PROCESSING, fReceiver.uuid().version() == 3)
                .build()
        );
    }

    private UserProfile createUserProfile(FPlayer fPlayer, FPlayer fReceiver) {
        if (!scoreboardModule.hasTeam(fPlayer, fReceiver)) {
            scoreboardModule.createOrUpdate(fPlayer);
        }

        PlayerHeadObjectContents.ProfileProperty profileProperty = skinService.getProfilePropertyFromCache(fPlayer);
        List<TextureProperty> textureProperties = List.of(new TextureProperty(profileProperty.name(), profileProperty.value(), profileProperty.signature()));
        return new UserProfile(fPlayer.uuid(), fPlayer.name(), textureProperties);
    }

}
