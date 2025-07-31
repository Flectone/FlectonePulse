package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FileResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

@Singleton
public class SuperVanishModule extends AbstractModule {

    private final Integration.Supervanish config;
    private final Permission.Integration.Supervanish permission;
    private final SuperVanishIntegration superVanishIntegration;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public SuperVanishModule(FileResolver fileResolver,
                             SuperVanishIntegration superVanishIntegration,
                             ListenerRegistry listenerRegistry) {
        this.config = fileResolver.getIntegration().getSupervanish();
        this.permission = fileResolver.getPermission().getIntegration().getSupervanish();
        this.superVanishIntegration = superVanishIntegration;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(SuperVanishIntegration.class);

        superVanishIntegration.hook();
    }

    @Override
    public void onDisable() {
        superVanishIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
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
