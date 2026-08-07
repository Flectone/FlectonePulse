package net.flectone.pulse.module;

import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.function.BiPredicate;

/**
 * The smallest form of a module, a named feature that can be switched on and off and that
 * owns a permission. Modules form a tree, and disabling one disables everything under it.
 * @author TheFaser
 */
public interface ModuleSimple {

    /**
     * Identifies this module in the config, in permissions and on the proxy channel.
     *
     * @return the module name
     */
    ModuleName name();

    /**
     * The config section this module reads its settings from.
     *
     * @return the settings
     */
    EnableSetting config();

    /**
     * The permission that gates this module.
     *
     * @return the permission
     */
    PermissionSetting permission();

    /**
     * Called once the module is switched on. Register listeners and start tasks here.
     */
    default void onEnable() {
    }

    /**
     * Called once the module is switched off. Undo whatever {@link #onEnable()} set up.
     */
    default void onDisable() {
    }

    /**
     * An extra condition that turns the module off for one entity, on top of the config and the
     * permission. It is used for things like per-world or per-player toggles.
     *
     * @return the condition; the boolean argument tells it whether the permission was already checked
     */
    default BiPredicate<FEntity, Boolean> isDisable() {
        return (_, _) -> false;
    }

    /**
     * The modules nested under this one. They are switched on and off along with it.
     *
     * @return the child module types, empty by default
     */
    default Set<@NonNull Class<? extends ModuleSimple>> children() {
        return Set.of();
    }

    /**
     * Every permission this module registers, which is its own by default.
     *
     * @return the permissions
     */
    default Set<@NonNull PermissionSetting> permissions() {
        return Set.of(permission());
    }

}
