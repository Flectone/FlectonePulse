package net.flectone.pulse.module.message.setblock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.setblock.listener.SetblockPulseListener;
import net.flectone.pulse.module.message.setblock.model.Setblock;
import net.flectone.pulse.module.message.setblock.model.SetblockMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class SetblockModule extends AbstractModuleLocalization<Localization.Message.Setblock> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SetblockModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(MessageType.SETBLOCK);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SetblockPulseListener.class);
    }

    @Override
    public Message.Setblock config() {
        return fileResolver.getMessage().getSetblock();
    }

    @Override
    public Permission.Message.Setblock permission() {
        return fileResolver.getPermission().getMessage().getSetblock();
    }

    @Override
    public Localization.Message.Setblock localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSetblock();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Setblock setblock) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SetblockMetadata.<Localization.Message.Setblock>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        localization.getFormat(),
                        new String[]{"<x>", "<y>", "<z>"},
                        new String[]{StringUtils.defaultString(setblock.x()), StringUtils.defaultString(setblock.y()), StringUtils.defaultString(setblock.z())}
                ))
                .setblock(setblock)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
