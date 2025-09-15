package net.flectone.pulse.module.message.rotate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
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
public class RotateModule extends AbstractModuleLocalization<Localization.Message.Rotate> {

    private final Message.Rotate message;
    private final Permission.Message.Rotate permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public RotateModule(FileResolver fileResolver,
                        ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getRotate(), MessageType.ROTATE);

        this.message = fileResolver.getMessage().getRotate();
        this.permission = fileResolver.getPermission().getMessage().getRotate();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(RotatePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(RotateMetadata.<Localization.Message.Rotate>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .target(target)
                .translationKey(translationKey)
                .format(Localization.Message.Rotate::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .build()
        );
    }

}