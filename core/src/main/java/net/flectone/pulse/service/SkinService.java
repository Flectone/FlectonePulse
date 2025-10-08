package net.flectone.pulse.service;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.apache.commons.lang3.Strings;

import java.util.List;

@Singleton
public class SkinService {

    private final Integration integration;
    private final IntegrationModule integrationModule;
    private final PacketProvider packetProvider;

    @Inject
    public SkinService(FileResolver fileResolver,
                       IntegrationModule integrationModule,
                       PacketProvider packetProvider) {
        this.integration = fileResolver.getIntegration();
        this.integrationModule = integrationModule;
        this.packetProvider = packetProvider;
    }

    public String getSkin(FEntity entity) {
        String texture = integrationModule.getTextureUrl(entity);
        return texture != null ? texture : entity.getUuid().toString();
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

}
