package net.flectone.pulse.platform.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitModernAttributesProvider implements BukkitAttributesProvider {

    private static final Attribute ARMOR_ATTRIBUTE = resolveAttribute("ARMOR", "GENERIC_ARMOR");
    private static final Attribute ATTACK_DAMAGE_ATTRIBUTE = resolveAttribute("ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");

    private static Attribute resolveAttribute(String modern, String legacy) {
        try {
            return Attribute.valueOf(modern);
        } catch (IllegalArgumentException _) {
            return Attribute.valueOf(legacy);
        }
    }

    @Override
    public double getArmorValue(Player player) {
        try {
            AttributeInstance instance = player.getAttribute(ARMOR_ATTRIBUTE);
            return instance != null ? round(instance.getValue()) : 0.0;
        } catch (Exception _) {
            return 0.0;
        }
    }

    @Override
    public double getAttackDamage(Player player) {
        try {
            AttributeInstance instance = player.getAttribute(ATTACK_DAMAGE_ATTRIBUTE);
            return instance != null ? round(instance.getValue()) : 1.0;
        } catch (Exception _) {
            return 1.0;
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

}
