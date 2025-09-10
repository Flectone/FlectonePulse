package net.flectone.pulse.module.message.experience;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.experience.listener.ExperiencePulseListener;
import net.flectone.pulse.module.message.experience.model.Experience;
import net.flectone.pulse.module.message.experience.model.ExperienceMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class ExperienceModule extends AbstractModuleLocalization<Localization.Message.Experience> {

    private final Message.Experience message;
    private final Permission.Message.Experience permission;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ExperienceModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry) {
        super(localization -> localization.getMessage().getExperience(), MessageType.EXPERIENCE);

        this.message = fileResolver.getMessage().getExperience();
        this.permission = fileResolver.getPermission().getMessage().getExperience();
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());

        listenerRegistry.register(ExperiencePulseListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Experience experience) {
        if (isModuleDisabledFor(fPlayer)) return;
        if (experience.isIncorrect()) return;

        sendMessage(ExperienceMetadata.<Localization.Message.Experience>builder()
                .sender(experience.target() == null ? fPlayer : experience.target())
                .filterPlayer(fPlayer)
                .format(string -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_SINGLE -> string.getAdd().getLevels().getSingle();
                            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_MULTIPLE -> string.getAdd().getLevels().getMultiple();
                            case COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_SINGLE -> string.getAdd().getPoints().getSingle();
                            case COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_MULTIPLE -> string.getAdd().getPoints().getMultiple();
                            case COMMANDS_EXPERIENCE_QUERY_LEVELS -> string.getQuery().getLevels();
                            case COMMANDS_EXPERIENCE_QUERY_POINTS -> string.getQuery().getPoints();
                            case COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_SINGLE -> string.getSet().getLevels().getSingle();
                            case COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_MULTIPLE -> string.getSet().getLevels().getMultiple();
                            case COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_SINGLE -> string.getSet().getPoints().getSingle();
                            case COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_MULTIPLE -> string.getSet().getPoints().getMultiple();
                            default -> "";
                        },
                        new String[]{"<amount>", "<count>"},
                        new String[]{experience.amount(), StringUtils.defaultString(experience.count())}
                ))
                .destination(message.getDestination())
                .sound(getModuleSound())
                .experience(experience)
                .translationKey(translationKey)
                .build()
        );
    }
}