package net.flectone.pulse.module.message.objective;

import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;

public abstract class ObjectiveModule extends AbstractModule {

    private final Message.Objective message;
    private final Permission.Message.Objective permission;

    public ObjectiveModule(FileManager fileManager) {
        message = fileManager.getMessage().getObjective();
        permission = fileManager.getPermission().getMessage().getObjective();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }
}
