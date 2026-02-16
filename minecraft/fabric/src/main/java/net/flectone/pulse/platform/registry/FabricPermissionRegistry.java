package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Permission;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricPermissionRegistry implements PermissionRegistry {

    @Getter
    private final Map<String, Permission.Type> permissions = new Object2ObjectOpenHashMap<>();

    @Override
    public void register(String name, Permission.Type type) {
        if (StringUtils.isEmpty(name)) return;
        if (type == null) return;

        permissions.put(name, type);
    }

    @Override
    public void reload() {
        permissions.clear();
    }

}
