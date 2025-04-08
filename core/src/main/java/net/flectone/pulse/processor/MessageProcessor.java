package net.flectone.pulse.processor;

import net.flectone.pulse.context.MessageContext;

@FunctionalInterface
public interface MessageProcessor {
    void process(MessageContext messageContext);
}
