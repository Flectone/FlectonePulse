package net.flectone.pulse.module.message.format.scoreboard;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.listener.ScoreboardPulseListener;
import net.flectone.pulse.module.message.format.scoreboard.model.Team;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ScoreboardModule extends AbstractModule {

    private final Map<UUID, Team> uuidTeamMap = new ConcurrentHashMap<>();

    private final Message.Format.Scoreboard message;
    private final Permission.Message.Format.Scoreboard permission;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ScoreboardModule(FileResolver fileResolver,
                            FPlayerService fPlayerService,
                            TaskScheduler taskScheduler,
                            MessagePipeline messagePipeline,
                            PacketSender packetSender,
                            ListenerRegistry listenerRegistry) {
        this.message = fileResolver.getMessage().getFormat().getScoreboard();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getScoreboard();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> uuidTeamMap.keySet().forEach(uuid -> {
                FPlayer fPlayer = fPlayerService.getFPlayer(uuid);

                // new info
                Team newTeam = createTeam(fPlayer);
                sendPacket(newTeam, WrapperPlayServerTeams.TeamMode.UPDATE);

                // update info
                uuidTeamMap.put(uuid, newTeam);

            }), ticker.getPeriod());
        }

        listenerRegistry.register(ScoreboardPulseListener.class);
    }

    @Override
    public void onDisable() {
        uuidTeamMap.values().forEach(team -> sendPacket(team, WrapperPlayServerTeams.TeamMode.REMOVE));
        uuidTeamMap.clear();
    }

    @Async
    public void create(FPlayer fPlayer, boolean reload) {
        if (isModuleDisabledFor(fPlayer)) return;

        if (!reload) {
            uuidTeamMap.values().forEach(cacheTeam ->
                    packetSender.send(fPlayer, new WrapperPlayServerTeams(cacheTeam.name(), WrapperPlayServerTeams.TeamMode.CREATE, cacheTeam.info(), List.of(cacheTeam.owner())))
            );
        }

        Team team = createTeam(fPlayer);
        sendPacket(team, WrapperPlayServerTeams.TeamMode.CREATE);

        uuidTeamMap.put(fPlayer.getUuid(), team);
    }

    @Async
    public void remove(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        Team team = uuidTeamMap.get(fPlayer.getUuid());
        if (team == null) return;

        uuidTeamMap.remove(fPlayer.getUuid());
        sendPacket(team, WrapperPlayServerTeams.TeamMode.REMOVE);
    }

    private Team createTeam(FPlayer fPlayer) {
        String teamName = fPlayerService.getSortedName(fPlayer);
        Component displayName = Component.text(teamName);

        Component prefix = Component.empty();
        if (!message.getPrefix().isEmpty()) {
            prefix = messagePipeline.builder(fPlayer, message.getPrefix()).build();
        }

        Component suffix = Component.empty();
        if (!message.getSuffix().isEmpty()) {
            suffix = messagePipeline.builder(fPlayer, message.getSuffix()).build();
        }

        WrapperPlayServerTeams.NameTagVisibility nameTagVisibility = message.isNameVisible()
                ? WrapperPlayServerTeams.NameTagVisibility.ALWAYS
                : WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
        WrapperPlayServerTeams.CollisionRule collisionRule = WrapperPlayServerTeams.CollisionRule.ALWAYS;
        NamedTextColor color = (NamedTextColor) messagePipeline.builder(fPlayer, message.getColor()).build().color();
        WrapperPlayServerTeams.OptionData optionData = WrapperPlayServerTeams.OptionData.NONE;

        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                displayName,
                prefix,
                suffix,
                nameTagVisibility,
                collisionRule,
                color,
                optionData
        );

        return new Team(teamName, fPlayer.getName(), info);
    }

    private void sendPacket(Team team, WrapperPlayServerTeams.TeamMode teamMode) {
        packetSender.send(new WrapperPlayServerTeams(team.name(), teamMode, team.info(), List.of(team.owner())));
    }

}
