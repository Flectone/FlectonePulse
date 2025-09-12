package net.flectone.pulse.module.message.worldborder;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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
public class WorldborderModule extends AbstractModuleLocalization<Localization.Message.Worldborder> {

    private final Message.Worldborder message;
    private final Permission.Message.Worldborder permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public WorldborderModule(FileResolver fileResolver,
                             ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getWorldborder(), MessageType.WORLDBORDER);

        this.message = fileResolver.getMessage().getWorldborder();
        this.permission = fileResolver.getPermission().getMessage().getWorldborder();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(WorldborderPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Worldborder worldborder) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(WorldborderMetadata.<Localization.Message.Worldborder>builder()
                .sender(fPlayer)
                .format(s -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_WORLDBORDER_CENTER_SUCCESS -> s.getCenter();
                            case COMMANDS_WORLDBORDER_DAMAGE_AMOUNT_SUCCESS -> s.getDamage().getAmount();
                            case COMMANDS_WORLDBORDER_DAMAGE_BUFFER_SUCCESS -> s.getDamage().getBuffer();
                            case COMMANDS_WORLDBORDER_GET -> s.getGet();
                            case COMMANDS_WORLDBORDER_SET_GROW -> s.getSet().getGrow();
                            case COMMANDS_WORLDBORDER_SET_IMMEDIATE -> s.getSet().getImmediate();
                            case COMMANDS_WORLDBORDER_SET_SHRINK -> s.getSet().getShrink();
                            case COMMANDS_WORLDBORDER_WARNING_DISTANCE_SUCCESS -> s.getWarning().getDistance();
                            case COMMANDS_WORLDBORDER_WARNING_TIME_SUCCESS -> s.getWarning().getTime();
                            default -> "";
                        },
                        new String[]{"<value>", "<second_value>"},
                        new String[]{worldborder.value(), StringUtils.defaultString(worldborder.secondValue())}
                ))
                .worldborder(worldborder)
                .translationKey(translationKey)
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}