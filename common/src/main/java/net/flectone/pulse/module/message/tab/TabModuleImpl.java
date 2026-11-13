package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabModuleImpl implements TabModule {

    private final FileFacade fileFacade;

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> children = new LinkedHashSet<>(TabModule.super.children());
        children.add(FooterModule.class);
        children.add(HeaderModule.class);
        children.add(PlayerlistnameModule.class);
        return Collections.unmodifiableSet(children);
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_TAB;
    }

    @Override
    public Message.Tab config() {
        return fileFacade.message().tab();
    }

    @Override
    public Permission.Message.Tab permission() {
        return fileFacade.permission().message().tab();
    }

}
