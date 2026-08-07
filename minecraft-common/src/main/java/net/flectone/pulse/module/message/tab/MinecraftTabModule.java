package net.flectone.pulse.module.message.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.tab.footer.MinecraftFooterModule;
import net.flectone.pulse.module.message.tab.header.MinecraftHeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
public class MinecraftTabModule extends TabModuleImpl {

    @Inject
    public MinecraftTabModule(FileFacade fileFacade) {
        super(fileFacade);
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<Class<? extends ModuleSimple>> children = new LinkedHashSet<>(super.children());
        children.add(MinecraftFooterModule.class);
        children.add(MinecraftHeaderModule.class);
        children.add(MinecraftPlayerlistnameModule.class);
        return Collections.unmodifiableSet(children);
    }

}
