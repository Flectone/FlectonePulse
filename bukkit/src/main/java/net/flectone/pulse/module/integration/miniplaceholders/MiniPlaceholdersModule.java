package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MiniPlaceholdersModule extends AbstractModule {

    private final Integration.MiniPlaceholders integration;
    private final Permission.Integration.MiniPlaceholders permission;

    private final MiniPlaceholdersIntegration miniPlaceholdersIntegration;

    @Inject
    public MiniPlaceholdersModule(FileManager fileManager,
                                  PermissionChecker permissionChecker,
                                  MessageProcessRegistry messageProcessRegistry,
                                  FLogger fLogger) {
        // don't use injection because we skip relocate
        this.miniPlaceholdersIntegration = new MiniPlaceholdersIntegration(fLogger);

        integration = fileManager.getIntegration().getMiniplaceholders();
        permission = fileManager.getPermission().getIntegration().getMiniplaceholders();

        messageProcessRegistry.register(180, messageContext -> {
            FEntity sender = messageContext.getSender();
            if (checkModulePredicates(sender)) return;

            FEntity receiver = messageContext.getReceiver();
            boolean isUserMessage = messageContext.isUserMessage();
            if (!permissionChecker.check(sender, permission.getUse()) && isUserMessage) return;
            if (!permissionChecker.check(receiver, permission.getUse()) && isUserMessage) return;

            miniPlaceholdersIntegration.process(messageContext);
        });
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        miniPlaceholdersIntegration.hook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
