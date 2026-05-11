package net.flectone.pulse.processing.resolver;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.util.WebUtil;
import net.flectone.pulse.util.logging.FLogger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleProfileResolver extends ProfileResolver {

    private static final String HYTALE_API = "https://playerdb.co/api/player/hytale/";

    private final HttpClient httpClient;
    private final FLogger fLogger;
    private final Gson gson;

    @NonNull
    @Override
    public String resolveOnlineName(@NonNull UUID uuid) {
        String url = HYTALE_API + uuid;
        return fetchProfile(url).map(PlayerData::username).orElse("");
    }

    @Nullable
    @Override
    public UUID resolveOnlineUUID(@NonNull String username) {
        String url = HYTALE_API + username;
        return fetchProfile(url).map(PlayerData::id).orElse(null);
    }

    private Optional<PlayerData> fetchProfile(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", WebUtil.USER_AGENT)
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(gson.fromJson(response.body(), ApiResponse.class))
                        .filter(resp -> "player.found".equals(resp.code()))
                        .map(resp -> resp.data().player());
            }
        } catch (Exception e) {
            fLogger.warning(e);
        }
        return Optional.empty();
    }

    private record ApiResponse(
            String code,
            String message,
            ApiData data,
            boolean success
    ) {}

    private record ApiData(
            PlayerData player
    ) {}

    private record PlayerData(
            UUID id,
            String username
    ) {}
}