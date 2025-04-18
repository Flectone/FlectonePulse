package net.flectone.pulse.module.message.bubble;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.bubble.service.BubbleService;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BubbleModule extends AbstractModule {

    private final Message.Bubble message;
    private final Permission.Message.Bubble permission;

    private final BubbleService bubbleService;

    @Inject
    public BubbleModule(FileManager fileManager,
                        BubbleService bubbleService) {
        message = fileManager.getMessage().getBubble();
        permission = fileManager.getPermission().getMessage().getBubble();

        this.bubbleService = bubbleService;
    }

    @Override
    public void reload() {
        bubbleService.reload();

        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void add(@NotNull FPlayer fPlayer, @NotNull String inputString) {
        if (checkModulePredicates(fPlayer)) return;

        bubbleService.addMessage(fPlayer, inputString);
    }
}
