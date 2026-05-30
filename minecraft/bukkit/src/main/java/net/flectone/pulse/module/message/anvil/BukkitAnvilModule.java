package net.flectone.pulse.module.message.anvil;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.module.message.anvil.listener.BukkitAnvilListener;
import net.flectone.pulse.module.message.anvil.listener.PaperAnvilListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.BukkitListenerRegistry;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

@Singleton
public class BukkitAnvilModule extends AnvilModule {

    private final FPlayerService fPlayerService;
    private final ReflectionResolver reflectionResolver;
    private final BukkitListenerRegistry listenerRegistry;

    @Inject
    public BukkitAnvilModule(FileFacade fileFacade,
                             BukkitListenerRegistry listenerRegistry,
                             FPlayerService fPlayerService,
                             ReflectionResolver reflectionResolver,
                             MessagePipeline messagePipeline,
                             ModuleController moduleController) {
        super(fileFacade, messagePipeline, moduleController);

        this.fPlayerService = fPlayerService;
        this.reflectionResolver = reflectionResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (reflectionResolver.hasMethod(ItemStack.class, "displayName")) {
            PaperAnvilListener paperAnvilListener = new PaperAnvilListener(fPlayerService, this);
            listenerRegistry.register(paperAnvilListener, EventPriority.NORMAL);
            return;
        }

        listenerRegistry.register(BukkitAnvilListener.class);
    }
}
