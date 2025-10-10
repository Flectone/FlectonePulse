package net.flectone.pulse.module.message.difficulty;

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
import net.flectone.pulse.module.message.difficulty.listener.DifficultyPulseListener;
import net.flectone.pulse.module.message.difficulty.model.DifficultyMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.Strings;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DifficultyModule extends AbstractModuleLocalization<Localization.Message.Difficulty> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(DifficultyPulseListener.class);
    }

    @Override
    public MessageType messageType() {
        return MessageType.DIFFICULTY;
    }

    @Override
    public Message.Difficulty config() {
        return fileResolver.getMessage().getDifficulty();
    }

    @Override
    public Permission.Message.Difficulty permission() {
        return fileResolver.getPermission().getMessage().getDifficulty();
    }

    @Override
    public Localization.Message.Difficulty localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getDifficulty();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, String difficulty) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(DifficultyMetadata.<Localization.Message.Difficulty>builder()
                .sender(fPlayer)
                .format(localization -> Strings.CS.replace(
                        translationKey == MinecraftTranslationKey.COMMANDS_DIFFICULTY_QUERY ? localization.getQuery() : localization.getSuccess(),
                        "<difficulty>",
                        difficulty
                ))
                .difficulty(difficulty)
                .translationKey(translationKey)
                .range(config().getRange())
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );
    }
}
