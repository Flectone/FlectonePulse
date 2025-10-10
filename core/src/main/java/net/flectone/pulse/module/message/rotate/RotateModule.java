package net.flectone.pulse.module.message.rotate;

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
import net.flectone.pulse.module.message.rotate.listener.RotatePulseListener;
import net.flectone.pulse.module.message.rotate.model.RotateMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RotateModule extends AbstractModuleLocalization<Localization.Message.Rotate> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(RotatePulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.ROTATE;
    }

    @Override
    public Message.Rotate config() {
        return fileResolver.getMessage().getRotate();
    }

    @Override
    public Permission.Message.Rotate permission() {
        return fileResolver.getPermission().getMessage().getRotate();
    }

    @Override
    public Localization.Message.Rotate localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getRotate();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(RotateMetadata.<Localization.Message.Rotate>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .target(target)
                .translationKey(translationKey)
                .format(Localization.Message.Rotate::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );
    }
}