package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ItemsAdderModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final ItemsAdderIntegration itemsAdderIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        itemsAdderIntegration.hook();

        listenerRegistry.register(ItemsAdderIntegration.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        itemsAdderIntegration.unhook();
    }

    @Override
    public Integration.Itemsadder config() {
        return fileFacade.integration().itemsadder();
    }

    @Override
    public Permission.Integration.Itemsadder permission() {
        return fileFacade.permission().integration().itemsadder();
    }

    public boolean isHooked() {
        return itemsAdderIntegration.isHooked();
    }
}
