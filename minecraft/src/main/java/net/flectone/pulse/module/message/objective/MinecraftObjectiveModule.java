package net.flectone.pulse.module.message.objective;

import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jspecify.annotations.NonNull;

@Singleton
public class MinecraftObjectiveModule extends ObjectiveModule {

    private final PacketSender packetSender;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessagePipeline messagePipeline;

    @Inject
    public MinecraftObjectiveModule(FileFacade fileFacade,
                                    PacketSender packetSender,
                                    PlatformPlayerAdapter platformPlayerAdapter,
                                    MessagePipeline messagePipeline) {
        super(fileFacade);
        this.packetSender = packetSender;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.messagePipeline = messagePipeline;
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                BelownameModule.class,
                TabnameModule.class
        );
    }

    public void createObjective(FPlayer fPlayer, Component displayName, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        removeObjective(fPlayer, scoreboardPosition);

        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

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

    public void updateObjective(FPlayer fPlayer, FPlayer fObjective, int score, Component scoreFormat, ScoreboardPosition scoreboardPosition) {
        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

        packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                fObjective.getName(),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                objectiveName,
                score,
                Component.text(fPlayer.getName()),
                ScoreFormat.fixedScore(scoreFormat)
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

    public Component buildFormat(FPlayer fPlayer, FPlayer fReceiver, String format, Mode mode) {
        int score = platformPlayerAdapter.getObjectiveScore(fPlayer.getUuid(), mode);

        MessageContext tabNameContext = messagePipeline.createContext(fPlayer, fReceiver, format)
                .addTagResolver(Placeholder.parsed("score", String.valueOf(score)));

        return messagePipeline.build(tabNameContext);
    }
}
