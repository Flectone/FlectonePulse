package net.flectone.pulse.module.message.format.scoreboard;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
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
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScoreboardModule extends AbstractModule {

    private final Map<UUID, Team> uuidTeamMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
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
        super.onDisable();

        uuidTeamMap.values().forEach(team -> sendPacket(team, WrapperPlayServerTeams.TeamMode.REMOVE));
        uuidTeamMap.clear();
    }

    @Override
    public Message.Format.Scoreboard config() {
        return fileFacade.message().format().scoreboard();
    }

    @Override
    public Permission.Message.Format.Scoreboard permission() {
        return fileFacade.permission().message().format().scoreboard();
    }

    @Async
    public void create(FPlayer fPlayer, boolean skipCacheTeam) {
        if (isModuleDisabledFor(fPlayer)) return;

        if (!skipCacheTeam) {
            uuidTeamMap.values().forEach(cacheTeam ->
                    packetSender.send(fPlayer, new WrapperPlayServerTeams(cacheTeam.name(), WrapperPlayServerTeams.TeamMode.CREATE, cacheTeam.info(), List.of(cacheTeam.owner())))
            );
        }

        Team team = createTeam(fPlayer);
        sendPacket(team, WrapperPlayServerTeams.TeamMode.CREATE);

        uuidTeamMap.put(fPlayer.getUuid(), team);
    }

    public boolean hasTeam(FPlayer fPlayer) {
        return uuidTeamMap.containsKey(fPlayer.getUuid());
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
        if (!config().prefix().isEmpty()) {
            prefix = messagePipeline.builder(fPlayer, config().prefix())
                    .flag(MessageFlag.INVISIBLE_NAME, false)
                    .build();
        }

        Component suffix = Component.empty();
        if (!config().suffix().isEmpty()) {
            suffix = messagePipeline.builder(fPlayer, config().suffix())
                    .flag(MessageFlag.INVISIBLE_NAME, false)
                    .build();
        }

        WrapperPlayServerTeams.NameTagVisibility nameTagVisibility = config().nameVisible()
                ? WrapperPlayServerTeams.NameTagVisibility.ALWAYS
                : WrapperPlayServerTeams.NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
        WrapperPlayServerTeams.CollisionRule collisionRule = WrapperPlayServerTeams.CollisionRule.ALWAYS;
        TextColor color = messagePipeline.builder(fPlayer, config().color()).build().color();
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

        return new Team(teamName, fPlayer.getName(), info);
    }

    private void sendPacket(Team team, WrapperPlayServerTeams.TeamMode teamMode) {
        packetSender.send(new WrapperPlayServerTeams(team.name(), teamMode, team.info(), List.of(team.owner())));
    }

}
