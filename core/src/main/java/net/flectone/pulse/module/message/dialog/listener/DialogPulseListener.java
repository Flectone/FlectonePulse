package net.flectone.pulse.module.message.dialog.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.dialog.DialogModule;
import net.flectone.pulse.module.message.dialog.extractor.DialogExtractor;
import net.flectone.pulse.module.message.dialog.model.Dialog;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DialogPulseListener implements PulseListener {

    private final DialogModule dialogModule;
    private final DialogExtractor dialogExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        switch (translationKey) {
            case COMMANDS_DIALOG_CLEAR_MULTIPLE, COMMANDS_DIALOG_SHOW_MULTIPLE -> {
                Optional<Dialog> optionalDialog = dialogExtractor.extractMultiple(event.getTranslatableComponent());
                if (optionalDialog.isEmpty()) return;

                event.setCancelled(true);
                dialogModule.send(event.getFPlayer(), event.getTranslationKey(), optionalDialog.get());
            }
            case COMMANDS_DIALOG_CLEAR_SINGLE, COMMANDS_DIALOG_SHOW_SINGLE -> {
                Optional<Dialog> optionalDialog = dialogExtractor.extractSingle(event.getTranslatableComponent());
                if (optionalDialog.isEmpty()) return;

                event.setCancelled(true);
                dialogModule.send(event.getFPlayer(), event.getTranslationKey(), optionalDialog.get());
            }
        }
    }

}