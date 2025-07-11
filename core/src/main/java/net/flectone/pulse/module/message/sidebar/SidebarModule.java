package net.flectone.pulse.module.message.sidebar;

import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.module.message.sidebar.listener.SidebarPacketListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.PacketSender;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

@Singleton
public class SidebarModule extends AbstractModuleListMessage<Localization.Message.Sidebar> {

    @Getter private final Message.Sidebar message;
    private final Permission.Message.Sidebar permission;

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final MessagePipeline messagePipeline;
    private final PacketSender packetSender;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SidebarModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         TaskScheduler taskScheduler,
                         MessagePipeline messagePipeline,
                         PacketSender packetSender,
                         ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSidebar());

        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.messagePipeline = messagePipeline;
        this.packetSender = packetSender;
        this.listenerRegistry = listenerRegistry;

        message = fileResolver.getMessage().getSidebar();
        permission = fileResolver.getPermission().getMessage().getSidebar();
    }

    @Override
    public void onDisable() {
        fPlayerService.getFPlayers().forEach(this::remove);
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        fPlayerService.getFPlayers().forEach(this::create);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::update), ticker.getPeriod());
        }

        listenerRegistry.register(SidebarPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
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
        send(fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE);
    }

    @Async
    public void send(FPlayer fPlayer, WrapperPlayServerScoreboardObjective.ObjectiveMode objectiveMode) {
        if (checkModulePredicates(fPlayer)) return;

        String format = getNextMessage(fPlayer, getMessage().isRandom());
        if (format == null) return;

        String[] lines = format.split("<br>");
        if (lines.length == 0) return;

        String objectiveName = createObjectiveName(fPlayer);
        Component title = messagePipeline.builder(fPlayer, lines[0]).build();

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

        for (int i = 1; i < lines.length; i++) {
            Component line = messagePipeline.builder(fPlayer, lines[i]).build();

            int lineIndex = i - 1;
            String lineId = createLineId(lineIndex, fPlayer);

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

    @Override
    public List<String> getAvailableMessages(FPlayer fPlayer) {
        return joinMultiList(resolveLocalization(fPlayer).getValues());
    }

    private String createObjectiveName(FPlayer fPlayer) {
        return "sb_" + fPlayer.getUuid();
    }

    private String createLineId(int index, FPlayer fPlayer) {
        return "ln_" + index + "_" + fPlayer.getUuid();
    }
}
