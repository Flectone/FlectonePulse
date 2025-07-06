package net.flectone.pulse.module.integration.placeholderapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.MessageProcessRegistry;

@Singleton
public class PlaceholderAPIModule extends AbstractModule {

    private final Integration.Placeholderapi integration;
    private final Permission.Integration.Placeholderapi permission;

    private final PlaceholderAPIIntegration placeholderAPIIntegration;

    @Inject
    public PlaceholderAPIModule(FileResolver fileResolver,
                                PermissionChecker permissionChecker,
                                PlaceholderAPIIntegration placeholderAPIIntegration,
                                MessageProcessRegistry messageProcessRegistry) {
        this.placeholderAPIIntegration = placeholderAPIIntegration;

        integration = fileResolver.getIntegration().getPlaceholderapi();
        permission = fileResolver.getPermission().getIntegration().getPlaceholderapi();

        messageProcessRegistry.register(10, messageContext -> {
            FEntity sender = messageContext.getSender();
            if (checkModulePredicates(sender)) return;

            FEntity receiver = messageContext.getReceiver();
            boolean isUserMessage = messageContext.isUserMessage();
            if (!permissionChecker.check(sender, permission.getUse()) && isUserMessage) return;
            if (!permissionChecker.check(receiver, permission.getUse()) && isUserMessage) return;

            placeholderAPIIntegration.process(messageContext);
        });
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        placeholderAPIIntegration.hook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
