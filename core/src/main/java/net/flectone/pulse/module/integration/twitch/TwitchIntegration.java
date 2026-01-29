package net.flectone.pulse.module.integration.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import feign.Logger;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.twitch.listener.ChannelMessageListener;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchIntegration implements FIntegration {

    private final FileFacade fileFacade;
    private final ChannelMessageListener channelMessageListener;
    private final PlatformServerAdapter platformServerAdapter;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;

    @Getter private FPlayer sender = FPlayer.UNKNOWN;
    private TwitchClient twitchClient;

    @Override
    public void hook() {
        sender = new FPlayer(fileFacade.localization().integration().twitch().senderName());

        Integration.Twitch integration = fileFacade.integration().twitch();
        String token = systemVariableResolver.substituteEnvVars(integration.token());
        String identityProvider = systemVariableResolver.substituteEnvVars(integration.clientID());
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

        for (List<String> channels : integration.messageChannel().values()) {
            for (String channel : channels) {
                if (twitchClient.getChat().isChannelJoined(channel)) continue;
                twitchClient.getChat().joinChannel(channel);
            }
        }

        for (String channel : integration.followChannel().keySet()) {
            twitchClient.getClientHelper().enableStreamEventListener(channel);
        }

        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
            String channelName = event.getChannel().getName();

            List<String> commands = integration.followChannel().get(channelName);
            if (commands == null) return;

            commands.forEach(platformServerAdapter::dispatchCommand);
        });

        if (!integration.messageChannel().isEmpty()) {
            twitchClient.getEventManager().onEvent(channelMessageListener.getEventType(), channelMessageListener::execute);
        }

        fLogger.info("✔ Twitch integration enabled");
    }

    public void sendMessage(FEntity sender, String messageName, UnaryOperator<String> twitchString) {
        List<String> channels = fileFacade.integration().twitch().messageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        String message = fileFacade.localization().integration().twitch().messageChannel().getOrDefault(messageName, "<final_message>");
        if (StringUtils.isEmpty(message)) return;

        message = twitchString.apply(message);
        if (StringUtils.isEmpty(message)) return;

        for (String channel : channels) {
            sendMessage(channel, message);
        }
    }

    public void sendMessage(String channel, String message) {
        twitchClient.getChat().sendMessage(channel, message);
    }

    @Override
    public void unhook() {
        if (twitchClient == null) return;

        twitchClient.close();

        fLogger.info("✖ Twitch integration disabled");
    }
}
