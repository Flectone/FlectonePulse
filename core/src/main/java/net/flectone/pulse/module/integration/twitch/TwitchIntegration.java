package net.flectone.pulse.module.integration.twitch;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.twitch.listener.TwitchMessageListener;
import net.flectone.pulse.module.integration.twitch.model.TwitchClient;
import net.flectone.pulse.module.integration.twitch.provider.TwitchClientProvider;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchIntegration implements FIntegration {

    private final AtomicLong taskGeneration = new AtomicLong(0);

    private final TwitchModule twitchModule;
    private final TwitchClientProvider twitchClientProvider;
    private final PlatformServerAdapter platformServerAdapter;
    private final TaskScheduler taskScheduler;
    private final Injector injector;

    @Getter private final FLogger fLogger;

    @Override
    public String getIntegrationName() {
        return "Twitch";
    }

    @Override
    public void hook() {
        long taskId = taskGeneration.incrementAndGet();

        TwitchClient twitchClient = twitchClientProvider.create();
        if (twitchClient == null) return;

        if (taskGeneration.get() != taskId) {
            twitchClient.client().close();
            return;
        }

        Integration.Twitch integration = twitchModule.config();
        for (List<String> channels : integration.messageChannel().values()) {
            for (String channel : channels) {
                TwitchChat twitchChat = twitchClient.client().getChat();
                if (!twitchChat.isChannelJoined(channel)) {
                    twitchChat.joinChannel(channel);
                }
            }
        }

        for (String channel : integration.followChannel().keySet()) {
            twitchClient.client().getClientHelper().enableStreamEventListener(channel);
        }

        twitchClient.client().getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
            String channelName = event.getChannel().getName();

            List<String> commands = integration.followChannel().get(channelName);
            if (commands == null) return;

            commands.forEach(platformServerAdapter::dispatchCommand);
        });

        if (!integration.messageChannel().isEmpty()) {
            TwitchMessageListener twitchMessageListener = injector.getInstance(TwitchMessageListener.class);

            twitchClient.client().getEventManager().onEvent(twitchMessageListener.getEventType(), channelMessageEvent ->
                    taskScheduler.runAsync(() -> twitchMessageListener.execute(channelMessageEvent))
            );
        }

        logHook();
    }

    @Override
    public void unhook() {
        TwitchClient twitchClient = twitchClientProvider.get();
        if (twitchClient == null) return;

        twitchClient.client().close();

        logUnhook();
    }
}
