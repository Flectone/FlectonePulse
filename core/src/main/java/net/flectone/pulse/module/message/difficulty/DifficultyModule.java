package net.flectone.pulse.module.message.difficulty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
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
public class DifficultyModule extends AbstractModuleLocalization<Localization.Message.Difficulty> {

    private final Message.Difficulty message;
    private final Permission.Message.Difficulty permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public DifficultyModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getDifficulty(), MessageType.DIFFICULTY);

        this.message = fileResolver.getMessage().getDifficulty();
        this.permission = fileResolver.getPermission().getMessage().getDifficulty();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(DifficultyPulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
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
                .range(message.getRange())
                .destination(message.getDestination())
                .sound(getModuleSound())
                .build()
        );
    }

}
