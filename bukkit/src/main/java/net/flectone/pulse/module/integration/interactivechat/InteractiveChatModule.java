package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Singleton
public class InteractiveChatModule extends AbstractModule {

    private final Integration.Interactivechat integration;
    private final Permission.Integration.Interactivechat permission;

    private final InteractiveChatIntegration interactiveChatIntegration;

    @Inject
    public InteractiveChatModule(FileManager fileManager,
                                 InteractiveChatIntegration interactiveChatIntegration) {
        this.interactiveChatIntegration = interactiveChatIntegration;

        integration = fileManager.getIntegration().getInteractivechat();
        permission = fileManager.getPermission().getIntegration().getInteractivechat();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        interactiveChatIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }

    public String checkMention(AsyncPlayerChatEvent event) {
        if (!isEnable()) return event.getMessage();

        return interactiveChatIntegration.checkMention(event);
    }

    public String mark(FEntity sender, String message) {
        if (checkModulePredicates(sender)) return message;

        return interactiveChatIntegration.mark(sender, message);
    }
}
