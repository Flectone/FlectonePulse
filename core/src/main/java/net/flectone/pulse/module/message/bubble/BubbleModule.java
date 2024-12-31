package net.flectone.pulse.module.message.bubble;

import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import org.jetbrains.annotations.NotNull;

public abstract class BubbleModule extends AbstractModule {

    private final Message.Bubble message;
    private final Permission.Message.Bubble permission;

    public BubbleModule(FileManager fileManager) {
        message = fileManager.getMessage().getBubble();
        permission = fileManager.getPermission().getMessage().getBubble();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void add(@NotNull FPlayer fPlayer, @NotNull String inputString);
}
