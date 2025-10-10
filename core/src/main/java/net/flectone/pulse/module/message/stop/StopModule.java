package net.flectone.pulse.module.message.stop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.stop.listener.StopPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StopModule extends AbstractModuleLocalization<Localization.Message.Stop> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(StopPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.STOP;
    }

    @Override
    public Message.Stop config() {
        return fileResolver.getMessage().getStop();
    }

    @Override
    public Permission.Message.Stop permission() {
        return fileResolver.getPermission().getMessage().getStop();
    }

    @Override
    public Localization.Message.Stop localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getStop();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(Localization.Message.Stop::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}