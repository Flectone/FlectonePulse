package net.flectone.pulse.service;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SkinService {

    private final @Named("profileProperty") Cache<UUID, PlayerHeadObjectContents.ProfileProperty> profilePropertyCache;
    private final FileFacade fileFacade;
    private final IntegrationModule integrationModule;
    private final PacketProvider packetProvider;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    public void updateProfilePropertyCache(UUID uuid, PlayerHeadObjectContents.ProfileProperty profileProperty) {
        profilePropertyCache.put(uuid, profileProperty);
    }

    @NotNull
    public PlayerHeadObjectContents.ProfileProperty getProfilePropertyFromCache(FEntity entity) {
        PlayerHeadObjectContents.ProfileProperty profileProperty = profilePropertyCache.getIfPresent(entity.getUuid());
        if (profileProperty != null) return profileProperty;

        profileProperty = getProfileProperty(entity);

        // not save profileProperty for offline player
        if (entity instanceof FPlayer fPlayer && !platformPlayerAdapter.isOnline(fPlayer) && profileProperty.signature() == null) {
            return profileProperty;
        }

        profilePropertyCache.put(entity.getUuid(), profileProperty);
        return profileProperty;
    }

    @NotNull
    public PlayerHeadObjectContents.ProfileProperty getProfileProperty(FEntity entity) {
        // get SkinsRestorer and other integration textures
        PlayerHeadObjectContents.ProfileProperty profileProperty = integrationModule.getProfileProperty(entity);
        if (profileProperty != null) return profileProperty;

        // get Platform Player textures
        profileProperty = platformPlayerAdapter.getTexture(entity.getUuid());
        if (profileProperty != null) {
            return profileProperty;
        }

        // get PacketEvents user textures
        User user = packetProvider.getUser(entity.getUuid());
        if (user != null) {
            List<TextureProperty> textureProperties = user.getProfile().getTextureProperties();
            if (!textureProperties.isEmpty()) {
                TextureProperty textureProperty = textureProperties.getFirst();
                return PlayerHeadObjectContents.property(
                        "textures",
                        textureProperty.getValue(),
                        textureProperty.getSignature()
                );
            }
        }

        // empty textures
        return PlayerHeadObjectContents.property(entity.getName(), "");
    }

    public String getAvatarUrl(FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().avatarApiUrl(), "<skin>", getSkin(entity));
    }

    public String getBodyUrl(FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().bodyApiUrl(), "<skin>", getSkin(entity));
    }

    public String getSkin(FEntity entity) {
        String texture = integrationModule.getTextureUrl(entity);
        return texture != null ? texture : entity.getUuid().toString();
    }

}
