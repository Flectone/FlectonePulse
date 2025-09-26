package net.flectone.pulse.module.message.debugstick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.debugstick.listener.DebugStickPulseListener;
import net.flectone.pulse.module.message.debugstick.model.DebugStick;
import net.flectone.pulse.module.message.debugstick.model.DebugStickMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class DebugstickModule extends AbstractModuleLocalization<Localization.Message.Debugstick> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DebugstickModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry) {
        super(MessageType.DEBUG_STICK);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(DebugStickPulseListener.class);
    }

    @Override
    public Message.Debugstick config() {
        return fileResolver.getMessage().getDebugstick();
    }

    @Override
    public Permission.Message.Debugstick permission() {
        return fileResolver.getPermission().getMessage().getDebugstick();
    }

    @Override
    public Localization.Message.Debugstick localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getDebugstick();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, DebugStick debugStick) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DebugStickMetadata.<Localization.Message.Debugstick>builder()
                .sender(fPlayer)
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case ITEM_MINECRAFT_DEBUG_STICK_EMPTY -> localization.getEmpty();
                            case ITEM_MINECRAFT_DEBUG_STICK_SELECT -> localization.getSelect();
                            case ITEM_MINECRAFT_DEBUG_STICK_UPDATE -> localization.getUpdate();
                            default -> "";
                        },
                        new String[]{"<property>", "<value>"},
                        new String[]{debugStick.getProperty(), StringUtils.defaultString(debugStick.getValue())}
                ))
                .range(config().getRange())
                .debugStick(debugStick)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}