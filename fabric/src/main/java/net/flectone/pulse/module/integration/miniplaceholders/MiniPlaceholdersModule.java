package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;

@Singleton
public class MiniPlaceholdersModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final MiniPlaceholdersIntegration miniPlaceholdersIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public MiniPlaceholdersModule(FileResolver fileResolver,
                                  ListenerRegistry listenerRegistry,
                                  MiniPlaceholdersIntegration miniPlaceholdersIntegration) {
        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
        this.miniPlaceholdersIntegration = miniPlaceholdersIntegration;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerPermission(permission().getUse());

        miniPlaceholdersIntegration.hookLater();

        listenerRegistry.register(MessageFormattingEvent.class, Event.Priority.HIGH, event -> {
            MessageFormattingEvent messageFormattingEvent = (MessageFormattingEvent) event;

            MessageContext messageContext = messageFormattingEvent.getContext();
            FEntity sender = messageContext.getSender();
            if (isModuleDisabledFor(sender)) return;

            miniPlaceholdersIntegration.onMessageFormattingEvent(messageFormattingEvent);
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();

        miniPlaceholdersIntegration.unhook();
    }

    @Override
    public Integration.MiniPlaceholders config() {
        return fileResolver.getIntegration().getMiniplaceholders();
    }

    @Override
    public Permission.Integration.MiniPlaceholders permission() {
        return fileResolver.getPermission().getIntegration().getMiniplaceholders();
    }
}
