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
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public ItemsAdderModule(FileResolver fileResolver,
                            ItemsAdderIntegration itemsAdderIntegration,
                            MessageProcessRegistry messageProcessRegistry) {
        this.integration = fileResolver.getIntegration().getItemsadder();
        this.permission = fileResolver.getPermission().getIntegration().getItemsadder();
        this.itemsAdderIntegration = itemsAdderIntegration;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        itemsAdderIntegration.hook();
    @Override
    public void onDisable() {
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public boolean isHooked() {
        return itemsAdderIntegration.isHooked();
    }
}
