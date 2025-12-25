package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.MessageModule;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Module extends AbstractModule {

    private final FileFacade fileFacade;

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                IntegrationModule.class,
                CommandModule.class,
                MessageModule.class
        );
    }

    @Override
    public Config.Module config() {
        return fileFacade.config().module();
    }

    @Override
    public PermissionSetting permission() {
        return fileFacade.permission().module();
    }

}
