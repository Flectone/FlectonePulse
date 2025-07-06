package net.flectone.pulse.module.message.objective;

import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;

public abstract class ObjectiveModule extends AbstractModule {

    private final Message.Objective message;
    private final Permission.Message.Objective permission;

    public ObjectiveModule(FileResolver fileResolver) {
        message = fileResolver.getMessage().getObjective();
        permission = fileResolver.getPermission().getMessage().getObjective();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }
}
