package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.BiPredicate;

@Getter
@Setter
public abstract class AbstractModule {

    private List<BiPredicate<FEntity, Boolean>> predicates;
    private List<Class<? extends AbstractModule>> children;
    private boolean enable;

    protected AbstractModule() {
    }

    public ImmutableList.Builder<@NonNull BiPredicate<FEntity, Boolean>> predicateBuilder() {
        return ImmutableList.builder();
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

    public boolean containsChild(Class<? extends AbstractModule> clazz) {
        if (children == null) return false;

        return children.contains(clazz);
    }

    public boolean isModuleDisabledFor(FEntity entity) {
        return isModuleDisabledFor(entity, false);
    }

    public boolean isModuleDisabledFor(FEntity entity, boolean isMessage) {
        if (predicates == null) return false;

        for (BiPredicate<FEntity, Boolean> predicate : predicates) {
            if (predicate.test(entity, isMessage)) {
                return true;
            }
        }

        return false;
    }
}
