package net.flectone.pulse.module.message.sidebar;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class MinecraftSidebarModule extends SidebarModule {

    private final List<UUID> playerSidebars = new ObjectArrayList<>();
    private final Map<UUID, List<String>> playerLegacySidebarContent = new Object2ObjectOpenHashMap<>();

    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final PacketProvider packetProvider;
    private final PermissionChecker permissionChecker;

    @Inject
    public MinecraftSidebarModule(FileFacade fileFacade,
                                  TaskScheduler taskScheduler,
                                  MessagePipeline messagePipeline,
                                  ListenerRegistry listenerRegistry,
                                  FPlayerService fPlayerService,
                                  PacketSender packetSender,
                                  PacketProvider packetProvider,
                                  PermissionChecker permissionChecker) {
        super(fileFacade, taskScheduler, listenerRegistry, fPlayerService);

        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.packetProvider = packetProvider;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public void remove(FPlayer fPlayer) {
        if (!playerSidebars.contains(fPlayer.uuid())) return;

        playerSidebars.remove(fPlayer.uuid());

        packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                getObjectiveName(fPlayer),
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                null,
                null
        ));
    }

    @Override
    public void update(FPlayer fPlayer) {
        if (!playerSidebars.contains(fPlayer.uuid())) {
            create(fPlayer);
        }

        send(fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE);
    }

    @Override
    public void create(FPlayer fPlayer) {
        remove(fPlayer);

        playerSidebars.add(fPlayer.uuid());

        send(fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE);
    }

    public void send(FPlayer fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode objectiveMode) {
        taskScheduler.runRegion(fPlayer, () -> {
            if (!permissionChecker.check(fPlayer, permission())) {
                remove(fPlayer);
                return;
            }

            if (isModuleDisabledFor(fPlayer)) return;

            String format = getNextMessage(fPlayer, config().random());
            if (format == null) return;

            String[] lines = format.split("<br>");
            if (lines.length == 0) return;

            String objectiveName = getObjectiveName(fPlayer);
            MessageContext titleContext = messagePipeline.createContext(fPlayer, lines[0]);
            Component title = messagePipeline.build(titleContext);

            packetSender.send(fPlayer, new WrapperPlayServerScoreboardObjective(
                    objectiveName,
                    objectiveMode,
                    title,
                    WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
                    ScoreFormat.blankScore()
            ));

            packetSender.send(fPlayer, new WrapperPlayServerDisplayScoreboard(
                    1, // 1 = sidebar, 0 = list, 2 = belowName
                    objectiveName
            ));

            if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_3)) {
                modernSidebarLines(fPlayer, objectiveName, lines);
            } else {
                legacySidebarLines(fPlayer, objectiveName, lines);
            }
        });
    }

    private void modernSidebarLines(FPlayer fPlayer, String objectiveName, String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            int lineIndex = i - 1;

            String lineId = getLineId(lineIndex, fPlayer);
            MessageContext lineContext = messagePipeline.createContext(fPlayer, lines[i]);
            Component line = messagePipeline.build(lineContext);

            packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                    lineId,
                    WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                    objectiveName,
                    lines.length - lineIndex,
                    line,
                    ScoreFormat.blankScore()
            ));
        }
    }

    private void legacySidebarLines(FPlayer fPlayer, String objectiveName, String[] lines) {
        List<String> content = playerLegacySidebarContent.getOrDefault(fPlayer.uuid(), new ObjectArrayList<>(15));

        for (int i = 0; i < content.size(); i++) {
            String oldLine = content.get(i);
            packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                    oldLine,
                    WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                    objectiveName,
                    lines.length - i,
                    null,
                    null
            ));
        }

        for (int i = 1; i < lines.length; i++) {
            int lineIndex = i - 1;

            MessageContext lineContext = messagePipeline.createContext(fPlayer, lines[i]);
            String line = messagePipeline.buildLegacy(lineContext);

            packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                    line,
                    WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                    objectiveName,
                    lines.length - lineIndex,
                    null,
                    null
            ));

            if (lineIndex < content.size()) {
                content.set(lineIndex, line);
            } else {
                content.add(line);
            }
        }

        playerLegacySidebarContent.put(fPlayer.uuid(), content);
    }
}