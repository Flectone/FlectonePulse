package net.flectone.pulse.module.message.sidebar;

import lombok.Getter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Ticker;
import net.flectone.pulse.module.AbstractModuleListMessage;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;

public abstract class SidebarModule extends AbstractModuleListMessage<Localization.Message.Sidebar> {

    @Getter private final Message.Sidebar message;
    private final Permission.Message.Sidebar permission;

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;

    public SidebarModule(FileResolver fileResolver,
                         FPlayerService fPlayerService,
                         TaskScheduler taskScheduler) {
        super(localization -> localization.getMessage().getSidebar());

        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;

        message = fileResolver.getMessage().getSidebar();
        permission = fileResolver.getPermission().getMessage().getSidebar();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        Ticker ticker = message.getTicker();
        if (ticker.isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::send), ticker.getPeriod());
        }
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void send(FPlayer fPlayer);
}
