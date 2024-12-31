package net.flectone.pulse.module.integration.twitch;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import feign.Logger;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.FIntegration;
import net.flectone.pulse.module.integration.twitch.listener.ChannelMessageListener;
import net.flectone.pulse.util.CommandUtil;
import net.flectone.pulse.util.MessageTag;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
public class TwitchIntegration implements FIntegration {

    private final Integration.Twitch integration;
    private final Localization.Integration.Twitch localization;

    private final ChannelMessageListener channelMessageListener;
    private final CommandUtil commandUtil;
    private final FLogger fLogger;

    private OAuth2Credential oAuth2Credential;
    private TwitchClient twitchClient;

    @Inject
    public TwitchIntegration(FileManager fileManager,
                             CommandUtil commandUtil,
                             ChannelMessageListener channelMessageListener,
                             FLogger fLogger) {
        this.channelMessageListener = channelMessageListener;
        this.commandUtil = commandUtil;
        this.fLogger = fLogger;

        integration = fileManager.getIntegration().getTwitch();
        localization = fileManager.getLocalization().getIntegration().getTwitch();
    }

    @Async
    @Override
    public void hook() {
        disconnect();

        String token = integration.getToken();
        String identityProvider = integration.getClientID();
        if (token.isEmpty() || identityProvider.isEmpty()) return;

        oAuth2Credential = new OAuth2Credential(identityProvider, token);
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

        for (String channel : integration.getFollowChannel().keySet()) {
            twitchClient.getClientHelper().enableStreamEventListener(channel);
        }

        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, event -> {
            String channelName = event.getChannel().getName();

            List<String> commands = integration.getFollowChannel().get(channelName);
            if (commands == null) return;

            commands.forEach(commandUtil::dispatch);
        });

        if (!integration.getMessageChannel().isEmpty()) {
            twitchClient.getEventManager().onEvent(channelMessageListener.getEventType(), channelMessageListener::execute);
        }

        fLogger.info("Twitch integration enabled");
    }

    @Async
    public void sendMessage(FEntity sender, MessageTag messageTag, UnaryOperator<String> twitchString) {
        List<String> channels = integration.getMessageChannel().get(messageTag);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        String message = localization.getMessageChannel().get(messageTag);
        if (message == null) return;
        if (message.isEmpty()) return;

        message = twitchString.apply(message);
        for (String channel : channels) {
            twitchClient.getChat().sendMessage(channel, message);
        }
    }

    public void disconnect() {
        if (twitchClient == null) return;

        twitchClient.close();
    }
}
