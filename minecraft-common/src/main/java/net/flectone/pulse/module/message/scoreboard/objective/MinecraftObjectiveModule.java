package net.flectone.pulse.module.message.scoreboard.objective;

import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MinecraftObjectiveModule extends ObjectiveModuleImpl {

    private final Set<String> objectives = ConcurrentHashMap.newKeySet();
    private final Map<String, Map<String, Integer>> sentScores = new ConcurrentHashMap<>();

    private final MinecraftPacketSender packetSender;

    @Inject
    public MinecraftObjectiveModule(FileFacade fileFacade,
                                    MessagePipeline messagePipeline,
                                    MinecraftPacketSender packetSender) {
        super(fileFacade, messagePipeline);
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        objectives.clear();
        sentScores.clear();
    }

    @Override
    public void createObjective(FPlayer fPlayer, Component displayName, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        String objectiveName = getObjectiveName(fPlayer, scoreboardPosition);
        if (!objectives.add(objectiveName)) return;

        sentScores.remove(objectiveName);

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                displayName,
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                ScoreFormat.fixedScore(scoreFormat)
        ));

        packetSender.send(fPlayer, new WrapperPlayServerDisplayScoreboard(
                scoreboardPosition.ordinal(),
                objectiveName
        ));
    }

    @Override
    public void updateObjective(FPlayer fPlayer, FPlayer fObjective, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        String objectiveName = getObjectiveName(fPlayer, scoreboardPosition);

        int score = Objects.hashCode(scoreFormat);
        Integer sentScore = rememberScore(objectiveName, fObjective, score);
        if (sentScore != null && sentScore == score) return;

        packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                fObjective.name(),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                objectiveName,
                -1,
                Component.text(fObjective.name()),
                ScoreFormat.fixedScore(scoreFormat)
        ));
    }

    @Override
    public void removeObjective(FPlayer fPlayer, ScoreboardPosition scoreboardPosition) {
        String objectiveName = getObjectiveName(fPlayer, scoreboardPosition);

        sentScores.remove(objectiveName);
        sentScores.values().forEach(scores -> scores.remove(fPlayer.name()));

        if (!objectives.remove(objectiveName)) return;

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.empty(),
                null,
                null
        ));
    }

    @Override
    public void forgetScore(FPlayer fPlayer, FPlayer fObjective, ScoreboardPosition scoreboardPosition) {
        Map<String, Integer> scores = sentScores.get(getObjectiveName(fPlayer, scoreboardPosition));
        if (scores != null) {
            scores.remove(fObjective.name());
        }
    }

    private @Nullable Integer rememberScore(String objectiveName, FPlayer fObjective, int score) {
        return sentScores.computeIfAbsent(objectiveName, _ -> new ConcurrentHashMap<>())
                .put(fObjective.name(), score);
    }

    private String getObjectiveName(FPlayer fPlayer, ScoreboardPosition scoreboardPosition) {
        return "fp_" + scoreboardPosition.name() + "_" + fPlayer.id();
    }

}
