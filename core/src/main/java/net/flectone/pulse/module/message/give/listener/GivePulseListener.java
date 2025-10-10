package net.flectone.pulse.module.message.give.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.give.GiveModule;
import net.flectone.pulse.module.message.give.extractor.GiveExtractor;
import net.flectone.pulse.module.message.give.model.Give;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GivePulseListener implements PulseListener {

    private final GiveModule giveModule;
    private final GiveExtractor giveExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (!translationKey.startsWith("commands.give.success")) return;

        Optional<Give> give = giveExtractor.extract(translationKey, event.getTranslatableComponent());
        if (give.isEmpty()) return;

        translationKey = give.get().getPlayers() == null ? translationKey : MinecraftTranslationKey.COMMANDS_GIVE_SUCCESS_MULTIPLE;

        event.setCancelled(true);
        giveModule.send(event.getFPlayer(), translationKey, give.get());
    }

}