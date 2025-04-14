package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.kyori.adventure.text.Component;

@Singleton
public class InteractiveChatModule extends AbstractModule {

    private final Integration.Interactivechat integration;
    private final Permission.Integration.Interactivechat permission;
    private final InteractiveChatIntegration interactiveChatIntegration;

    @Inject
    public InteractiveChatModule(FileManager fileManager,
                                 InteractiveChatIntegration interactiveChatIntegration,
                                 MessageProcessRegistry messageProcessRegistry) {
        this.interactiveChatIntegration = interactiveChatIntegration;

        integration = fileManager.getIntegration().getInteractivechat();
        permission = fileManager.getPermission().getIntegration().getInteractivechat();

        messageProcessRegistry.register(0, interactiveChatIntegration);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);
        interactiveChatIntegration.hook();
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
