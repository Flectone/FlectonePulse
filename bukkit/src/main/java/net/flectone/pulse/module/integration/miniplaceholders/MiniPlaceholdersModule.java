package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MiniPlaceholdersModule extends AbstractModule {

    private final Integration.MiniPlaceholders integration;
    private final Permission.Integration.MiniPlaceholders permission;
    private final MiniPlaceholdersIntegration miniPlaceholdersIntegration;
    private final PermissionChecker permissionChecker;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public MiniPlaceholdersModule(FileResolver fileResolver,
                                  PermissionChecker permissionChecker,
                                  MessageProcessRegistry messageProcessRegistry,
                                  FLogger fLogger) {
        this.integration = fileResolver.getIntegration().getMiniplaceholders();
        this.permission = fileResolver.getPermission().getIntegration().getMiniplaceholders();
        this.permissionChecker = permissionChecker;
        this.messageProcessRegistry = messageProcessRegistry;

        // don't use injection because we skip relocate
        this.miniPlaceholdersIntegration = new MiniPlaceholdersIntegration(fLogger);
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        miniPlaceholdersIntegration.hook();

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
    public void onDisable() {
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
