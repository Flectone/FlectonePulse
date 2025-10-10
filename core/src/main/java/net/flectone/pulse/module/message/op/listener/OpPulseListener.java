package net.flectone.pulse.module.message.op.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.op.OpModule;
import net.flectone.pulse.module.message.op.extractor.OpExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class OpPulseListener implements PulseListener {

    private final OpModule opModule;
    private final OpExtractor opExtractor;

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        MinecraftTranslationKey translationKey = event.getTranslationKey();
        if (translationKey != MinecraftTranslationKey.COMMANDS_OP_SUCCESS) return;

        Optional<FEntity> target = opExtractor.extract(event.getTranslatableComponent());
        if (target.isEmpty()) return;

        event.setCancelled(true);
        opModule.send(event.getFPlayer(), translationKey, target.get());
    }

}
