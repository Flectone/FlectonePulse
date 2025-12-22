package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPermissionRegistry implements PermissionRegistry {

    @Getter
    private final Map<String, Integer> permissions = new HashMap<>();

    @Override
    public void register(String name, Permission.Type type) {
        if (StringUtils.isEmpty(name)) return;
        if (type == null) return;

        permissions.put(name, type.ordinal());
    }

    @Override
    public void reload() {
        permissions.clear();
    }

}
