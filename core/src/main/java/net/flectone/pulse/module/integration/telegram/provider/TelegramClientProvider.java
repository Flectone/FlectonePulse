package net.flectone.pulse.module.integration.telegram.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.telegram.model.TelegramClient;
import net.flectone.pulse.processing.resolver.SystemVariableResolver;
import net.flectone.pulse.util.logging.FLogger;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.api.methods.GetMe;

import java.lang.reflect.Field;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TelegramClientProvider {

    private final TelegramModule telegramModule;
    private final SystemVariableResolver systemVariableResolver;
    private final FLogger fLogger;

    private volatile TelegramClient telegramClient;

    @Nullable
    public TelegramClient create() {
        String token = systemVariableResolver.substituteEnvVars(telegramModule.config().token());
        if (token.isEmpty()) return null;

        try {
            // create client
            OkHttpTelegramClient client = new OkHttpTelegramClient(createHttpClient(), token);

            // create application
            TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication();

            // due to jackson relocation, we can't use the constructor with the OkHttpClient change, so we do it via reflection
            // waiting for https://github.com/rubenlagus/TelegramBots/pull/1583
            Field okHttpClientCreatorField = TelegramBotsLongPollingApplication.class.getDeclaredField("okHttpClientCreator");
            okHttpClientCreatorField.setAccessible(true);
            okHttpClientCreatorField.set(application, (Supplier<OkHttpClient>) () -> createHttpClient());

            // get bot id
            long id = client.execute(new GetMe()).getId();

            telegramClient = new TelegramClient(
                    token,
                    FPlayer.builder().integration(true).name(telegramModule.localization().senderName()).build(),
                    client,
                    application,
                    id
            );

        } catch (Exception e) {
            fLogger.warning(e);
        }

        return telegramClient;
    }

    @Nullable
    public TelegramClient get() {
        return telegramClient;
    }

    @NonNull
    private OkHttpClient createHttpClient() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(100);
        dispatcher.setMaxRequestsPerHost(100);

        OkHttpClient.Builder builder = new OkHttpClient()
                .newBuilder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(
                        100,
                        75,
                        TimeUnit.SECONDS
                ))
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(70, TimeUnit.SECONDS)
                .connectTimeout(75, TimeUnit.SECONDS);

        Integration.Proxy proxy = telegramModule.config().proxy();
        if (proxy.type() == Proxy.Type.DIRECT) {
            return builder.build();
        }

        InetSocketAddress socketAddress = new InetSocketAddress(proxy.host(), proxy.port());
        builder.proxy(new Proxy(proxy.type(), socketAddress));

        if (StringUtils.isNotEmpty(proxy.user()) && StringUtils.isNotEmpty(proxy.password())) {
            if (proxy.type() == Proxy.Type.HTTP) {
                builder.proxyAuthenticator((_, response) ->
                        response.request().newBuilder()
                                .header("Proxy-Authorization", Credentials.basic(
                                        systemVariableResolver.substituteEnvVars(proxy.user()),
                                        systemVariableResolver.substituteEnvVars(proxy.password())
                                ))
                                .build()
                );
            } else {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    public PasswordAuthentication requestPasswordAuthenticationInstance(String host,
                                                                                        InetAddress addr,
                                                                                        int port,
                                                                                        String protocol,
                                                                                        String prompt,
                                                                                        String scheme,
                                                                                        URL url,
                                                                                        RequestorType reqType) {
                        if (proxy.host().equalsIgnoreCase(host) && proxy.port() == port) {
                            return new PasswordAuthentication(proxy.user(), proxy.password().toCharArray());
                        }

                        return null;
                    }
                });
            }
        }

        return builder.build();
    }

}
