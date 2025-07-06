package net.flectone.pulse.module.integration.itemsadder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.MessageProcessRegistry;

@Singleton
public class ItemsAdderModule extends AbstractModule {

    private final Integration.Itemsadder integration;
    private final Permission.Integration.Itemsadder permission;

    private final ItemsAdderIntegration itemsAdderIntegration;

    @Inject
    public ItemsAdderModule(FileResolver fileResolver,
                            ItemsAdderIntegration itemsAdderIntegration,
                            MessageProcessRegistry messageProcessRegistry) {
        integration = fileResolver.getIntegration().getItemsadder();
        permission = fileResolver.getPermission().getIntegration().getItemsadder();

        this.itemsAdderIntegration = itemsAdderIntegration;

        messageProcessRegistry.register(30, itemsAdderIntegration);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        itemsAdderIntegration.hook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return itemsAdderIntegration.isHooked();
    }
}
