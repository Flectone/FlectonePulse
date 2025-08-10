package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.IntegrationModule;
import org.apache.commons.lang3.Strings;

@Singleton
public class SkinService {

    private final String avatarApiUrl = "https://mc-heads.net/avatar/<skin>/8.png";
    private final String bodyApiUrl = "https://mc-heads.net/player/<skin>/16";

    private final IntegrationModule integrationModule;

    @Inject
    public SkinService(IntegrationModule integrationModule) {
        this.integrationModule = integrationModule;
    }

    public String getSkin(FEntity entity) {
        String texture = integrationModule.getTextureUrl(entity);
        return texture != null ? texture : entity.getUuid().toString();
    }

    public String getAvatarUrl(FEntity entity) {
        return Strings.CS.replace(avatarApiUrl, "<skin>", getSkin(entity));
    }

    public String getBodyUrl(FEntity entity) {
        return Strings.CS.replace(bodyApiUrl, "<skin>", getSkin(entity));
    }

}
