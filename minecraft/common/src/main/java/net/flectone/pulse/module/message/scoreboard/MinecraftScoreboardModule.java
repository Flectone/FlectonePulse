package net.flectone.pulse.module.message.scoreboard;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.FlectonePulseAPI;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.scoreboard.listener.MinecraftPacketScoreboardListener;
import net.flectone.pulse.module.message.scoreboard.model.Team;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MinecraftScoreboardModule extends ScoreboardModule {

    private static final int ATTRIBUTE_BASE_VALUE = 64;
    private static final int ATTRIBUTE_INVISIBLE_VALUE = 0;

    private final Map<UUID, Map<UUID, Team>> playerReceiverTeamMap = new ConcurrentHashMap<>();

    private final ListenerRegistry listenerRegistry;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final MinecraftPacketSender packetSender;
    private final MinecraftPacketProvider packetProvider;
    private final ModuleController moduleController;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerService fPlayerService;
    private final boolean isNewerThanOrEqualsV118;
    private final boolean isNewerThanOrEqualsV262;

    @Inject
    public MinecraftScoreboardModule(FileFacade fileFacade,
                                     TaskScheduler taskScheduler,
                                     MessagePipeline messagePipeline,
                                     MinecraftPacketSender packetSender,
                                     MinecraftPacketProvider packetProvider,
                                     ListenerRegistry listenerRegistry,
                                     PlatformPlayerAdapter platformPlayerAdapter,
                                     ModuleController moduleController,
                                     Provider<IntegrationModule> integrationModuleProvider,
                                     SocialService socialService,
                                     FPlayerService fPlayerService,
                                     @Named("isNewerThanOrEqualsV_1_18") boolean isNewerThanOrEqualsV118,
                                     @Named("isNewerThanOrEqualsV_26_2") boolean isNewerThanOrEqualsV262) {
        super(fileFacade, listenerRegistry, platformPlayerAdapter, socialService);

        this.listenerRegistry = listenerRegistry;
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.moduleController = moduleController;
        this.integrationModuleProvider = integrationModuleProvider;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.fPlayerService = fPlayerService;
        this.isNewerThanOrEqualsV118 = isNewerThanOrEqualsV118;
        this.isNewerThanOrEqualsV262 = isNewerThanOrEqualsV262;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (isNewerThanOrEqualsV262) {
            listenerRegistry.register(MinecraftPacketScoreboardListener.class);

            if (!config().nameVisible()) {
                sendForAll(false);
            }
        }

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::createOrUpdate, ticker.period());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        playerReceiverTeamMap.values().forEach(teamMap ->
                teamMap.forEach((uuid, team) ->
                        send(uuid, team, WrapperPlayServerTeams.TeamMode.REMOVE)
                )
        );

        if (!FlectonePulseAPI.isDisabling()) {
            sendForAll(true);
        }

        playerReceiverTeamMap.clear();
    }

    @Override
    public void createOrUpdate(@NonNull FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            fPlayerService.getPlatformFPlayers().forEach(fReceiver -> {
                createOrUpdate(fPlayer, fReceiver);

                if (!hasTeam(fReceiver, fPlayer)) {
                    createOrUpdate(fReceiver, fPlayer);
                }
            });
        });
    }

    @Override
    public void remove(@NonNull FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (moduleController.isDisabledFor(this, fPlayer)) return;

            Map<UUID, Team> receivers = playerReceiverTeamMap.remove(fPlayer.uuid());
            if (receivers != null && !receivers.isEmpty()) {
                receivers.forEach((receiver, team) ->
                        send(receiver, team, WrapperPlayServerTeams.TeamMode.REMOVE)
                );
            }

            playerReceiverTeamMap.values().forEach(teamMap -> teamMap.remove(fPlayer.uuid()));
        });
    }

    public boolean hasTeam(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver) {
        return getTeam(fPlayer, fReceiver).isPresent();
    }

    // in new versions 26.2+, name can be hidden using an attribute to avoid problems with displaying name for pets
    // check MinecraftPacketScoreboardListener.class
    public boolean isModernPlayer(UUID uuid) {
        User user = packetProvider.getUser(uuid);
        if (user == null) return false;

        return user.getPacketVersion().isNewerThanOrEquals(ClientVersion.V_26_2);
    }

    public void send(@NonNull FPlayer fReceiver, @NonNull Team team, WrapperPlayServerTeams.@NonNull TeamMode teamMode) {
        send(fReceiver.uuid(), team, teamMode);
    }

    public void send(@NonNull UUID receiver, @NonNull Team team, WrapperPlayServerTeams.@NonNull TeamMode teamMode) {
        packetSender.send(receiver, new WrapperPlayServerTeams(team.name(), teamMode, team.info(), List.of(team.owner())));
    }

    public void sendForAll(boolean visible) {
        List<UUID> onlinePlayers = platformPlayerAdapter.getOnlinePlayers();
        onlinePlayers.forEach(player -> {
            int entityId = platformPlayerAdapter.getEntityId(player);
            if (entityId == 0) return;

            onlinePlayers.stream()
                    .filter(this::isModernPlayer)
                    .forEach(receiver -> send(receiver, entityId, visible));
        });
    }

    public void send(@NonNull UUID receiver, int playerId, boolean visible) {
        packetSender.send(receiver, new WrapperPlayServerUpdateAttributes(
                playerId,
                List.of(new WrapperPlayServerUpdateAttributes.Property(Attributes.NAME_TAG_DISTANCE, visible ? ATTRIBUTE_BASE_VALUE : ATTRIBUTE_INVISIBLE_VALUE, List.of())))
        );
    }

    private Optional<Team> getTeam(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver) {
        Map<UUID, Team> teamMap = playerReceiverTeamMap.get(fPlayer.uuid());
        if (teamMap == null || teamMap.isEmpty()) return Optional.empty();

        return Optional.ofNullable(teamMap.get(fReceiver.uuid()));
    }

    private void createOrUpdate(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver) {
        // new info
        Team newTeam = create(fPlayer, fReceiver);

        Optional<Team> optionalTeam = getTeam(fPlayer, fReceiver);
        if (optionalTeam.isPresent()) {
            Team oldTeam = optionalTeam.get();
            if (newTeam.name().equals(oldTeam.name())) {
                send(fReceiver, newTeam, WrapperPlayServerTeams.TeamMode.UPDATE);
            } else {
                send(fReceiver, oldTeam, WrapperPlayServerTeams.TeamMode.REMOVE);
                send(fReceiver, newTeam, WrapperPlayServerTeams.TeamMode.CREATE);
            }
        } else {
            send(fReceiver, newTeam, WrapperPlayServerTeams.TeamMode.CREATE);
        }

        // update info
        playerReceiverTeamMap.computeIfAbsent(fPlayer.uuid(), _ -> new ConcurrentHashMap<>()).put(fReceiver.uuid(), newTeam);
    }

    @NonNull
    private Team create(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver) {
        Component prefix = Component.empty();
        if (StringUtils.isNotEmpty(localization(fReceiver).prefix())) {
            prefix = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .receiver(fReceiver)
                    .message(localization(fReceiver).prefix())
                    .flag(MessageFlag.INVISIBLE_NAME_DETECTION, false)
                    .build()
            );
        }

        Component suffix = Component.empty();
        if (StringUtils.isNotEmpty(localization(fReceiver).suffix())) {
            suffix = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .receiver(fReceiver)
                    .message(localization(fReceiver).suffix())
                    .flag(MessageFlag.INVISIBLE_NAME_DETECTION, false)
                    .build()
            );
        }

        String teamName = getSortedName(fPlayer);

        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.text(teamName),
                prefix,
                suffix,
                isInvisibleNameFor(fPlayer) && !isModernPlayer(fReceiver.uuid()) ? WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OTHER_TEAMS : WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
                WrapperPlayServerTeams.CollisionRule.ALWAYS,
                getColor(fPlayer, fReceiver),
                WrapperPlayServerTeams.OptionData.NONE
        );

        return new Team(teamName, fPlayer.name(), info);
    }

    @NonNull
    private NamedTextColor getColor(@NonNull FPlayer fPlayer, @NonNull FPlayer fReceiver) {
        TextColor color = messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .receiver(fReceiver)
                .message(config().color())
                .build()
        ).color();

        return color == null ? NamedTextColor.WHITE : NamedTextColor.nearestTo(color);
    }

    @NonNull
    private String getSortedName(@NonNull FPlayer fPlayer) {
        int weight = integrationModuleProvider.get().getGroupWeight(fPlayer);

        // 32767 limit
        if (isNewerThanOrEqualsV118) {
            String paddedRank = String.format("%010d", Integer.MAX_VALUE - weight);
            String paddedName = String.format("%-16s", fPlayer.name());
            return paddedRank + paddedName;
        }

        // 16 limit
        String paddedRank = String.format("%06d", Integer.MAX_VALUE - weight);
        String truncatedName = fPlayer.name().substring(0, Math.min(fPlayer.name().length(), 10));
        String paddedName = String.format("%-10s", truncatedName);
        return paddedRank + paddedName;
    }

}