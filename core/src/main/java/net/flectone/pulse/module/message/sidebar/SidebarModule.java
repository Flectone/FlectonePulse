package net.flectone.pulse.module.message.sidebar;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Ticker;
import net.flectone.pulse.module.AbstractModuleListLocalization;
import net.flectone.pulse.module.message.sidebar.listener.SidebarPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;

import java.util.List;
import java.util.UUID;

public abstract class SidebarModule extends AbstractModuleListLocalization<Localization.Message.Sidebar> {

    private final FileFacade fileFacade;
    private final TaskScheduler taskScheduler;
    private final ListenerRegistry listenerRegistry;
    private final FPlayerService fPlayerService;

    protected SidebarModule(FileFacade fileFacade,
                            TaskScheduler taskScheduler,
                            ListenerRegistry listenerRegistry,
                            FPlayerService fPlayerService) {
        this.fileFacade = fileFacade;
        this.taskScheduler = taskScheduler;
        this.listenerRegistry = listenerRegistry;
        this.fPlayerService = fPlayerService;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerRegionTimer(this::update, ticker.period());
        }

        listenerRegistry.register(SidebarPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        fPlayerService.getOnlineFPlayers().forEach(this::remove);
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
