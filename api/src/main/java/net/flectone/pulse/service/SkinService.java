package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FEntity;

/**
 * Resolves player skins and the avatar images used by the chat integrations.
 * @author TheFaser
 */
public interface SkinService {

    /**
     * The encoded skin texture of an entity.
     *
     * @param fPlayer the entity
     * @return the texture value, or an empty string if it could not be resolved
     */
    String getSkin(FEntity fPlayer);

    /**
     * A url rendering the entity's head, used as the avatar on Discord and Telegram.
     *
     * @param entity the entity
     * @return the avatar url
     */
    String getAvatarUrl(FEntity entity);

    /**
     * A url rendering the entity's full body.
     *
     * @param entity the entity
     * @return the body url
     */
    String getBodyUrl(FEntity entity);

}
