package net.flectone.pulse.module.message;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.serverlink.MinecraftServerlinkModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
public class MinecraftMessageModule extends MessageModule {

    @Inject
    public MinecraftMessageModule(FileFacade fileFacade) {
        super(fileFacade);
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return super.childrenBuilder().add(MinecraftServerlinkModule.class);
    }

}
