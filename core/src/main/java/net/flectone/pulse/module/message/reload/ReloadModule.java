package net.flectone.pulse.module.message.reload;

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
import net.flectone.pulse.module.message.reload.listener.ReloadPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReloadModule extends AbstractModuleLocalization<Localization.Message.Reload> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(ReloadPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.RELOAD;
    }

    @Override
    public Message.Reload config() {
        return fileResolver.getMessage().getReload();
    }

    @Override
    public Permission.Message.Reload permission() {
        return fileResolver.getPermission().getMessage().getReload();
    }

    @Override
    public Localization.Message.Reload localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getReload();
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(metadataBuilder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(Localization.Message.Reload::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}