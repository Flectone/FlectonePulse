package net.flectone.pulse.module.integration.interactivechat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.interactivechat.listener.InteractiveChatPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.Component;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InteractiveChatModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final InteractiveChatIntegration interactiveChatIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        interactiveChatIntegration.hook();

        listenerRegistry.register(InteractiveChatIntegration.class);
        listenerRegistry.register(InteractiveChatPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        interactiveChatIntegration.unhook();
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
        if (isModuleDisabledFor(fSender)) return message;

        return interactiveChatIntegration.checkMention(fSender, message);
    }

    public boolean sendMessage(FEntity fReceiver, Component message) {
        if (isModuleDisabledFor(fReceiver)) return false;

        return interactiveChatIntegration.sendMessage(fReceiver, message);
    }

}
