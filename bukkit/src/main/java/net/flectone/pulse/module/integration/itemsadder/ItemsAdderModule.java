package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class ItemsAdderModule extends AbstractModule {

    private final Integration.Itemsadder integration;
    private final Permission.Integration.Itemsadder permission;
    private final ItemsAdderIntegration itemsAdderIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ItemsAdderModule(FileResolver fileResolver,
                            ItemsAdderIntegration itemsAdderIntegration,
                            ListenerRegistry listenerRegistry) {
        this.integration = fileResolver.getIntegration().getItemsadder();
        this.permission = fileResolver.getPermission().getIntegration().getItemsadder();
        this.itemsAdderIntegration = itemsAdderIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        itemsAdderIntegration.hook();

        listenerRegistry.register(ItemsAdderIntegration.class);
    }

    @Override
    public void onDisable() {
        itemsAdderIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return itemsAdderIntegration.isHooked();
    }
}
