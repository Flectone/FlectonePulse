package net.flectone.pulse.util;

import com.google.inject.Singleton;
import net.flectone.pulse.model.FEntity;

@Singleton
public class FabricPermissionUtil extends PermissionUtil {

    @Override
    public void register(String name, String type) {

    }

    @Override
    public boolean has(FEntity sender, String permission) {
        return true;
    }
}
