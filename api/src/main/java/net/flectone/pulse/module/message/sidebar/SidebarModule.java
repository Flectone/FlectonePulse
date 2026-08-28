package net.flectone.pulse.module.message.sidebar;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleListLocalization;

import java.util.UUID;

/**
 * Draws the rotating sidebar lines.
 * @author TheFaser
 */
public interface SidebarModule extends ModuleListLocalization {

    @Override
    Message.Sidebar config();

    @Override
    Permission.Message.Sidebar permission();

    @Override
    Localization.Message.Sidebar localization(FPlayer fPlayer);

    /**
     * Takes the sidebar away from a player.
     *
     * @param fPlayer the player
     */
    void remove(FPlayer fPlayer);

    /**
     * Advances the sidebar to its next set of lines.
     *
     * @param fPlayer the player
     */
    void update(FPlayer fPlayer);

    /**
     * Shows the sidebar to a player, looked up by id.
     *
     * @param uuid the player
     */
    void create(UUID uuid);

    /**
     * Shows the sidebar to a player.
     *
     * @param fPlayer the player
     */
    void create(FPlayer fPlayer);

}
