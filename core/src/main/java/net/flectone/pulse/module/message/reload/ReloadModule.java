package net.flectone.pulse.module.message.reload;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.reload.listener.ReloadPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class ReloadModule extends AbstractModuleLocalization<Localization.Message.Reload> {

    private final Message.Reload message;
    private final Permission.Message.Reload permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ReloadModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getReload(), MessageType.RELOAD);

        this.message = fileResolver.getMessage().getReload();
        this.permission = fileResolver.getPermission().getMessage().getReload();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ReloadPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(Localization.Message.Reload::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}