package net.flectone.pulse.module.message.op.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.op.OpModule;
import net.flectone.pulse.module.message.op.extractor.OpExtractor;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

import java.util.Optional;

@Singleton
public class OpPulseListener implements PulseListener {

    private final OpModule opModule;
    private final OpExtractor opExtractor;

    @Inject
    public OpPulseListener(OpModule opModule,
                           OpExtractor opExtractor) {
        this.opModule = opModule;
        this.opExtractor = opExtractor;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_OP_SUCCESS) return;

        Optional<String> target = opExtractor.extract(event);
        if (target.isEmpty()) return;

        event.setCancelled(true);
        opModule.send(event.getFPlayer(), target.get());
    }

}
