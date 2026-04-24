package net.flectone.pulse.module.integration.miniplaceholders;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.message.MessageFormattingEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricMiniPlaceholdersModule implements ModuleSimple {

    private final FileFacade fileFacade;
    private final FabricMiniPlaceholdersIntegration miniPlaceholdersIntegration;
    private final ListenerRegistry listenerRegistry;
    private final ModuleController moduleController;

    @Override
    public void onEnable() {
        miniPlaceholdersIntegration.hookLater();

        listenerRegistry.register(MessageFormattingEvent.class, Event.Priority.HIGH, event -> {
            MessageFormattingEvent messageFormattingEvent = (MessageFormattingEvent) event;

            MessageContext messageContext = messageFormattingEvent.context();
            FEntity sender = messageContext.sender();
            if (moduleController.isDisabledFor(this, sender)) return event;
            if (messageContext.isFlag(MessageFlag.PLAYER_MESSAGE)) return event;

            return miniPlaceholdersIntegration.onMessageFormattingEvent(messageFormattingEvent);
        });
    }

    @Override
    public ImmutableSet.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ModuleSimple.super.permissionBuilder().add(permission().use());
    }

    @Override
    public void onDisable() {
        miniPlaceholdersIntegration.unhook();
    }

    @Override
    public ModuleName name() {
        return ModuleName.INTEGRATION_MINIPLACEHOLDERS;
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
