package net.flectone.pulse.module.message.sidebar;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.module.message.sidebar.listener.SidebarPulseListener;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.sender.PacketSender;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.kyori.adventure.text.Component;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SidebarModule extends AbstractModuleListLocalization<Localization.Message.Sidebar> {

    private final Map<UUID, List<String>> playerSidebar = new HashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final ListenerRegistry listenerRegistry;
    private final PacketProvider packetProvider;

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::update), ticker.period());
        }

        listenerRegistry.register(SidebarPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        fPlayerService.getOnlineFPlayers().forEach(this::remove);
        // no clear playerSidebar map for next sidebars
    }

    @Override
    public MessageType messageType() {
        return MessageType.SIDEBAR;
    }

    @Override
    public Message.Sidebar config() {
        return fileFacade.message().sidebar();
    }

    @Override
    public Permission.Message.Sidebar permission() {
        return fileFacade.permission().message().sidebar();
    }

    @Override
    public Localization.Message.Sidebar localization(FEntity sender) {
        return fileFacade.localization(sender).message().sidebar();
    }

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(localization(fPlayer).values());
    }

    public void remove(FPlayer fPlayer) {
        packetSender.send(fPlayer,  new WrapperPlayServerScoreboardObjective(
                createObjectiveName(fPlayer),
                WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
                null,
                null
        ));
    }

    public void update(FPlayer fPlayer) {
        send(fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE);
    }

    public void create(UUID uuid) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        create(fPlayer);
    }

    public void create(FPlayer fPlayer) {
        remove(fPlayer);
        send(fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE);
    }

    @Async
    public void send(FPlayer fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode objectiveMode) {
        if (isModuleDisabledFor(fPlayer)) return;

        String format = getNextMessage(fPlayer, config().random());
        if (format == null) return;

        String[] lines = format.split("<br>");
        if (lines.length == 0) return;

        String objectiveName = createObjectiveName(fPlayer);
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

    }

    private void modernSidebarLines(FPlayer fPlayer, String objectiveName, String[] lines) {
        for (int i = 1; i < lines.length; i++) {
            int lineIndex = i - 1;

            String lineId = createLineId(lineIndex, fPlayer);
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
        List<String> sidebars = playerSidebar.getOrDefault(fPlayer.getUuid(), new ArrayList<>(15));

        for (int i = 0; i < sidebars.size(); i++) {
            String oldLine = sidebars.get(i);
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

            if (lineIndex < sidebars.size()) {
                sidebars.set(lineIndex, line);
            } else {
                sidebars.add(line);
            }
        }

        playerSidebar.put(fPlayer.getUuid(), sidebars);
    }

    private String createObjectiveName(FPlayer fPlayer) {
        return "sb_" + fPlayer.getUuid();
    }

    private String createLineId(int index, FPlayer fPlayer) {
        return "ln_" + index + "_" + fPlayer.getUuid();
    }
}
