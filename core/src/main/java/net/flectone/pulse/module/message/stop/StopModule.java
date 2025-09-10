package net.flectone.pulse.module.message.stop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.stop.listener.StopPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
public class StopModule extends AbstractModuleLocalization<Localization.Message.Stop> {

    private final Message.Stop message;
    private final Permission.Message.Stop permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public StopModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getStop(), MessageType.STOP);

        this.message = fileResolver.getMessage().getStop();
        this.permission = fileResolver.getPermission().getMessage().getStop();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(StopPulseListener.class);
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
                .format(Localization.Message.Stop::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}