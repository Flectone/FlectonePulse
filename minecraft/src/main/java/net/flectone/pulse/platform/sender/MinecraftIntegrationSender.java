package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.vanilla.model.VanillaMetadata;
import net.flectone.pulse.util.constant.MessageType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Singleton
public class MinecraftIntegrationSender extends IntegrationSender {

    @Inject
    public MinecraftIntegrationSender(IntegrationModule integrationModule,
                                      MessagePipeline messagePipeline,
                                      TaskScheduler taskScheduler) {
        super(integrationModule, messagePipeline, taskScheduler);
    }

    @Override
    protected Collection<String> createSpecificMessageNames(MessageType messageType, EventMetadata<?> eventMetadata) {
        if (messageType == MessageType.VANILLA) {
            if (!(eventMetadata instanceof VanillaMetadata<?> vanillaMetadata)) return Collections.emptyList();

            String vanillaMessageName = vanillaMetadata.parsedComponent().vanillaMessage().name();
            if (vanillaMessageName.isEmpty()) return Collections.emptyList();

            return List.of(vanillaMessageName.toUpperCase(), vanillaMetadata.parsedComponent().translationKey());
        }

        return createSpecificMessageNames(messageType, eventMetadata);
    }
}
