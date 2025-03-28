package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.IntegrationModule;

@Singleton
public class SkinService {

    private final String AVATAR_URL_TEMPLATE = "https://mc-heads.net/avatar/<skin>/8.png";
    private final String BODY_URL_TEMPLATE = "https://mc-heads.net/player/<skin>/16";

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
        return AVATAR_URL_TEMPLATE.replace("<skin>", getSkin(entity));
    }

    public String getBodyUrl(FEntity entity) {
        return BODY_URL_TEMPLATE.replace("<skin>", getSkin(entity));
    }

}
