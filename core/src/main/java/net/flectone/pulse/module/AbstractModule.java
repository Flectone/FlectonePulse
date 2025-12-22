package net.flectone.pulse.module;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.flectone.pulse.config.setting.EnableSetting;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
public abstract class AbstractModule {

    private final Set<Class<? extends AbstractModule>> children = new LinkedHashSet<>();
    private final List<BiPredicate<FEntity, Boolean>> predicates = new ArrayList<>();

    @Setter
    private boolean enable;

    protected AbstractModule() {
    }

    public ImmutableList.Builder<@NonNull PermissionSetting> permissionBuilder() {
        return ImmutableList.<PermissionSetting>builder().add(permission());
    }

    public void onEnable() {}

    public void onDisable() {}

    public void configureChildren() {}

    public abstract EnableSetting config();

    public abstract PermissionSetting permission();

    public void addChild(Class<? extends AbstractModule> clazz) {
        children.add(clazz);
    }

    public void addPredicate(Predicate<FEntity> predicate) {
        predicates.add((fEntity, value) -> predicate.test(fEntity));
    }

    public void addPredicate(BiPredicate<FEntity, Boolean> biPredicate) {
        predicates.add(biPredicate);
    }

    public boolean containsChild(Class<? extends AbstractModule> clazz) {
        return getChildren().contains(clazz);
    }

    public boolean isModuleDisabledFor(FEntity entity) {
        return isModuleDisabledFor(entity, false);
    }

    public boolean isModuleDisabledFor(FEntity entity, boolean isMessage) {
        for (BiPredicate<FEntity, Boolean> predicate : predicates) {
            if (predicate.test(entity, isMessage)) {
                return true;
            }
        }

        return false;
    }
}
