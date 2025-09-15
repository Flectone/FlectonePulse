package net.flectone.pulse.module.message.op;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
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
public class OpModule extends AbstractModuleLocalization<Localization.Message.Op> {

    private final Message.Op message;
    private final Permission.Message.Op permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public OpModule(FileResolver fileResolver,
                    ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getOp(), MessageType.OP);

        this.message = fileResolver.getMessage().getOp();
        this.permission = fileResolver.getPermission().getMessage().getOp();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(OpPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, FEntity target) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(OpMetadata.<Localization.Message.Op>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(Localization.Message.Op::getFormat)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, target)})
                .target(target)
                .translationKey(translationKey)
                .build()
        );
    }

}
