package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.interactivechat.listener.BukkitPulseInteractiveChatListener;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitInteractiveChatModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final BukkitInteractiveChatIntegration interactiveChatIntegration;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        interactiveChatIntegration.hook();

        listenerRegistry.register(BukkitInteractiveChatIntegration.class);
        listenerRegistry.register(BukkitPulseInteractiveChatListener.class);
    }

    @Override
    public void onDisable() {
        interactiveChatIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_INTERACTIVECHAT;
    }

    @Override
    public Integration.Interactivechat config() {
        return fileFacade.integration().interactivechat();
    }

    @Override
    public Permission.Integration.Interactivechat permission() {
        return fileFacade.permission().integration().interactivechat();
    }

    public String checkMention(FEntity fSender, String message) {
        if (moduleController.isDisabledFor(this, fSender)) return message;

        return interactiveChatIntegration.checkMention(fSender, message);
    }

    public boolean sendMessage(FEntity fReceiver, Component message) {
        if (moduleController.isDisabledFor(this, fReceiver)) return false;

        return interactiveChatIntegration.sendMessage(fReceiver, message);
    }

}
