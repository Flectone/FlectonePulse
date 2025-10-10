package net.flectone.pulse.module.message.sound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.sound.listener.SoundPulseListener;
import net.flectone.pulse.module.message.sound.model.Sound;
import net.flectone.pulse.module.message.sound.model.SoundMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SoundModule extends AbstractModuleLocalization<Localization.Message.Sound> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(SoundPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.SOUND;
    }

    @Override
    public Message.CommandSound config() {
        return fileResolver.getMessage().getSound();
    }

    @Override
    public Permission.Message.Sound permission() {
        return fileResolver.getPermission().getMessage().getSound();
    }

    @Override
    public Localization.Message.Sound localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getSound();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Sound sound) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(SoundMetadata.<Localization.Message.Sound>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_PLAYSOUND_SUCCESS_MULTIPLE -> localization.getPlay().getMultiple();
                            case COMMANDS_PLAYSOUND_SUCCESS_SINGLE, COMMANDS_PLAYSOUND_SUCCESS -> localization.getPlay().getSingle();
                            case COMMANDS_STOPSOUND_SUCCESS_SOURCE_ANY, COMMANDS_STOPSOUND_SUCCESS_ALL -> localization.getStop().getSourceAny();
                            case COMMANDS_STOPSOUND_SUCCESS_SOURCE_SOUND, COMMANDS_STOPSOUND_SUCCESS_INDIVIDUALSOUND -> localization.getStop().getSourceSound();
                            case COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_ANY, COMMANDS_STOPSOUND_SUCCESS_SOUNDSOURCE -> localization.getStop().getSourcelessAny();
                            case COMMANDS_STOPSOUND_SUCCESS_SOURCELESS_SOUND -> localization.getStop().getSourcelessSound();
                            default -> "";
                        },
                        new String[]{"<sound>", "<source>", "<players>"},
                        new String[]{StringUtils.defaultString(sound.getName()), StringUtils.defaultString(sound.getSource()), StringUtils.defaultString(sound.getPlayers())}
                ))
                .destination(config().getDestination())
                .metaSound(sound)
                .translationKey(translationKey)
                .sound(getModuleSound())
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, sound.getTarget())})
                .build()
        );
    }
}
