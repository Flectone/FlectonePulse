package net.flectone.pulse.module.command.maintenance;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The /maintenance command, which closes the server to everyone but the allowed players.
 * @author TheFaser
 */
public interface MaintenanceModule extends ModuleCommand {

    @Override
    Command.Maintenance config();

    @Override
    Permission.Command.Maintenance permission();

    @Override
    Localization.Command.Maintenance localization(FPlayer fPlayer);

    /**
     * Whether a player may join while maintenance is on.
     *
     * @param fPlayer the connecting player
     * @return true if they are allowed in
     */
    boolean isAllowed(FPlayer fPlayer);

    /**
     * Whether maintenance is currently on.
     *
     * @return true if the server is closed
     */
    boolean isTurnedOn();

    /**
     * Switches maintenance on or off.
     *
     * @param fPlayer the moderator
     * @param reason the stated reason, may be null
     * @param time when it ends in milliseconds, or -1 for indefinite
     * @param turned whether to switch it on
     * @return the stored entry, or empty when switching off
     */
    Optional<Moderation> turn(FPlayer fPlayer, @Nullable String reason, long time, boolean turned);

    /**
     * Disconnects everyone who is not allowed to stay.
     *
     * @param maintenance the maintenance entry
     */
    void kickOnlinePlayers(@NonNull Moderation maintenance);

}
