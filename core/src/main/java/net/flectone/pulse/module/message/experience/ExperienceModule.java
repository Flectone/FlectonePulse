package net.flectone.pulse.module.message.experience;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.message.experience.listener.ExperiencePulseListener;
import net.flectone.pulse.module.message.experience.model.Experience;
import net.flectone.pulse.module.message.experience.model.ExperienceMetadata;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class ExperienceModule extends AbstractModuleLocalization<Localization.Message.Experience> {

    private final FileResolver fileResolver;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public ExperienceModule(FileResolver fileResolver,
                            ListenerRegistry listenerRegistry) {
        super(MessageType.EXPERIENCE);

        this.fileResolver = fileResolver;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        createSound(config().getSound(), permission().getSound());

        listenerRegistry.register(ExperiencePulseListener.class);
    }

    @Override
    public Message.Experience config() {
        return fileResolver.getMessage().getExperience();
    }

    @Override
    public Permission.Message.Experience permission() {
        return fileResolver.getPermission().getMessage().getExperience();
    }

    @Override
    public Localization.Message.Experience localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getExperience();
    }

    @Async
    public void send(FPlayer fPlayer, MinecraftTranslationKey translationKey, Experience experience) {
        if (isModuleDisabledFor(fPlayer)) return;

        sendMessage(ExperienceMetadata.<Localization.Message.Experience>builder()
                .sender(fPlayer)
                .range(config().getRange())
                .format(localization -> StringUtils.replaceEach(
                        switch (translationKey) {
                            case COMMANDS_XP_SUCCESS_NEGATIVE_LEVELS -> localization.getTaken();
                            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_SINGLE, COMMANDS_XP_SUCCESS_LEVELS -> localization.getAdd().getLevels().getSingle();
                            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_MULTIPLE -> localization.getAdd().getLevels().getMultiple();
                            case COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_SINGLE, COMMANDS_XP_SUCCESS -> localization.getAdd().getPoints().getSingle();
                            case COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_MULTIPLE -> localization.getAdd().getPoints().getMultiple();
                            case COMMANDS_EXPERIENCE_QUERY_LEVELS -> localization.getQuery().getLevels();
                            case COMMANDS_EXPERIENCE_QUERY_POINTS -> localization.getQuery().getPoints();
                            case COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_SINGLE -> localization.getSet().getLevels().getSingle();
                            case COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_MULTIPLE -> localization.getSet().getLevels().getMultiple();
                            case COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_SINGLE -> localization.getSet().getPoints().getSingle();
                            case COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_MULTIPLE -> localization.getSet().getPoints().getMultiple();
                            default -> "";
                        },
                        new String[]{"<amount>", "<players>"},
                        new String[]{experience.getAmount(), StringUtils.defaultString(experience.getPlayers())}
                ))
                .destination(config().getDestination())
                .sound(getModuleSound())
                .experience(experience)
                .translationKey(translationKey)
                .tagResolvers(fResolver -> new TagResolver[]{targetTag(fResolver, experience.getTarget())})
                .build()
        );
    }
}