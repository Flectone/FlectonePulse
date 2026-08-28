package net.flectone.pulse.module.message.sidebar;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.module.message.sidebar.listener.PulseSidebarListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SidebarModuleImpl implements SidebarModule {

    private final Map<Integer, Integer> messageIndexMap = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;
    private final RandomGenerator randomUtil;
    private final SocialService socialService;
    private final ConditionModule conditionModule;

    protected SidebarModuleImpl(FileFacade fileFacade,
                            TaskScheduler taskScheduler,
                            ListenerRegistry listenerRegistry,
                            FPlayerService fPlayerService,
                            RandomGenerator randomUtil,
                            SocialService socialService,
                            ConditionModule conditionModule) {
        this.fileFacade = fileFacade;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.fPlayerService = fPlayerService;
        this.randomUtil = randomUtil;
        this.socialService = socialService;
        this.conditionModule = conditionModule;
    }

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::update, ticker.period());
        }

        listenerRegistry.register(PulseSidebarListener.class);
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
    public Localization.Message.Sidebar localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().sidebar();
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

    @Override
    public void create(UUID uuid) {
        FPlayer fPlayer = fPlayerService.getFPlayer(uuid);
        create(fPlayer);
    }

    protected @Nullable String getNextFormat(FPlayer fPlayer) {
        String format = getNextMessage(fPlayer, config().random());
        if (format == null) return null;

        return conditionModule.replaceCondition(format, fPlayer);
    }

    protected String getObjectiveName(FPlayer fPlayer) {
        return "sb_" + fPlayer.uuid();
    }

    protected String getLineId(int index, FPlayer fPlayer) {
        return "ln_" + index + "_" + fPlayer.uuid();
    }

}
