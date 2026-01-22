package net.flectone.pulse.module.message.tab;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
public class MinecraftTabModule extends TabModule {

    @Inject
    public MinecraftTabModule(FileFacade fileFacade) {
        super(fileFacade);
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                FooterModule.class,
                HeaderModule.class,
                PlayerlistnameModule.class
        );
    }

}
