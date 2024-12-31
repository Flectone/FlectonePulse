package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.MessageModule;

@Singleton
public class Module extends AbstractModule {

    private final Config.Module config;
    private final Permission permission;

    @Inject
    public Module(FileManager fileManager) {
        config = fileManager.getConfig().getModule();
        permission = fileManager.getPermission();
    }

    @Override
    public void reload() {
        registerModulePermission(permission.getModule());

        addChildren(CommandModule.class);
        addChildren(IntegrationModule.class);
        addChildren(MessageModule.class);
    }

    @Override
    public boolean isConfigEnable() {
        return config.isEnable();
    }
}
