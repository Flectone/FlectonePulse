package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FEntity;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleSkinService implements SkinService {

    @Override
    public String getSkin(FEntity fPlayer) {
        return "";
    }

    @Override
    public String getAvatarUrl(FEntity entity) {
        return "";
    }

    @Override
    public String getBodyUrl(FEntity entity) {
        return "";
    }

}
