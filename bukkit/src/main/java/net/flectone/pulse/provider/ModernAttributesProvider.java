package net.flectone.pulse.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

@Singleton
public class ModernAttributesProvider implements AttributesProvider {

    @Inject
    public ModernAttributesProvider() {

    }

    @Override
    public double getArmorValue(Player player) {
        try {
            AttributeInstance instance = player.getAttribute(Attribute.GENERIC_ARMOR);
            return instance != null ? round(instance.getValue()) : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public double getAttackDamage(Player player) {
        try {
            AttributeInstance instance = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            return instance != null ? round(instance.getValue()) : 1.0;
        } catch (Exception e) {
            return 1.0;
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

}
