package net.flectone.pulse.module.message.tab;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.tab.footer.MinecraftFooterModule;
import net.flectone.pulse.module.message.tab.header.MinecraftHeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
public class MinecraftTabModule extends TabModule {

    @Inject
    public MinecraftTabModule(FileFacade fileFacade) {
        super(fileFacade);
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return super.childrenBuilder().add(
                MinecraftFooterModule.class,
                MinecraftHeaderModule.class,
                MinecraftPlayerlistnameModule.class
        );
    }

}
