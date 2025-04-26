package net.flectone.pulse.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class FabricPermissionRegistry implements PermissionRegistry {

    @Getter
    private final Map<String, Integer> permissions = new HashMap<>();

    @Inject
    public FabricPermissionRegistry() {
    }

    @Override
    public void register(String name, net.flectone.pulse.configuration.Permission.Type type) {
        permissions.put(name, type.ordinal());
    }

    @Override
    public void reload() {
        permissions.clear();
    }

}
