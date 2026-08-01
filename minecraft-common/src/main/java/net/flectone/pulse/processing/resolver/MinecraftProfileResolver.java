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
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftProfileResolver extends ProfileResolver {

    private static final String MOJANG_API_WITH_NAME = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_API_WITH_UUID = "https://sessionserver.mojang.com/session/minecraft/profile/";

    private final HttpClient httpClient;
    private final FLogger fLogger;
    private final Gson gson;

    @NonNull
    @Override
    public String resolveOnlineName(@NonNull UUID uuid) {
        String url = MOJANG_API_WITH_UUID + uuid.toString().replace("-", "");
        return fetchProfile(url).map(PlayerProfile::name).orElse("");
    }

    @Nullable
    @Override
    public UUID resolveOnlineUUID(@NonNull String username) {
        String url = MOJANG_API_WITH_NAME + username;
        return fetchProfile(url).map(PlayerProfile::getUUID).orElse(null);
    }

    @NonNull
    @Override
    public UUID resolveOfflineUUID(String playerName) {
        String offlinePrefix = "OfflinePlayer:" + playerName;
        return UUID.nameUUIDFromBytes(offlinePrefix.getBytes(StandardCharsets.UTF_8));
    }

    private Optional<PlayerProfile> fetchProfile(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("User-Agent", WebUtil.USER_AGENT)
                .uri(URI.create(url))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return Optional.of(gson.fromJson(response.body(), PlayerProfile.class));
            }

        } catch (Exception e) {
            fLogger.warning(e);
        }

        return Optional.empty();
    }

    private record PlayerProfile(
            String id,
            String name
    ) {

        public UUID getUUID() {
            long mostSigBits = Long.parseUnsignedLong(id.substring(0, 16), 16);
            long leastSigBits = Long.parseUnsignedLong(id.substring(16), 16);
            return new UUID(mostSigBits, leastSigBits);
        }

    }

}
