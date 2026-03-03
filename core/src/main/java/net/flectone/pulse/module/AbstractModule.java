package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import org.jspecify.annotations.NonNull;

import java.util.function.BiPredicate;

public abstract class AbstractModule {

    protected AbstractModule() {
    }

    public BiPredicate<FEntity, Boolean> disablePredicate() {
        return (fEntity, aBoolean) -> false;
    }

    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return ImmutableList.builder();
    }

    public ImmutableList.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ImmutableList.<PermissionSetting>builder().add(permission());
    }

    public void onEnable() {}

    public void onDisable() {}

    public abstract EnableSetting config();

    public abstract PermissionSetting permission();

}
