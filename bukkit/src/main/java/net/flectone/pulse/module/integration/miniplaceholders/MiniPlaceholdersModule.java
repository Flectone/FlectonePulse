package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;

@Singleton
public class MiniPlaceholdersModule extends AbstractModule {

    private final Integration.MiniPlaceholders integration;
    private final Permission.Integration.MiniPlaceholders permission;
    private final MiniPlaceholdersIntegration miniPlaceholdersIntegration;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MiniPlaceholdersModule(FileResolver fileResolver,
                                  PermissionChecker permissionChecker,
                                  ListenerRegistry listenerRegistry,
                                  FLogger fLogger) {
        this.integration = fileResolver.getIntegration().getMiniplaceholders();
        this.permission = fileResolver.getPermission().getIntegration().getMiniplaceholders();
        this.permissionChecker = permissionChecker;
        this.listenerRegistry = listenerRegistry;

        // don't use injection because we skip relocate
        this.miniPlaceholdersIntegration = new MiniPlaceholdersIntegration(fLogger);
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        registerPermission(permission.getUse());

        miniPlaceholdersIntegration.hook();

        listenerRegistry.register(MessageFormattingEvent.class, Event.Priority.HIGH, event -> {
            MessageFormattingEvent messageFormattingEvent = (MessageFormattingEvent) event;

            MessageContext messageContext = messageFormattingEvent.getContext();
            FEntity sender = messageContext.getSender();
            if (checkModulePredicates(sender)) return;

            FEntity receiver = messageContext.getReceiver();
            boolean isUserMessage = messageContext.isFlag(MessageFlag.USER_MESSAGE);
            if (!permissionChecker.check(sender, permission.getUse()) && isUserMessage) return;
            if (!permissionChecker.check(receiver, permission.getUse()) && isUserMessage) return;

            miniPlaceholdersIntegration.onMessageProcessingEvent(messageFormattingEvent);
        });
    }

    @Override
    public void onDisable() {
        miniPlaceholdersIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
