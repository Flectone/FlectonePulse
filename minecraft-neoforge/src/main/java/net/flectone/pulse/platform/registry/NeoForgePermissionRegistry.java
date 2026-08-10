package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePermissionRegistry implements PermissionRegistry {

    @Getter
    private final Map<String, Permission.Type> permissions = new HashMap<>();

    @Override
    public void register(String name, Permission.Type type) {
        if (name == null || name.isEmpty()) return;
        if (type == null) return;

        permissions.put(name, type);
    }

    @Override
    public void onDisable() {
        permissions.clear();
    }
}
