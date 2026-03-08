package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
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
        return (fEntity, aBoolean) -> false;
    }

    default ImmutableList.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return ImmutableList.builder();
    }

    default ImmutableList.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ImmutableList.<PermissionSetting>builder().add(permission());
    }

}
