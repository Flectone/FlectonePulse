package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.MessageModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class Module extends AbstractModule {

    private final Config.Module config;
    private final Permission permission;

    @Inject
    public Module(FileResolver fileResolver) {
        this.config = fileResolver.getConfig().getModule();
        this.permission = fileResolver.getPermission();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission.getModule());

        addChildren(IntegrationModule.class);
        addChildren(CommandModule.class);
        addChildren(MessageModule.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }
}
