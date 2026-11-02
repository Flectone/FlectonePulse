package net.flectone.pulse.module.message.sidebar;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.message.format.condition.ConditionModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.render.HytaleHudService;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class HytaleSidebarModule extends SidebarModuleImpl {

    private final PermissionChecker permissionChecker;
    private final MessagePipeline messagePipeline;
    private final ModuleController moduleController;
    private final HytaleHudService hudService;

    @Inject
    public HytaleSidebarModule(FileFacade fileFacade,
                               TaskScheduler taskScheduler,
                               ListenerRegistry listenerRegistry,
                               FPlayerService fPlayerService,
                               PermissionChecker permissionChecker,
                               MessagePipeline messagePipeline,
                               ModuleController moduleController,
                               RandomGenerator randomUtil,
                               SocialService socialService,
                               ConditionModule conditionModule,
                               HytaleHudService hudService) {
        super(fileFacade, taskScheduler, listenerRegistry, fPlayerService, randomUtil, socialService, conditionModule);

        this.permissionChecker = permissionChecker;
        this.messagePipeline = messagePipeline;
        this.moduleController = moduleController;
        this.hudService = hudService;
    }

    @Override
    public void remove(FPlayer fPlayer) {
        hudService.removeSidebar(fPlayer);
    }

    @Override
    public void update(FPlayer fPlayer) {
        if (!permissionChecker.check(fPlayer, permission()) || moduleController.isDisabledFor(this, fPlayer)) {
            remove(fPlayer);
            return;
        }

        List<Component> lines = createLines(fPlayer);
        if (lines == null) return;

        hudService.updateSidebar(fPlayer, lines, config());
    }

    @Override
    public void create(FPlayer fPlayer) {
        if (!permissionChecker.check(fPlayer, permission())) {
            remove(fPlayer);
            return;
        }

        if (moduleController.isDisabledFor(this, fPlayer)) return;

        List<Component> lines = createLines(fPlayer);
        if (lines == null) {
            remove(fPlayer);
            return;
        }

        hudService.updateSidebar(fPlayer, lines, config());
    }

    private @Nullable List<Component> createLines(FPlayer fPlayer) {
        String format = getNextFormat(fPlayer);
        if (format == null) return null;

        String[] lines = format.split("<br>");
        if (lines.length == 0) return null;

        List<Component> components = new ArrayList<>(lines.length);

        for (String line : lines) {
            components.add(messagePipeline.build(MessageContext.builder()
                    .module(this.name())
                    .sender(fPlayer)
                    .message(line)
                    .build()
            ));
        }

        return components;
    }

}
