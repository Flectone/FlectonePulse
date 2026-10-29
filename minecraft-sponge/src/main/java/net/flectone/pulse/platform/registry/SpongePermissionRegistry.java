package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SpongePermissionRegistry implements PermissionRegistry {

    private final Map<String, Permission.Type> permissions = new HashMap<>();

    @Override
    public void register(String name, Permission.Type type) {
        if (name == null || name.isEmpty()) return;
        if (type == null) return;

        permissions.put(name, type);
    }

    @Override
    public Permission.@Nullable Type getType(@NonNull String name) {
        return permissions.get(name);
    }

    @Override
    public void onDisable() {
        permissions.clear();
    }
}
