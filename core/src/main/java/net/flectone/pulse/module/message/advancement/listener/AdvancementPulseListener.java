package net.flectone.pulse.module.message.advancement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.advancement.extractor.AdvancementExtractor;
import net.flectone.pulse.module.message.advancement.model.Advancement;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class AdvancementPulseListener implements PulseListener {

    private final AdvancementModule advancementModule;
    private final AdvancementExtractor advancementExtractor;

    @Inject
    public AdvancementPulseListener(AdvancementModule advancementModule,
                                    AdvancementExtractor advancementExtractor) {
        this.advancementModule = advancementModule;
        this.advancementExtractor = advancementExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case CHAT_TYPE_ADVANCEMENT_TASK, CHAT_TYPE_ADVANCEMENT_GOAL, CHAT_TYPE_ADVANCEMENT_CHALLENGE,
                 CHAT_TYPE_ACHIEVEMENT, CHAT_TYPE_ACHIEVEMENT_TAKEN -> {
                Optional<Advancement> advancement = advancementExtractor.extractAdvancement(event.getTranslatableComponent());
                if (advancement.isEmpty()) return;

                event.setCancelled(true);
                advancementModule.sendChatAdvancement(event.getFPlayer(), translationKey, advancement.get());
            }
            case COMMANDS_ADVANCEMENT_GRANT_MANY_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_MANY_SUCCESS,
                 COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS,
                 COMMANDS_ADVANCEMENT_GRANT_ONE_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_MANY_SUCCESS,
                 COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS,
                 COMMANDS_ACHIEVEMENT_GIVE_SUCCESS_ALL, COMMANDS_ACHIEVEMENT_TAKE_SUCCESS_ALL,
                 COMMANDS_ACHIEVEMENT_GIVE_SUCCESS_ONE, COMMANDS_ACHIEVEMENT_TAKE_SUCCESS_ONE -> {
                Optional<Advancement> advancement = advancementExtractor.extractCommandAdvancement(translationKey, event.getTranslatableComponent());
                if (advancement.isEmpty()) return;

                event.setCancelled(true);
                advancementModule.sendCommandAdvancement(event.getFPlayer(), translationKey, advancement.get());
            }
            case COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_CRITERION_TO_ONE_SUCCESS,
                 COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_MANY_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_CRITERION_TO_ONE_SUCCESS -> {
                Optional<Advancement> advancement = advancementExtractor.extractCriterionAdvancement(translationKey, event.getTranslatableComponent());
                if (advancement.isEmpty()) return;

                event.setCancelled(true);
                advancementModule.sendCommandAdvancement(event.getFPlayer(), translationKey, advancement.get());
            }
        }
    }
}
