package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.format.moderation.delete.DeleteModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;

@Singleton
public class InteractiveChatModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final InteractiveChatIntegration interactiveChatIntegration;
    private final ListenerRegistry listenerRegistry;
    private final DeleteModule deleteModule;
    private final FLogger fLogger;

    @Inject
    public InteractiveChatModule(FileResolver fileResolver,
                                 InteractiveChatIntegration interactiveChatIntegration,
                                 ListenerRegistry listenerRegistry,
                                 DeleteModule deleteModule,
                                 FLogger fLogger) {
        this.fileResolver = fileResolver;
        this.interactiveChatIntegration = interactiveChatIntegration;
        this.listenerRegistry = listenerRegistry;
        this.deleteModule = deleteModule;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        if (fileResolver.getMessage().getFormat().getModeration().getDelete().isEnable()) {
            fLogger.warning("InteractiveChat and Delete module incompatible");
            deleteModule.addPredicate(fEntity -> false);
        }

        registerModulePermission(permission());

        interactiveChatIntegration.hook();

        listenerRegistry.register(InteractiveChatIntegration.class);
    }

    @Override
    public void onDisable() {
        interactiveChatIntegration.unhook();
    }

    @Override
    public Integration.Interactivechat config() {
        return fileResolver.getIntegration().getInteractivechat();
    }

    @Override
    public Permission.Integration.Interactivechat permission() {
        return fileResolver.getPermission().getIntegration().getInteractivechat();
    }

    public String checkMention(FEntity fSender, String message) {
        if (isModuleDisabledFor(fSender)) return message;

        return interactiveChatIntegration.checkMention(fSender, message);
    }

    public boolean sendMessage(FEntity fReceiver, Component message) {
        if (isModuleDisabledFor(fReceiver)) return false;

        return interactiveChatIntegration.sendMessage(fReceiver, message);
    }

}
