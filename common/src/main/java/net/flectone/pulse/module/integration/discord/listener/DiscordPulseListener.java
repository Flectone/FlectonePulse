package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.MessagePrepareEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.scheduler.TaskScheduler;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordPulseListener implements PulseListener {

    private final TaskScheduler taskScheduler;
    private final DiscordModule discordModule;

    @Pulse(priority = Event.Priority.LOW)
    public void onMessagePrepareEvent(MessagePrepareEvent event) {
        IntegrationMessageFormat integrationMessageFormat = event.integrationMessageFormat();
        if (integrationMessageFormat == null) return;

        ModuleName moduleName = event.moduleName();
        if (moduleName == ModuleName.INTEGRATION_DISCORD) return;

        MessageContext messageContext = event.messageContext();
        taskScheduler.runAsync(ModuleName.INTEGRATION_DISCORD, () ->
                discordModule.sendMessage(moduleName, messageContext, integrationMessageFormat)
        );
    }

}
