package net.flectone.pulse.service;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * Resolves player skins and the avatar images used by the chat integrations.
 * @author TheFaser
 */
public interface SkinService {

    /**
     * Reads the current skin of a player and stores it, replacing the stored one.
     *
     * @param fPlayer the player
     * @param updatePlayerlistname whether to update the player's name in the tab list with the new skin
     */
    void updateProfileProperty(@NonNull FPlayer fPlayer, boolean updatePlayerlistname);

    /**
     * Reads the current skin of a player and stores it, replacing the stored one,
     * then updates the player's name in the tab list.
     *
     * @param fPlayer the player
     */
    default void updateProfileProperty(@NonNull FPlayer fPlayer) {
        updateProfileProperty(fPlayer, true);
    }

    /**
     * Drops the stored skin of a player, so it is read again on the next use.
     *
     * @param player the UUID of the player
     */
    void invalidateProfileProperty(@NonNull UUID player);

    /**
     * The skin of a player, used to build the avatar and body urls.
     *
     * @param fPlayer the entity
     * @return the texture value, or an empty string if it could not be resolved
     */
    @NonNull String getSkin(@NonNull FEntity fPlayer);

    /**
     * A url rendering the entity's head, used as the avatar on Discord and Telegram.
     *
     * @param entity the entity
     * @return the avatar url
     */
    @NonNull String getAvatarUrl(@NonNull FEntity entity);

    /**
     * A url rendering the entity's full body.
     *
     * @param entity the entity
     * @return the body url
     */
    @NonNull String getBodyUrl(@NonNull FEntity entity);

}