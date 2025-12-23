package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.util.file.FileFacade;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MiniPlaceholdersModule extends AbstractModule {

    private final FileFacade fileFacade;
    private final MiniPlaceholdersIntegration miniPlaceholdersIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        miniPlaceholdersIntegration.hookLater();

        listenerRegistry.register(MessageFormattingEvent.class, Event.Priority.HIGH, event -> {
            MessageFormattingEvent messageFormattingEvent = (MessageFormattingEvent) event;

            MessageContext messageContext = messageFormattingEvent.context();
            FEntity sender = messageContext.sender();
            if (isModuleDisabledFor(sender)) return event;

            return miniPlaceholdersIntegration.onMessageFormattingEvent(messageFormattingEvent);
        });
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder().add(permission().use());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        miniPlaceholdersIntegration.unhook();
    }

    @Override
    public Integration.MiniPlaceholders config() {
        return fileFacade.integration().miniplaceholders();
    }

    @Override
    public Permission.Integration.MiniPlaceholders permission() {
        return fileFacade.permission().integration().miniplaceholders();
    }
}
