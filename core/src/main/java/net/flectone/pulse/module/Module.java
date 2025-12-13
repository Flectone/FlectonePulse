package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.MessageModule;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Module extends AbstractModule {

    private final FileResolver fileResolver;

    @Override
    public void configureChildren() {
        super.configureChildren();

        addChild(IntegrationModule.class);
        addChild(CommandModule.class);
        addChild(MessageModule.class);
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
