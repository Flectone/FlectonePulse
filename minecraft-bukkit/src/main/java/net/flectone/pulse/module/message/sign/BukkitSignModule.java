package net.flectone.pulse.module.message.sign;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.message.sign.listener.BukkitSignListener;
import net.flectone.pulse.module.message.sign.listener.PaperSignListener;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.resolver.ReflectionResolver;
import org.bukkit.event.block.SignChangeEvent;

@Singleton
public class BukkitSignModule extends SignModuleImpl {

    private final ReflectionResolver reflectionResolver;
    private final BukkitListenerRegistry listenerRegistry;

    @Inject
    public BukkitSignModule(FileFacade fileFacade,
                            MessagePipeline messagePipeline,
                            ReflectionResolver reflectionResolver,
                            BukkitListenerRegistry listenerRegistry,
                            ModuleController moduleController) {
        super(fileFacade, messagePipeline, moduleController);

        this.reflectionResolver = reflectionResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (reflectionResolver.hasMethod(SignChangeEvent.class, "lines")) {
            listenerRegistry.register(PaperSignListener.class);
        } else {
            listenerRegistry.register(BukkitSignListener.class);
        }
    }
}
