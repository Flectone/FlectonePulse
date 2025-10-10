package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ItemsAdderModule extends AbstractModule {

    private final FileResolver fileResolver;
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
        return fileResolver.getIntegration().getItemsadder();
    }

    @Override
    public Permission.Integration.Itemsadder permission() {
        return fileResolver.getPermission().getIntegration().getItemsadder();
    }

    public boolean isHooked() {
        return itemsAdderIntegration.isHooked();
    }
}
