package net.flectone.pulse.module;

import com.google.common.collect.ImmutableSet;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

import java.util.function.BiPredicate;

public interface ModuleSimple {

    ModuleName name();

    EnableSetting config();

    PermissionSetting permission();

    default void onEnable() {
    }

    default void onDisable() {
    }

    default BiPredicate<FEntity, Boolean> disablePredicate() {
        return (_, _) -> false;
    }

    default ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ImmutableSet.builder();
    }

    default ImmutableSet.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ImmutableSet.<PermissionSetting>builder().add(permission());
    }

}
