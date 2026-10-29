package net.flectone.pulse.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.MinecraftIntegrationModule;
import net.flectone.pulse.module.message.tab.playerlist.MinecraftPlayerlistnameModule;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.sender.ProxySender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.LazyInstance;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MinecraftSkinService implements SkinService {

    private final @Named("profileProperty") Cache<UUID, PlayerHeadObjectContents.ProfileProperty> profilePropertyCache;
    private final FileFacade fileFacade;
    private final MinecraftIntegrationModule integrationModule;
    private final MinecraftPacketProvider packetProvider;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final TaskScheduler taskScheduler;
    private final LazyInstance<MinecraftPlayerlistnameModule> playerlistnameModule;
    private final ProxySender proxySender;
    private final Gson gson;

    @Override
    public void updateProfileProperty(@NonNull FPlayer fPlayer, boolean updatePlayerlistname, boolean sendToProxy) {
        updateProfileProperty(fPlayer, getProfileProperty(fPlayer), updatePlayerlistname, sendToProxy);
    }

    public void updateProfileProperty(@NonNull FPlayer fPlayer, PlayerHeadObjectContents.@NonNull ProfileProperty profileProperty, boolean updatePlayerlistname, boolean sendToProxy) {
        if (profileProperty.value().isEmpty() && !platformPlayerAdapter.isOnline(fPlayer)) {
            profilePropertyCache.invalidate(fPlayer.uuid());
        } else {
            profilePropertyCache.put(fPlayer.uuid(), profileProperty);
        }

        if (updatePlayerlistname) {
            MinecraftPlayerlistnameModule minecraftPlayerlistnameModuleInstance = playerlistnameModule.get();

            if (platformPlayerAdapter.isOnline(fPlayer)) {
                taskScheduler.runAsyncLater(() -> minecraftPlayerlistnameModuleInstance.update(fPlayer), 2L);
            } else {
                minecraftPlayerlistnameModuleInstance.update();
            }
        }

        if (sendToProxy) {
            proxySender.send(fPlayer, ModuleName.UPDATE_CACHE_SKINPROFILE, dataOutputStream -> {
                dataOutputStream.writeUTF(profileProperty.name());
                dataOutputStream.writeUTF(profileProperty.value());
                dataOutputStream.writeUTF(StringUtils.defaultString(profileProperty.signature()));
            });
        }
    }

    @Override
    public void invalidateProfileProperty(@NonNull UUID player) {
        profilePropertyCache.invalidate(player);
    }

    @Override
    public @NonNull String getAvatarUrl(@NonNull FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().avatarApiUrl(), "<skin>", getSkin(entity));
    }

    @Override
    public @NonNull String getBodyUrl(@NonNull FEntity entity) {
        return Strings.CS.replace(fileFacade.integration().bodyApiUrl(), "<skin>", getSkin(entity));
    }

    @Override
    public @NonNull String getSkin(@NonNull FEntity entity) {
        String texture = integrationModule.getTextureUrl(entity);
        return texture != null ? texture : entity.uuid().toString();
    }

    public PlayerHeadObjectContents.@NonNull ProfileProperty getProfilePropertyFromCache(FEntity entity) {
        PlayerHeadObjectContents.ProfileProperty profileProperty = profilePropertyCache.getIfPresent(entity.uuid());
        if (profileProperty != null) return profileProperty;

        profileProperty = getProfileProperty(entity);

        // not save profileProperty for offline player without textures
        if (entity instanceof FPlayer fPlayer && !platformPlayerAdapter.isOnline(fPlayer) && profileProperty.value().isEmpty()) {
            return profileProperty;
        }

        profilePropertyCache.put(entity.uuid(), profileProperty);
        return profileProperty;
    }

    public PlayerHeadObjectContents.@NonNull ProfileProperty getProfileProperty(FEntity entity) {
        // get Platform Player textures
        PlayerHeadObjectContents.ProfileProperty profileProperty = platformPlayerAdapter.getTexture(entity.uuid());
        if (profileProperty != null) return profileProperty;

        // get SkinsRestorer and other integration textures
        profileProperty = integrationModule.getProfileProperty(entity);
        if (profileProperty != null) return profileProperty;

        // get PacketEvents user textures
        User user = packetProvider.getUser(entity.uuid());
        if (user != null) {
            List<TextureProperty> textureProperties = user.getProfile().getTextureProperties();
            if (!textureProperties.isEmpty()) {
                TextureProperty textureProperty = textureProperties.getFirst();
                return PlayerHeadObjectContents.property(
                        "textures",
                        textureProperty.getValue()
                );
            }
        }

        // empty textures
        return PlayerHeadObjectContents.property(entity.name(), "");
    }

}