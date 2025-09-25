package net.flectone.pulse.module.message.fill;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.fill.listener.FillPulseListener;
import net.flectone.pulse.module.message.fill.model.FillMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.Strings;

@Singleton
public class FillModule extends AbstractModuleLocalization<Localization.Message.Fill> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public FillModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(MessageType.FILL);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(FillPulseListener.class);
    }

    @Override
    public Message.Fill config() {
        return fileResolver.getMessage().getFill();
    }

    @Override
    public Permission.Message.Fill permission() {
        return fileResolver.getPermission().getMessage().getFill();
    }

    @Override
    public Localization.Message.Fill localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getFill();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String blocks) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(FillMetadata.<Localization.Message.Fill>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> Strings.CS.replace(localization.getFormat(), "<blocks>", blocks))
                .blocks(blocks)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
