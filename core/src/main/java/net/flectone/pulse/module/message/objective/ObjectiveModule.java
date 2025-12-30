package net.flectone.pulse.module.message.objective;

import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ObjectiveModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final PacketSender packetSender;

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                BelownameModule.class,
                TabnameModule.class
        );
    }

    @Override
    public Message.Objective config() {
        return fileFacade.message().objective();
    }

    @Override
    public Permission.Message.Objective permission() {
        return fileFacade.permission().message().objective();
    }

    public void createObjective(FPlayer fPlayer, @Nullable Component displayName, @Nullable Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        removeObjective(fPlayer, scoreboardPosition);

        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                displayName != null ? displayName : Component.text(objectiveName),
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                scoreFormat != null ? ScoreFormat.fixedScore(displayName) : null
        ));

        packetSender.send(fPlayer, new WrapperPlayServerDisplayScoreboard(
                scoreboardPosition.ordinal(),
                objectiveName
        ));
    }

    public void updateObjective(FPlayer fPlayer, FPlayer fObjective, int score, @Nullable Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

        packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                fObjective.getName(),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                objectiveName,
                score,
                Component.text(fPlayer.getName()),
                scoreFormat != null ? ScoreFormat.fixedScore(scoreFormat) : null
        ));
    }

    public void removeObjective(FPlayer fPlayer, ScoreboardPosition scoreboardPosition) {
        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.empty(),
                null,
                null
        ));
    }

    public enum Mode {
        HEALTH,
        LEVEL,
        FOOD,
        PING,
        ARMOR,
        ATTACK
    }
}
