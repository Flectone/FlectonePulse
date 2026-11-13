package net.flectone.pulse.module.message.status.icon;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import org.jspecify.annotations.Nullable;

/**
 * Replaces the icon shown in the server list.
 * @author TheFaser
 */
public interface IconModule extends ModuleSimple {

    @Override
    Message.Status.Icon config();

    @Override
    Permission.Message.Status.Icon permission();

    /**
     * The next icon to show to a player.
     *
     * @param fPlayer the player
     * @return the icon, or null if there are none
     */
    @Nullable
    String next(FPlayer fPlayer);

}
