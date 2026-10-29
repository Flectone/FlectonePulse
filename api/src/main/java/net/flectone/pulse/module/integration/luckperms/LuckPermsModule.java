package net.flectone.pulse.module.integration.luckperms;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Reads prefixes, suffixes and group weights from LuckPerms.
 * @author TheFaser
 */
public interface LuckPermsModule extends ModuleSimple {

    @Override
    Integration.Luckperms config();

    @Override
    Permission.Integration.Luckperms permission();

    /**
     * The value LuckPerms sets for a node of a player.
     *
     * @param fPlayer the player
     * @param permission the node
     * @return true if it is granted, false if it is denied, or null if it is not set
     */
    @Nullable Boolean getLuckPermission(FPlayer fPlayer, String permission);

    /**
     * Whether nodes granted by default stay granted even when LuckPerms denies them, for example with {@code * false}.
     *
     * @return true if default nodes are always granted
     */
    boolean isAlwaysHaveTrue();

    /**
     * The weight of a player's highest group.
     *
     * @param fPlayer the player
     * @return the weight
     */
    int getGroupWeight(FPlayer fPlayer);

    /**
     * The weight a player is sorted by in the tab list.
     *
     * @param fPlayer the player
     * @return the weight
     */
    int getSortWeight(FPlayer fPlayer);

    /**
     * The prefix LuckPerms gives a player.
     *
     * @param fPlayer the player
     * @return the prefix, or an empty string if none is set
     */
    String getPrefix(FPlayer fPlayer);

    /**
     * The suffix LuckPerms gives a player.
     *
     * @param fPlayer the player
     * @return the suffix, or an empty string if none is set
     */
    String getSuffix(FPlayer fPlayer);

    /**
     * Every group defined in LuckPerms.
     *
     * @return the group names
     */
    Set<String> getGroups();

}
