package net.flectone.pulse.service;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class SkinService {

    private final Cache<UUID, PlayerHeadObjectContents.ProfileProperty> profilePropertyCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    private final Integration integration;
    private final IntegrationModule integrationModule;
    private final PacketProvider packetProvider;
    private final FLogger fLogger;

    @Inject
    public SkinService(FileResolver fileResolver,
                       IntegrationModule integrationModule,
                       PacketProvider packetProvider,
                       FLogger fLogger) {
        this.integration = fileResolver.getIntegration();
        this.integrationModule = integrationModule;
        this.packetProvider = packetProvider;
        this.fLogger = fLogger;
    }

    public void updateProfilePropertyCache(UUID uuid, PlayerHeadObjectContents.ProfileProperty profileProperty) {
        profilePropertyCache.put(uuid, profileProperty);
    }

    public PlayerHeadObjectContents.ProfileProperty getProfilePropertyFromCache(FEntity entity) {
        try {
            return profilePropertyCache.get(entity.getUuid(), () -> getProfileProperty(entity));
        } catch (ExecutionException e) {
            fLogger.warning(e);
            return getProfileProperty(entity);
        }
    }

    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity entity) {
        PlayerHeadObjectContents.ProfileProperty profileProperty = integrationModule.getProfileProperty(entity);
        if (profileProperty != null) return profileProperty;

        User user = packetProvider.getUser(entity.getUuid());
        if (user == null) return null;

        List<TextureProperty> textureProperties = user.getProfile().getTextureProperties();
        if (textureProperties.isEmpty()) return null;

        TextureProperty textureProperty = textureProperties.getFirst();

        return PlayerHeadObjectContents.property(
                "textures",
                textureProperty.getValue(),
                textureProperty.getSignature()
        );
    }

    public String getAvatarUrl(FEntity entity) {
        return Strings.CS.replace(integration.getAvatarApiUrl(), "<skin>", getSkin(entity));
    }

    public String getBodyUrl(FEntity entity) {
        return Strings.CS.replace(integration.getBodyApiUrl(), "<skin>", getSkin(entity));
    }

    public String getSkin(FEntity entity) {
        String texture = integrationModule.getTextureUrl(entity);
        return texture != null ? texture : entity.getUuid().toString();
    }

}
