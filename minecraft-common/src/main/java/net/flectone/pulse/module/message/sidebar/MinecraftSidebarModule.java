package net.flectone.pulse.module.message.sidebar;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.MinecraftPacketSender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Singleton
public class MinecraftSidebarModule extends SidebarModuleImpl {

    private final List<UUID> playerSidebars = new ObjectArrayList<>();
    private final Map<UUID, List<String>> playerSidebarContent = new Object2ObjectOpenHashMap<>();

    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final MinecraftPacketSender packetSender;
    private final PermissionChecker permissionChecker;
    private final ModuleController moduleController;
    private final SocialService socialService;
    private final boolean isNewerThanOrEqualsV_1_20_3;

    @Inject
    public MinecraftSidebarModule(FileFacade fileFacade,
                                  TaskScheduler taskScheduler,
                                  MessagePipeline messagePipeline,
                                  ListenerRegistry listenerRegistry,
                                  FPlayerService fPlayerService,
                                  MinecraftPacketSender packetSender,
                                  MinecraftPacketProvider packetProvider,
                                  PermissionChecker permissionChecker,
                                  ModuleController moduleController,
                                  RandomGenerator randomUtil,
                                  SocialService socialService) {
        super(fileFacade, taskScheduler, listenerRegistry, fPlayerService, randomUtil, socialService);

        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.permissionChecker = permissionChecker;
        this.moduleController = moduleController;
        this.socialService = socialService;
        this.isNewerThanOrEqualsV_1_20_3 = packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_3);
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
        taskScheduler.runAsync(() -> {
            if (!permissionChecker.check(fPlayer, permission()) || !socialService.isSetting(fPlayer, name())) {
                remove(fPlayer);
                return;
            }

            if (moduleController.isDisabledFor(this, fPlayer)) return;

            String format = getNextMessage(fPlayer, config().random());
            if (format == null) return;

            String[] lines = format.split("<br>");
            if (lines.length == 0) return;

            String objectiveName = getObjectiveName(fPlayer);
            Component title = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .message(lines[0])
                    .build()
            );

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

            List<String> oldContent = playerSidebarContent.get(fPlayer.uuid());
            List<String> content = isNewerThanOrEqualsV_1_20_3
                    ? modernSidebarLines(fPlayer, objectiveName, lines)
                    : legacySidebarLines(fPlayer, objectiveName, lines);

            if (oldContent != null && oldContent.size() > content.size()) {
                for (int i = content.size(); i < oldContent.size(); i++) {
                    String oldLine = oldContent.get(i);
                    if (isNewerThanOrEqualsV_1_20_3) {
                        packetSender.send(fPlayer, new WrapperPlayServerResetScore(oldLine, objectiveName));
                    } else {
                        packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                                oldLine,
                                WrapperPlayServerUpdateScore.Action.REMOVE_ITEM,
                                objectiveName,
                                lines.length - i,
                                null,
                                null
                        ));
                    }
                }
            }

            playerSidebarContent.put(fPlayer.uuid(), content);
        });
    }

    private List<String> modernSidebarLines(FPlayer fPlayer, String objectiveName, String[] lines) {
        List<String> content = new ObjectArrayList<>(15);

        for (int i = 1; i < lines.length && i < 16; i++) {
            int lineIndex = i - 1;

            String lineId = getLineId(lineIndex, fPlayer);

            Component line = messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .message(lines[i])
                    .build()
            );

            packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                    lineId,
                    WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                    objectiveName,
                    lines.length - lineIndex,
                    line,
                    ScoreFormat.blankScore()
            ));

            content.add(lineId);
        }

        return content;
    }

    private List<String> legacySidebarLines(FPlayer fPlayer, String objectiveName, String[] lines) {
        List<String> content = new ObjectArrayList<>(15);

        for (int i = 1; i < lines.length && i < 16; i++) {
            int lineIndex = i - 1;

            String line = messagePipeline.buildLegacy(MessageContext.builder()
                    .sender(fPlayer)
                    .message(lines[i])
                    .build()
            );

            packetSender.send(fPlayer, new WrapperPlayServerUpdateScore(
                    line,
                    WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                    objectiveName,
                    lines.length - lineIndex,
                    null,
                    null
            ));

            content.add(line);
        }

        return content;
    }
}