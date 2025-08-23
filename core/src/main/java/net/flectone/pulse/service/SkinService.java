package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.apache.commons.lang3.Strings;

@Singleton
public class SkinService {

    private final Integration integration;
    private final IntegrationModule integrationModule;

    @Inject
    public SkinService(FileResolver fileResolver,
                       IntegrationModule integrationModule) {
        this.integration = fileResolver.getIntegration();
        this.integrationModule = integrationModule;
    }

    public String getSkin(FEntity entity) {
        String texture = integrationModule.getTextureUrl(entity);
        return texture != null ? texture : entity.getUuid().toString();
    }

    public String getAvatarUrl(FEntity entity) {
        return Strings.CS.replace(integration.getAvatarApiUrl(), "<skin>", getSkin(entity));
    }

    public String getBodyUrl(FEntity entity) {
        return Strings.CS.replace(integration.getBodyApiUrl(), "<skin>", getSkin(entity));
    }

}
