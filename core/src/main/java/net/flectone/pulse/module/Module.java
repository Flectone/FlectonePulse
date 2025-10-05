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

    private final FileResolver fileResolver;

    @Inject
    public Module(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChildren(IntegrationModule.class);
        addChildren(CommandModule.class);
        addChildren(MessageModule.class);
    }

    @Override
    public Config.Module config() {
        return fileResolver.getConfig().getModule();
    }

    @Override
    public Permission.IPermission permission() {
        return fileResolver.getPermission().getModule();
    }

}
