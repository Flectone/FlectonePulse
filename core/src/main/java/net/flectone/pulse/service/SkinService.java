package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FEntity;

public interface SkinService {

    String getSkin(FEntity fPlayer);

    String getAvatarUrl(FEntity entity);

    String getBodyUrl(FEntity entity);

}
