package net.flectone.pulse.module.message.sidebar;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.ModuleListLocalization;
import net.flectone.pulse.module.message.sidebar.listener.SidebarPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SidebarModule implements ModuleListLocalization<Localization.Message.Sidebar> {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;
    private final RandomUtil randomUtil;

    protected SidebarModule(FileFacade fileFacade,
                            TaskScheduler taskScheduler,
                            ListenerRegistry listenerRegistry,
                            FPlayerService fPlayerService,
                            RandomUtil randomUtil) {
        this.fileFacade = fileFacade;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.fPlayerService = fPlayerService;
        this.randomUtil = randomUtil;
    }

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::update, ticker.period());
        }

        listenerRegistry.register(SidebarPulseListener.class);
    }

    @Override
    public void onDisable() {
        messageIndexMap.clear();

        fPlayerService.getOnlineFPlayers().forEach(this::remove);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_SIDEBAR;
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

    @Override
    public int getPlayerIndexOrDefault(int id, int defaultIndex) {
        return messageIndexMap.getOrDefault(id, defaultIndex);
    }

    @Override
    public int nextInt(int start, int end) {
        return randomUtil.nextInt(start, end);
    }

    @Override
    public void savePlayerIndex(int id, int playerIndex) {
        messageIndexMap.put(id, playerIndex);
    }

    public abstract void remove(FPlayer fPlayer);

    public abstract void update(FPlayer fPlayer);

    public void create(UUID uuid) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        create(fPlayer);
    }

    public abstract void create(FPlayer fPlayer);

    protected String getObjectiveName(FPlayer fPlayer) {
        return "sb_" + fPlayer.uuid();
    }

    protected String getLineId(int index, FPlayer fPlayer) {
        return "ln_" + index + "_" + fPlayer.uuid();
    }

}
