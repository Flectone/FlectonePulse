package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;

@Singleton
public class InteractiveChatModule extends AbstractModule {

    private final Integration.Interactivechat integration;
    private final Permission.Integration.Interactivechat permission;
    private final InteractiveChatIntegration interactiveChatIntegration;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public InteractiveChatModule(FileResolver fileResolver,
                                 InteractiveChatIntegration interactiveChatIntegration,
                                 MessageProcessRegistry messageProcessRegistry) {
        this.integration = fileResolver.getIntegration().getInteractivechat();
        this.permission = fileResolver.getPermission().getIntegration().getInteractivechat();
        this.interactiveChatIntegration = interactiveChatIntegration;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        interactiveChatIntegration.hook();
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
