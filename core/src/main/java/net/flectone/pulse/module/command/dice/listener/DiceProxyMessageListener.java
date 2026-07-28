package net.flectone.pulse.module.command.dice.listener;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.message.ProxyMessageEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.command.dice.DiceModule;
import net.flectone.pulse.module.command.dice.model.DiceMessageContext;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.io.ProxyPayload;

import java.io.IOException;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiceProxyMessageListener implements PulseListener {

    private final DiceModule diceModule;
    private final ModuleController moduleController;
    private final MessageDispatcher messageDispatcher;
    private final Gson gson;

    @Pulse
    public Event onProxyMessageEvent(ProxyMessageEvent event) throws IOException {
        if (event.processed()) return event;
        if (event.name() != ModuleName.COMMAND_DICE) return event;
        if (moduleController.isDisabledFor(diceModule, event.sender())) return event.withProcessed(true);
        if (!diceModule.config().range().is(Range.Type.PROXY)) return event.withProcessed(true);

        try (ProxyPayload proxyPayload = event.openPayload()) {
            List<Integer> cubes = gson.fromJson(proxyPayload.readString(), new TypeToken<List<Integer>>() {}.getType());

            messageDispatcher.dispatch(diceModule, EventMetadata.builder()
                    .range(Range.get(Range.Type.SERVER))
                    .destination(diceModule.config().destination())
                    .sound(diceModule.soundOrThrow())
                    .messageContext(fResolver -> DiceMessageContext.builder()
                            .base(MessageContext.builder()
                                    .uuid(event.uuid())
                                    .sender(event.sender())
                                    .receiver(fResolver)
                                    .message(diceModule.replaceResult(fResolver, cubes))
                                    .build()
                            )
                            .cubes(cubes)
                            .build()
                    )
                    .build()
            );
        }

        return event.withProcessed(true);
    }

}
