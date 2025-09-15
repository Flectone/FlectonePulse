package net.flectone.pulse.module.message.setblock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Setblock message;
    private final Permission.Message.Setblock permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SetblockModule(FileResolver fileResolver,
                          ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSetblock(), MessageType.SETBLOCK);

        this.message = fileResolver.getMessage().getSetblock();
        this.permission = fileResolver.getPermission().getMessage().getSetblock();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SetblockPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Setblock setblock) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SetblockMetadata.<Localization.Message.Setblock>builder()
                .sender(fPlayer)
                .range(message.getRange())
                .format(localization -> StringUtils.replaceEach(
                        localization.getFormat(),
                        new String[]{"<x>", "<y>", "<z>"},
                        new String[]{StringUtils.defaultString(setblock.x()), StringUtils.defaultString(setblock.y()), StringUtils.defaultString(setblock.z())}
                ))
                .setblock(setblock)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
