package net.flectone.pulse.module.message.experience.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.experience.ExperienceModule;
import net.flectone.pulse.module.message.experience.extractor.ExperienceExtractor;
import net.flectone.pulse.module.message.experience.model.Experience;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExperiencePulseListener implements PulseListener {

    private final ExperienceModule experienceModule;
    private final ExperienceExtractor experienceExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_SINGLE, COMMANDS_EXPERIENCE_ADD_LEVELS_SUCCESS_MULTIPLE,
                 COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_SINGLE, COMMANDS_EXPERIENCE_ADD_POINTS_SUCCESS_MULTIPLE,
                 COMMANDS_EXPERIENCE_QUERY_LEVELS, COMMANDS_EXPERIENCE_QUERY_POINTS,
                 COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_SINGLE, COMMANDS_EXPERIENCE_SET_LEVELS_SUCCESS_MULTIPLE,
                 COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_SINGLE, COMMANDS_EXPERIENCE_SET_POINTS_SUCCESS_MULTIPLE,
                 COMMANDS_XP_SUCCESS,  COMMANDS_XP_SUCCESS_LEVELS, COMMANDS_XP_SUCCESS_NEGATIVE_LEVELS -> {
                Optional<Experience> optionalExperience = experienceExtractor.extract(translationKey, event.getTranslatableComponent());
                if (optionalExperience.isEmpty()) return;

                event.setCancelled(true);
                experienceModule.send(event.getFPlayer(), translationKey, optionalExperience.get());
            }
        }
    }

}