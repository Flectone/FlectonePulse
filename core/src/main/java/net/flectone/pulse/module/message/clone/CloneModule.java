package net.flectone.pulse.module.message.clone;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.clone.listener.ClonePulseListener;
import net.flectone.pulse.module.message.clone.model.CloneMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.Strings;

@Singleton
public class CloneModule extends AbstractModuleLocalization<Localization.Message.Clone> {

    private final Message.Clone message;
    private final Permission.Message.Clone permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public CloneModule(FileResolver fileResolver,
                       ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getClone(), MessageType.CLONE);

        this.message = fileResolver.getMessage().getClone();
        this.permission = fileResolver.getPermission().getMessage().getClone();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ClonePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String blocks) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(CloneMetadata.<Localization.Message.Clone>builder()
                .sender(fPlayer)
                .format(localization -> Strings.CS.replace(localization.getFormat(), "<blocks>", blocks))
                .blocks(blocks)
                .translationKey(translationKey)
                .range(message.getRange())
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
