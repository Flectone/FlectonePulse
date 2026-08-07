package net.flectone.pulse.module.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.serverlink.MinecraftServerlinkModule;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
public class MinecraftMessageModule extends MessageModuleImpl {

    @Inject
    public MinecraftMessageModule(FileFacade fileFacade) {
        super(fileFacade);
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<Class<? extends ModuleSimple>> children = new LinkedHashSet<>(super.children());
        children.add(MinecraftServerlinkModule.class);
        return Collections.unmodifiableSet(children);
    }

}
