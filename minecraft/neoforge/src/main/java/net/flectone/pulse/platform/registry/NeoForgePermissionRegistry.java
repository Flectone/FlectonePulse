package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;

import java.util.Map;

/**
 * NeoForge-specific permission registry that stores and manages permissions.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgePermissionRegistry implements PermissionRegistry {

    @Getter
    private final Map<String, Permission.Type> permissions = new Object2ObjectOpenHashMap<>();

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
