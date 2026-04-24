package net.flectone.pulse.platform.provider;

import org.bukkit.entity.Player;

public interface BukkitAttributesProvider {

    double getArmorValue(Player player);

    double getAttackDamage(Player player);

}
