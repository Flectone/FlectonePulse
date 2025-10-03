package net.flectone.pulse.module.message.save;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
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

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SaveModule(FileResolver fileResolver,
                      ListenerRegistry listenerRegistry) {
        super(MessageType.SAVE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SavePulseListener.class);
    }

    @Override
    public Message.Save config() {
        return fileResolver.getMessage().getSave();
    }

    @Override
    public Permission.Message.Save permission() {
        return fileResolver.getPermission().getMessage().getSave();
    }

    @Override
    public Localization.Message.Save localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSave();
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
                    case COMMANDS_SAVE_SAVING, COMMANDS_SAVE_START -> localization.getSaving();
                    case COMMANDS_SAVE_SUCCESS -> localization.getSuccess();
                    default -> "";
                })
                .destination(config().getDestination())
                .sound(getModuleSound())
                .translationKey(translationKey)
                .build()
        );
    }
}