package net.flectone.pulse.module.message.debugstick;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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

    private final Message.Debugstick message;
    private final Permission.Message.Debugstick permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DebugstickModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getDebugstick(), MessageType.DEBUG_STICK);

        this.message = fileResolver.getMessage().getDebugstick();
        this.permission = fileResolver.getPermission().getMessage().getDebugstick();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DebugStickPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, DebugStick debugStick) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DebugStickMetadata.<Localization.Message.Debugstick>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case ITEM_MINECRAFT_DEBUG_STICK_EMPTY -> s.getEmpty();
                            case ITEM_MINECRAFT_DEBUG_STICK_SELECT -> s.getSelect();
                            case ITEM_MINECRAFT_DEBUG_STICK_UPDATE -> s.getUpdate();
                            default -> "";
                        },
                        new String[]{"<name>", "<value>"},
                        new String[]{debugStick.name(), StringUtils.defaultString(debugStick.value())}
                ))
                .debugStick(debugStick)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}