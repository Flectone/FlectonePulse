package net.flectone.pulse.module.message.format.scoreboard;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.format.scoreboard.model.Team;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MinecraftScoreboardModule extends ScoreboardModule {

    private final Map<UUID, Team> uuidTeamMap = new ConcurrentHashMap<>();

    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final Provider<IntegrationModule> integrationModuleProvider;

    @Inject
    public MinecraftScoreboardModule(FileFacade fileFacade,
                                     TaskScheduler taskScheduler,
                                     MessagePipeline messagePipeline,
                                     PacketSender packetSender,
                                     PacketProvider packetProvider,
                                     ListenerRegistry listenerRegistry,
                                     Provider<IntegrationModule> integrationModuleProvider) {
        super(fileFacade, listenerRegistry);
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.integrationModuleProvider = integrationModuleProvider;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(fPlayer -> {
                if (!uuidTeamMap.containsKey(fPlayer.uuid())) return;

                // new info
                Team newTeam = createTeam(fPlayer);
                sendPacket(newTeam, WrapperPlayServerTeams.TeamMode.UPDATE);

                // update info
                uuidTeamMap.put(fPlayer.uuid(), newTeam);

            }, ticker.period());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        uuidTeamMap.values().forEach(team -> sendPacket(team, WrapperPlayServerTeams.TeamMode.REMOVE));
        uuidTeamMap.clear();
    }

    @Override
    public void create(FPlayer fPlayer, boolean skipCacheTeam) {

        taskScheduler.runRegion(fPlayer, () -> {
            if (isModuleDisabledFor(fPlayer)) return;

            if (!skipCacheTeam) {
                uuidTeamMap.values().forEach(cacheTeam ->
                        packetSender.send(fPlayer, new WrapperPlayServerTeams(cacheTeam.name(), WrapperPlayServerTeams.TeamMode.CREATE, cacheTeam.info(), List.of(cacheTeam.owner())))
                );
            }

            Team team = createTeam(fPlayer);
            sendPacket(team, WrapperPlayServerTeams.TeamMode.CREATE);

            uuidTeamMap.put(fPlayer.uuid(), team);
        });
    }

    public boolean hasTeam(FPlayer fPlayer) {
        return uuidTeamMap.containsKey(fPlayer.uuid());
    }

    @Override
    public void remove(FPlayer fPlayer) {
        taskScheduler.runAsync(() -> {
            if (isModuleDisabledFor(fPlayer)) return;

            Team team = uuidTeamMap.get(fPlayer.uuid());
            if (team == null) return;

            uuidTeamMap.remove(fPlayer.uuid());
            sendPacket(team, WrapperPlayServerTeams.TeamMode.REMOVE);
        });
    }

    private Team createTeam(FPlayer fPlayer) {
        String teamName = getSortedName(fPlayer);
        Component displayName = Component.text(teamName);

        Component prefix = Component.empty();
        if (!config().prefix().isEmpty()) {
            MessageContext prefixContext = messagePipeline.createContext(fPlayer, config().prefix())
                    .addFlag(MessageFlag.INVISIBLE_NAME, false);
            prefix = messagePipeline.build(prefixContext);
        }

        Component suffix = Component.empty();
        if (!config().suffix().isEmpty()) {
            MessageContext suffixContext = messagePipeline.createContext(fPlayer, config().suffix())
                    .addFlag(MessageFlag.INVISIBLE_NAME, false);
            suffix = messagePipeline.build(suffixContext);
        }

        WrapperPlayServerTeams.NameTagVisibility nameTagVisibility = config().nameVisible()
                ? WrapperPlayServerTeams.NameTagVisibility.ALWAYS
                : WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
        WrapperPlayServerTeams.CollisionRule collisionRule = WrapperPlayServerTeams.CollisionRule.ALWAYS;

        MessageContext colorContext = messagePipeline.createContext(fPlayer, config().color());
        Component colorComponent = messagePipeline.build(colorContext);
        TextColor color = colorComponent.color();

        WrapperPlayServerTeams.OptionData optionData = WrapperPlayServerTeams.OptionData.NONE;

        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                displayName,
                prefix,
                suffix,
                nameTagVisibility,
                collisionRule,
                color == null ? NamedTextColor.WHITE : NamedTextColor.nearestTo(color),
                optionData
        );

        return new Team(teamName, fPlayer.name(), info);
    }

    private void sendPacket(Team team, WrapperPlayServerTeams.TeamMode teamMode) {
        packetSender.send(new WrapperPlayServerTeams(team.name(), teamMode, team.info(), List.of(team.owner())));
    }

    public String getSortedName(FPlayer fPlayer) {
        int weight = integrationModuleProvider.get().getGroupWeight(fPlayer);

        // 32767 limit
        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
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
