package net.flectone.pulse.module.message.save;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.save.listener.SavePulseListener;
import net.flectone.pulse.module.message.save.model.SaveMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
public class SaveModule extends AbstractModuleLocalization<Localization.Message.Save> {

    private final Message.Save message;
    private final Permission.Message.Save permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SaveModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getSave(), MessageType.SAVE);

        this.message = fileResolver.getMessage().getSave();
        this.permission = fileResolver.getPermission().getMessage().getSave();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(SavePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SaveMetadata.<Localization.Message.Save>builder()
                .sender(fPlayer)
                .filterPlayer(fPlayer)
                .format(localization -> switch (translationKey) {
                    case COMMANDS_SAVE_DISABLED -> localization.getDisabled();
                    case COMMANDS_SAVE_ENABLED -> localization.getEnabled();
                    case COMMANDS_SAVE_SAVING -> localization.getSaving();
                    case COMMANDS_SAVE_SUCCESS -> localization.getSuccess();
                    default -> "";
                })
                .destination(message.getDestination())
                .sound(getModuleSound())
                .translationKey(translationKey)
                .build()
        );
    }

}