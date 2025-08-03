package net.flectone.pulse.module.message.advancement.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.TranslatableMessageReceiveEvent;
import net.flectone.pulse.module.message.advancement.AdvancementModule;
import net.flectone.pulse.module.message.advancement.extractor.AdvancementExtractor;
import net.flectone.pulse.module.message.advancement.model.ChatAdvancement;
import net.flectone.pulse.module.message.advancement.model.CommandAdvancement;
import net.flectone.pulse.processing.resolver.FileResolver;

import java.util.Optional;

@Singleton
public class AdvancementPulseListener implements PulseListener {

    private final Message.Advancement message;
    private final AdvancementModule advancementModule;
    private final AdvancementExtractor advancementExtractor;

    @Inject
    public AdvancementPulseListener(FileResolver fileResolver,
                                    AdvancementModule advancementModule,
                                    AdvancementExtractor advancementExtractor) {
        this.message = fileResolver.getMessage().getAdvancement();
        this.advancementModule = advancementModule;
        this.advancementExtractor = advancementExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(TranslatableMessageReceiveEvent event) {
        switch (event.getKey()) {
            case CHAT_TYPE_ADVANCEMENT_TASK, CHAT_TYPE_ADVANCEMENT_GOAL, CHAT_TYPE_ADVANCEMENT_CHALLENGE,
                 CHAT_TYPE_ACHIEVEMENT, CHAT_TYPE_ACHIEVEMENT_TAKEN -> {
                Optional<ChatAdvancement> advancement = advancementExtractor.extractFromChat(event);
                if (advancement.isEmpty()) return;

                event.setCancelled(true);
                advancementModule.send(event.getFPlayer(), advancement.get());
            }
            case COMMANDS_ADVANCEMENT_GRANT_ONE_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_MANY_TO_ONE_SUCCESS,
                 COMMANDS_ACHIEVEMENT_GIVE_ONE, COMMANDS_ACHIEVEMENT_GIVE_MANY,
                 COMMANDS_ADVANCEMENT_GRANT_ONLY_SUCCESS, COMMANDS_ADVANCEMENT_GRANT_EVERYTHING_SUCCESS -> {
                if (!message.isGrant()) return;

                Optional<CommandAdvancement> commandAdvancement = advancementExtractor.extractFromCommand(event);
                if (commandAdvancement.isEmpty()) return;

                event.setCancelled(true);
                advancementModule.send(false, event.getFPlayer(), commandAdvancement.get());
            }
            case COMMANDS_ADVANCEMENT_REVOKE_MANY_TO_ONE_SUCCESS, COMMANDS_ADVANCEMENT_REVOKE_ONE_TO_ONE_SUCCESS,
                 COMMANDS_ACHIEVEMENT_TAKE_MANY, COMMANDS_ACHIEVEMENT_TAKE_ONE,
                 COMMANDS_ADVANCEMENT_REVOKE_EVERYTHING_SUCCESS,COMMANDS_ADVANCEMENT_REVOKE_ONLY_SUCCESS -> {
                if (!message.isRevoke()) return;

                Optional<CommandAdvancement> commandAdvancement = advancementExtractor.extractFromCommand(event);
                if (commandAdvancement.isEmpty()) return;

                event.setCancelled(true);
                advancementModule.send(true, event.getFPlayer(), commandAdvancement.get());
            }
        }
    }
}
