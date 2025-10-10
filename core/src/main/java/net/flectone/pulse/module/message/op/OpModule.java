package net.flectone.pulse.module.message.op;

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
import net.flectone.pulse.module.message.op.listener.OpPulseListener;
import net.flectone.pulse.module.message.op.model.OpMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OpModule extends AbstractModuleLocalization<Localization.Message.Op> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(OpPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.OP;
    }

    @Override
    public Message.Op config() {
        return fileResolver.getMessage().getOp();
    }

    @Override
    public Permission.Message.Op permission() {
        return fileResolver.getPermission().getMessage().getOp();
    }

    @Override
    public Localization.Message.Op localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getOp();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(OpMetadata.<Localization.Message.Op>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(Localization.Message.Op::getFormat)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .target(target)
                .translationKey(translationKey)
                .build()
        );
    }
}
