package net.flectone.pulse.module.integration.luckperms;

import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;

import java.util.Set;

/**
 * Reads prefixes, suffixes and group weights from LuckPerms.
 * @author TheFaser
 */
public interface LuckPermsModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Integration.Luckperms config();

    @Override
    Permission.Integration.Luckperms permission();

    /**
     * Whether LuckPerms grants a node to a player.
     *
     * @param fPlayer the player
     * @param permission the node
     * @return true if it is granted
     */
    boolean hasLuckPermission(FPlayer fPlayer, String permission);

    /**
     * Whether LuckPerms grants every node, which means its answers carry no information.
     *
     * @return true if every node is granted
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
