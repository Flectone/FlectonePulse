package net.flectone.pulse.module.integration.twitch.provider;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.common.config.ProxyConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.twitch.model.TwitchClient;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.generator.RandomGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.Proxy;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchClientProvider {

    private final SystemVariableResolver systemVariableResolver;
    private final TwitchModule twitchModule;
    private final RandomGenerator randomGenerator;

    private volatile TwitchClient twitchClient;

    @Nullable
    public TwitchClient create() {
        twitchClient = null;

        Integration.Twitch integration = twitchModule.config();
        String token = systemVariableResolver.substituteEnvVars(integration.token());
        String identityProvider = systemVariableResolver.substituteEnvVars(integration.clientID());
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(identityProvider)) return twitchClient;

        com.github.twitch4j.TwitchClient client = createTwitch4JClient(identityProvider, token);

        FPlayer fPlayer = FPlayer.builder()
                .id(randomGenerator.nextInt(Integer.MIN_VALUE, -1))
                .name(twitchModule.localization().senderName())
                .uuid(UUID.randomUUID())
                .type(FPlayer.INTEGRATION_TYPE)
                .build();

        twitchClient = new TwitchClient(
                token,
                identityProvider,
                fPlayer,
                client
        );

        return twitchClient;
    }

    @Nullable
    public TwitchClient get() {
        return twitchClient;
    }

    private com.github.twitch4j.TwitchClient createTwitch4JClient(@NonNull String identityProvider,
                                                                  @NonNull String token) {
        OAuth2Credential oAuth2Credential = new OAuth2Credential(identityProvider, token);

        TwitchClientBuilder twitchClientBuilder = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withEnableEventSocket(true)
                .withEnableHelix(true)
                .withFeignLogLevel(Logger.Level.NONE)
                .withDefaultAuthToken(oAuth2Credential)
                .withChatAccount(oAuth2Credential);

        Integration.Proxy proxy = twitchModule.config().proxy();
        if (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.SOCKS) {
            return twitchClientBuilder.build();
        }

        ProxyConfig proxyConfig = ProxyConfig.builder()
                .hostname(proxy.host())
                .port(proxy.port())
                .username(StringUtils.isNotEmpty(proxy.user()) ? systemVariableResolver.substituteEnvVars(proxy.user()) : null)
                .password(StringUtils.isNotEmpty(proxy.password()) ? systemVariableResolver.substituteEnvVars(proxy.password()).toCharArray() : null)
                .build();

        return twitchClientBuilder.withProxyConfig(proxyConfig).build();
    }

}
