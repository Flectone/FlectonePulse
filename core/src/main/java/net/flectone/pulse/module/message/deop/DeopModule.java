package net.flectone.pulse.module.message.deop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.deop.listener.DeopPulseListener;
import net.flectone.pulse.module.message.deop.model.DeopMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@Singleton
public class DeopModule extends AbstractModuleLocalization<Localization.Message.Deop> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DeopModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(MessageType.DEOP);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(DeopPulseListener.class);
    }

    @Override
    public Message.Deop config() {
        return fileResolver.getMessage().getDeop();
    }

    @Override
    public Permission.Message.Deop permission() {
        return fileResolver.getPermission().getMessage().getDeop();
    }

    @Override
    public Localization.Message.Deop localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getDeop();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DeopMetadata.<Localization.Message.Deop>builder()
                .sender(fPlayer)
                .target(target)
                .translationKey(translationKey)
                .format(Localization.Message.Deop::getFormat)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );
    }
}
