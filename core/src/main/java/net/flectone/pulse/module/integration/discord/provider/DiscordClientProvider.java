package net.flectone.pulse.module.integration.discord.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.common.ReactorResources;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.ApplicationInfo;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Status;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.gateway.GatewayReactorResources;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.request.RouterOptions;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.discord.model.DiscordClient;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.generator.RandomGenerator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DiscordClientProvider {

    private final DiscordModule discordModule;
    private final SystemVariableResolver systemVariableResolver;
    private final RandomGenerator randomGenerator;

    private volatile DiscordClient discordClient;

    @Nullable
    public DiscordClient create() {
        discordClient = null;

        String token = systemVariableResolver.substituteEnvVars(discordModule.config().token());
        if (StringUtils.isEmpty(token)) return discordClient;

        discord4j.core.DiscordClient discord4JClient = createDiscord4JClient(createHttpClient());
        GatewayDiscordClient gateway = createGatewayClient(discord4JClient, createHttpClient(), createClientPresence());
        if (gateway == null) return discordClient;

        ApplicationInfo applicationInfo = gateway.getApplicationInfo().block();
        if (applicationInfo == null) return discordClient;

        long clientID = applicationInfo.getId().asLong();

        FPlayer fPlayer = FPlayer.builder()
                .id(randomGenerator.nextInt(Integer.MIN_VALUE, -1))
                .name(discordModule.localization().senderName())
                .uuid(UUID.randomUUID())
                .type(FPlayer.INTEGRATION_TYPE)
                .build();

        discordClient = new DiscordClient(
                token,
                fPlayer,
                discord4JClient,
                gateway,
                clientID
        );

        return discordClient;
    }

    @Nullable
    public DiscordClient get() {
        return discordClient;
    }

    @Nullable
    private ClientPresence createClientPresence() {
        Integration.Discord.Presence presence = discordModule.config().presence();
        if (!presence.enable()) return null;

        Integration.Discord.Presence.Activity activity = presence.activity();

        ClientActivity clientActivity = activity.enable()
                ? ClientActivity.of(Activity.Type.valueOf(activity.type()), activity.name(), activity.url())
                : null;

        return ClientPresence.of(Status.valueOf(presence.status()), clientActivity);
    }

    @Nullable
    private HttpClient createHttpClient() {
        Integration.Proxy proxy = discordModule.config().proxy();
        if (proxy.type() == Proxy.Type.DIRECT) {
            return null;
        }

        return HttpClient.create()
                .keepAlive(false)
                .compress(true)
                .followRedirect(true)
                .proxy(typeSpec -> {
                    ProxyProvider.Builder proxyProviderBuilder = typeSpec
                            .type(proxy.type() == Proxy.Type.HTTP ? ProxyProvider.Proxy.HTTP : ProxyProvider.Proxy.SOCKS5)
                            .socketAddress(new InetSocketAddress(proxy.host(), proxy.port()));

                    if (StringUtils.isNotEmpty(proxy.user()) && StringUtils.isNotEmpty(proxy.password())) {
                        proxyProviderBuilder
                                .username(systemVariableResolver.substituteEnvVars(proxy.user()))
                                .password(_ -> systemVariableResolver.substituteEnvVars(proxy.password()))
                                .connectTimeoutMillis(30000);
                    }
                });
    }

    private discord4j.core.@NonNull DiscordClient createDiscord4JClient(@Nullable HttpClient httpClient) {
        DiscordClientBuilder<discord4j.core.@NonNull DiscordClient, @NonNull RouterOptions> discordClientBuilder = discord4j.core.DiscordClient.builder(systemVariableResolver.substituteEnvVars(discordModule.config().token()));
        if (httpClient == null) return discordClientBuilder.build();

        discordClientBuilder.setReactorResources(ReactorResources.builder()
                .httpClient(httpClient)
                .build()
        );

        return discordClientBuilder.build();
    }

    @Nullable
    private GatewayDiscordClient createGatewayClient(discord4j.core.@NonNull DiscordClient discordClient,
                                                     @Nullable HttpClient httpClient,
                                                     @Nullable ClientPresence clientPresence) {
        GatewayBootstrap<?> gatewayBootstrap = discordClient.gateway()
                .setEnabledIntents(IntentSet.nonPrivileged().or(IntentSet.of(Intent.MESSAGE_CONTENT, Intent.GUILD_PRESENCES)));

        if (clientPresence != null) {
            gatewayBootstrap = gatewayBootstrap.setInitialPresence(_ -> clientPresence);
        }

        if (httpClient != null) {
            gatewayBootstrap = gatewayBootstrap.setGatewayReactorResources(reactorResources -> GatewayReactorResources.builder(reactorResources)
                    .httpClient(httpClient)
                    .build()
            );
        }

        return gatewayBootstrap.login().block();
    }

}
