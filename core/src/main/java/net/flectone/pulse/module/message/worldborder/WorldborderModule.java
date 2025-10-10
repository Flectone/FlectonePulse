package net.flectone.pulse.module.message.worldborder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.worldborder.listener.WorldborderPulseListener;
import net.flectone.pulse.module.message.worldborder.model.Worldborder;
import net.flectone.pulse.module.message.worldborder.model.WorldborderMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WorldborderModule extends AbstractModuleLocalization<Localization.Message.Worldborder> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(WorldborderPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.WORLDBORDER;
    }

    @Override
    public Message.Worldborder config() {
        return fileResolver.getMessage().getWorldborder();
    }

    @Override
    public Permission.Message.Worldborder permission() {
        return fileResolver.getPermission().getMessage().getWorldborder();
    }

    @Override
    public Localization.Message.Worldborder localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getWorldborder();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Worldborder worldborder) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(WorldborderMetadata.<Localization.Message.Worldborder>builder()
                .sender(fPlayer)
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_WORLDBORDER_CENTER_SUCCESS -> localization.getCenter();
                            case COMMANDS_WORLDBORDER_DAMAGE_AMOUNT_SUCCESS -> localization.getDamage().getAmount();
                            case COMMANDS_WORLDBORDER_DAMAGE_BUFFER_SUCCESS -> localization.getDamage().getBuffer();
                            case COMMANDS_WORLDBORDER_GET, COMMANDS_WORLDBORDER_GET_SUCCESS -> localization.getGet();
                            case COMMANDS_WORLDBORDER_SET_GROW, COMMANDS_WORLDBORDER_SETSLOWLY_GROW_SUCCESS -> localization.getSet().getGrow();
                            case COMMANDS_WORLDBORDER_SET_IMMEDIATE, COMMANDS_WORLDBORDER_SET_SUCCESS -> localization.getSet().getImmediate();
                            case COMMANDS_WORLDBORDER_SET_SHRINK, COMMANDS_WORLDBORDER_SETSLOWLY_SHRINK_SUCCESS -> localization.getSet().getShrink();
                            case COMMANDS_WORLDBORDER_WARNING_DISTANCE_SUCCESS -> localization.getWarning().getDistance();
                            case COMMANDS_WORLDBORDER_WARNING_TIME_SUCCESS -> localization.getWarning().getTime();
                            default -> "";
                        },
                        new String[]{"<value>", "<second_value>"},
                        new String[]{worldborder.value(), StringUtils.defaultString(worldborder.secondValue())}
                ))
                .worldborder(worldborder)
                .translationKey(translationKey)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}