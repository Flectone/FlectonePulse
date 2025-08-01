package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.Component;

@Singleton
public class InteractiveChatModule extends AbstractModule {

    private final Integration.Interactivechat integration;
    private final Permission.Integration.Interactivechat permission;
    private final InteractiveChatIntegration interactiveChatIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public InteractiveChatModule(FileResolver fileResolver,
                                 InteractiveChatIntegration interactiveChatIntegration,
                                 ListenerRegistry listenerRegistry) {
        this.integration = fileResolver.getIntegration().getInteractivechat();
        this.permission = fileResolver.getPermission().getIntegration().getInteractivechat();
        this.interactiveChatIntegration = interactiveChatIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        interactiveChatIntegration.hook();

        listenerRegistry.register(InteractiveChatIntegration.class);
    }

    @Override
    public void onDisable() {
        interactiveChatIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String checkMention(FEntity fSender, String message) {
        if (checkModulePredicates(fSender)) return message;

        return interactiveChatIntegration.checkMention(fSender, message);
    }

    public boolean sendMessage(FEntity fReceiver, Component message) {
        if (checkModulePredicates(fReceiver)) return false;

        return interactiveChatIntegration.sendMessage(fReceiver, message);
    }

}
