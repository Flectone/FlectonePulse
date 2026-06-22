package net.flectone.pulse.module.message.scoreboard.objective;

import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.scoreboard.objective.belowname.MinecraftBelownameModule;
import net.flectone.pulse.module.message.scoreboard.objective.tabname.MinecraftTabnameModule;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jspecify.annotations.NonNull;

@Singleton
public class MinecraftObjectiveModule extends ObjectiveModule {

    private final MinecraftPacketSender packetSender;
    private final MessagePipeline messagePipeline;

    @Inject
    public MinecraftObjectiveModule(FileFacade fileFacade,
                                    MinecraftPacketSender packetSender,
                                    MessagePipeline messagePipeline) {
        super(fileFacade);
        this.packetSender = packetSender;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return super.childrenBuilder().add(
                MinecraftBelownameModule.class,
                MinecraftTabnameModule.class
        );
    }

    public void createObjective(FPlayer fPlayer, Component displayName, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        String objectiveName = getObjectiveName(fPlayer, scoreboardPosition);

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

    public void updateObjective(FPlayer fPlayer, FPlayer fObjective, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        String objectiveName = getObjectiveName(fPlayer, scoreboardPosition);

        packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                fObjective.name(),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                objectiveName,
                -1,
                Component.text(fPlayer.name()),
                ScoreFormat.fixedScore(scoreFormat)
        ));
    }

    public void removeObjective(FPlayer fPlayer, ScoreboardPosition scoreboardPosition) {
        String objectiveName = getObjectiveName(fPlayer, scoreboardPosition);

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                Component.empty(),
                null,
                null
        ));
    }

    public Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String score, String format) {
        return buildFormat(fPlayer, fReceiver, score, format, true);
    }

    public Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String score, String format, boolean colorContextSender) {
        return messagePipeline.build(MessageContext.builder()
                .sender(fPlayer)
                .receiver(fReceiver)
                .message(format)
                .flag(MessageFlag.COLOR_CONTEXT_SENDER, colorContextSender)
                .tagResolver(messagePipeline.resolver("score", (_, _) ->
                        Tag.inserting(messagePipeline.build(MessageContext.builder()
                                        .sender(fPlayer)
                                        .receiver(fReceiver)
                                        .message(score)
                                        .flag(MessageFlag.COLOR_CONTEXT_SENDER, colorContextSender)
                                        .build()
                                )
                        )
                ))
                .build()
        );
    }

    private String getObjectiveName(FPlayer fPlayer, ScoreboardPosition scoreboardPosition) {
        return scoreboardPosition.name() + "_" + fPlayer.id();
    }

}
