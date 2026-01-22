package net.flectone.pulse.platform.provider;

import org.bukkit.entity.Player;

public interface AttributesProvider {

    double getArmorValue(Player player);

    double getAttackDamage(Player player);

}
