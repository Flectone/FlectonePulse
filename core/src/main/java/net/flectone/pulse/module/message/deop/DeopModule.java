package net.flectone.pulse.module.message.deop;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
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

    private final Message.Deop message;
    private final Permission.Message.Deop permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DeopModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getDeop(), MessageType.DEOP);

        this.message = fileResolver.getMessage().getDeop();
        this.permission = fileResolver.getPermission().getMessage().getDeop();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DeopPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DeopMetadata.<Localization.Message.Deop>builder()
                .sender(fPlayer)
                .target(target)
                .translationKey(translationKey)
                .format(Localization.Message.Deop::getFormat)
                .range(message.getRange())
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );
    }

}
