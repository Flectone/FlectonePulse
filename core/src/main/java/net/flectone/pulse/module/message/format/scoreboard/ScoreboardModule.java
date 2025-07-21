package net.flectone.pulse.module.message.format.scoreboard;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.scoreboard.model.Team;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
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
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public ScoreboardModule(FileResolver fileResolver,
                            FPlayerService fPlayerService,
                            TaskScheduler taskScheduler,
                            MessagePipeline messagePipeline,
                            PacketSender packetSender,
                            EventProcessRegistry eventProcessRegistry) {
        this.message = fileResolver.getMessage().getFormat().getScoreboard();
        this.permission = fileResolver.getPermission().getMessage().getFormat().getScoreboard();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void onEnable() {
        fPlayerService.getPlatformFPlayers().forEach(this::create);

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

        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_LOAD, this::create);
        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_QUIT, this::remove);
    }

    @Override
    public void onDisable() {
        uuidTeamMap.values().forEach(team -> sendPacket(team, WrapperPlayServerTeams.TeamMode.REMOVE));
        uuidTeamMap.clear();
    }

    public void create(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Team team = createTeam(fPlayer);
        sendPacket(team, WrapperPlayServerTeams.TeamMode.CREATE);
        uuidTeamMap.put(fPlayer.getUuid(), team);

        uuidTeamMap.forEach((uuid, cacheTeam) ->
                packetSender.send(fPlayer, new WrapperPlayServerTeams(cacheTeam.name(), WrapperPlayServerTeams.TeamMode.CREATE, cacheTeam.info(), List.of(cacheTeam.owner())))
        );
    }

    public void remove(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

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
