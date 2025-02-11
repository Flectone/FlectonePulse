package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.metadata.MetadataValue;

@Singleton
public class SuperVanishModule extends AbstractModule {

    private final Integration.Supervanish config;
    private final Permission.Integration.Supervanish permission;

    private final SuperVanishIntegration superVanishIntegration;
    private final BukkitListenerRegistry bukkitListenerManager;

    @Inject
    public SuperVanishModule(FileManager fileManager,
                             SuperVanishIntegration superVanishIntegration,
                             BukkitListenerRegistry bukkitListenerManager) {
        this.superVanishIntegration = superVanishIntegration;
        this.bukkitListenerManager = bukkitListenerManager;

        config = fileManager.getIntegration().getSupervanish();
        permission = fileManager.getPermission().getIntegration().getSupervanish();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        bukkitListenerManager.register(SuperVanishIntegration.class, EventPriority.NORMAL);

        superVanishIntegration.hook();
    }

    @Override
    public boolean isConfigEnable() {
        return config.isEnable();
    }

    public boolean isVanished(FEntity sender) {
        if (checkModulePredicates(sender)) return false;

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player == null) return false;

        return player
                .getMetadata("vanished").stream()
                .anyMatch(MetadataValue::asBoolean);
    }
}
