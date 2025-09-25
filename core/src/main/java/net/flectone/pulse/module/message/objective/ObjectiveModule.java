package net.flectone.pulse.module.message.objective;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Singleton
public class ObjectiveModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final PacketSender packetSender;

    @Inject
    public ObjectiveModule(FileResolver fileResolver,
                           PacketSender packetSender) {
        this.fileResolver = fileResolver;
        this.packetSender = packetSender;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        addChildren(BelownameModule.class);
        addChildren(TabnameModule.class);
    }

    @Override
    public Message.Objective config() {
        return fileResolver.getMessage().getObjective();
    }

    @Override
    public Permission.Message.Objective permission() {
        return fileResolver.getPermission().getMessage().getObjective();
    }

    public void createObjective(FPlayer fPlayer, @Nullable Component displayName, ScoreboardPosition scoreboardPosition) {
        removeObjective(fPlayer, scoreboardPosition);

        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                objectiveName,
                WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
                displayName == null ? Component.text(objectiveName) : displayName,
                WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                null
        ));

        packetSender.send(fPlayer, new WrapperPlayServerDisplayScoreboard(
                scoreboardPosition.ordinal(),
                objectiveName
        ));
    }

    public void updateObjective(FPlayer fPlayer, FPlayer fObjective, int score, ScoreboardPosition scoreboardPosition) {
        String objectiveName = scoreboardPosition.name() + fPlayer.getUuid();

        packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                fObjective.getName(),
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                objectiveName,
                score,
                Component.text(fPlayer.getName()),
                null
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
