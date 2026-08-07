package net.flectone.pulse.module;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.command.CommandModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.MessageModule;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModuleImpl implements Module {

    private final FileFacade fileFacade;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(Module.super.children());
        children.add(IntegrationModule.class);
        children.add(CommandModule.class);
        children.add(MessageModule.class);
        return Collections.unmodifiableSet(children);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MODULE;
    }

    @Override
    public Config.Internal config() {
        return fileFacade.config().internal();
    }

    @Override
    public PermissionSetting permission() {
        return fileFacade.permission().module();
    }

}
