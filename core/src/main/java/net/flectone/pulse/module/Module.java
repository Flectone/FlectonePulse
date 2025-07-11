package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.MessageModule;

@Singleton
public class Module extends AbstractModule {

    private final Config.Module config;
    private final Permission permission;

    @Inject
    public Module(FileResolver fileResolver) {
        config = fileResolver.getConfig().getModule();
        permission = fileResolver.getPermission();

        addChildren(CommandModule.class);
        addChildren(IntegrationModule.class);
        addChildren(MessageModule.class);
    }

    @Override
    public void reload() {
        registerModulePermission(permission.getModule());
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }
}
