package net.flectone.pulse.platform.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;

@Singleton
public class BukkitModernAttributesProvider implements BukkitAttributesProvider {

    private final MethodHandle valueOfMethod;
    private final Attribute armorAttribute;
    private final Attribute attackDamageAttribute;

    @Inject
    public BukkitModernAttributesProvider(ReflectionResolver reflectionResolver) {
        this.valueOfMethod = reflectionResolver.unreflectMethod(Attribute.class, "valueOf", String.class);
        this.armorAttribute = resolveAttribute("ARMOR", "GENERIC_ARMOR");
        this.attackDamageAttribute = resolveAttribute("ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");
    }

    private Attribute resolveAttribute(String modern, String legacy) {
        try {
            return (Attribute) valueOfMethod.invoke(modern);
        } catch (Throwable _) {
        }

        try {
            return (Attribute) valueOfMethod.invoke(legacy);
        } catch (Throwable _) {
            throw new IllegalArgumentException("Cannot resolve attribute");
        }
    }

    @Override
    public double getArmorValue(Player player) {
        try {
            AttributeInstance instance = player.getAttribute(armorAttribute);
            return instance != null ? round(instance.getValue()) : 0.0;
        } catch (Exception _) {
            return 0.0;
        }
    }

    @Override
    public double getAttackDamage(Player player) {
        try {
            AttributeInstance instance = player.getAttribute(attackDamageAttribute);
            return instance != null ? round(instance.getValue()) : 1.0;
        } catch (Exception _) {
            return 1.0;
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

}
