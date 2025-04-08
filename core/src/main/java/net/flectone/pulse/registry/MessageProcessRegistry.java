package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.processor.MessageProcessor;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

@Getter
@Singleton
public class MessageProcessRegistry {

    private final TreeMap<Integer, Set<MessageProcessor>> processors = new TreeMap<>();

    @Inject
    public MessageProcessRegistry() {

    }

    public void register(int priority, MessageProcessor messageProcessor) {
        processors.computeIfAbsent(priority, i -> new HashSet<>())
                .add(messageProcessor);
    }

}
