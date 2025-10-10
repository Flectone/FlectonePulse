package net.flectone.pulse.module.integration.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.twitch.listener.ChannelMessageListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchIntegration implements FIntegration {

    private final FileResolver fileResolver;
    private final ChannelMessageListener channelMessageListener;
    private final PlatformServerAdapter platformServerAdapter;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;

    private TwitchClient twitchClient;

    @Override
    public void hook() {
        Integration.Twitch integration = fileResolver.getIntegration().getTwitch();
        String token = systemVariableResolver.substituteEnvVars(integration.getToken());
        String identityProvider = systemVariableResolver.substituteEnvVars(integration.getClientID());
        if (token.isEmpty() || identityProvider.isEmpty()) return;

        OAuth2Credential oAuth2Credential = new OAuth2Credential(identityProvider, token);
        twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withEnableEventSocket(true)
                .withEnableHelix(true)
                .withFeignLogLevel(Logger.Level.NONE)
                .withDefaultAuthToken(oAuth2Credential)
                .withChatAccount(oAuth2Credential)
                .build();

        for (List<String> channels : integration.getMessageChannel().values()) {
            for (String channel : channels) {
                if (twitchClient.getChat().isChannelJoined(channel)) continue;
                twitchClient.getChat().joinChannel(channel);
            }
        }

        if (!integration.getFollowChannel().isEmpty()) {
            fLogger.warning("Follow channel is currently not supported");
        }

        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
            String channelName = event.getChannel().getName();

            List<String> commands = integration.getFollowChannel().get(channelName);
            if (commands == null) return;

            commands.forEach(platformServerAdapter::dispatchCommand);
        });

        if (!integration.getMessageChannel().isEmpty()) {
            twitchClient.getEventManager().onEvent(channelMessageListener.getEventType(), channelMessageListener::execute);
        }

        fLogger.info("✔ Twitch integration enabled");
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> twitchString) {
        List<String> channels = fileResolver.getIntegration().getTwitch().getMessageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        String message = fileResolver.getLocalization().getIntegration().getTwitch().getMessageChannel().get(messageName);
        if (message == null) return;
        if (message.isEmpty()) return;

        message = twitchString.apply(message);
        for (String channel : channels) {
            twitchClient.getChat().sendMessage(channel, message);
        }
    }

    @Override
    public void unhook() {
        if (twitchClient == null) return;

        twitchClient.close();

        fLogger.info("✖ Twitch integration disabled");
    }
}
