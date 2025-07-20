package net.flectone.pulse.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Singleton
public class LegacyAttributesProvider implements AttributesProvider {

    @Inject
    public LegacyAttributesProvider() {
    }

    @Override
    public double getArmorValue(Player player) {
        double armorPoints = 0;
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        for (ItemStack item : armorContents) {
            if (item == null) continue;

            String type = item.getType().name();
            if (type.endsWith("_HELMET")) armorPoints += 1;
            else if (type.endsWith("_CHESTPLATE")) armorPoints += 3;
            else if (type.endsWith("_LEGGINGS")) armorPoints += 2;
            else if (type.endsWith("_BOOTS")) armorPoints += 1;
        }

        return armorPoints;
    }

    @Override
    public double getAttackDamage(Player player) {
        ItemStack hand = player.getInventory().getItemInHand();
        if (hand == null) return 1.0;

        String type = hand.getType().name();
        if (type.contains("DIAMOND_SWORD")) return 7.0;
        if (type.contains("IRON_SWORD")) return 6.0;
        if (type.contains("STONE_SWORD")) return 5.0;
        if (type.contains("WOOD_SWORD")) return 4.0;

        return 1.0;
    }
}
