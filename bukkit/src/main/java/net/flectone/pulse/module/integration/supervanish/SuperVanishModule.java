package net.flectone.pulse.module.integration.supervanish;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SuperVanishModule extends AbstractModule {

    private final FileResolver fileResolver;
    private final SuperVanishIntegration superVanishIntegration;
    private final ListenerRegistry listenerRegistry;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(SuperVanishIntegration.class);

        superVanishIntegration.hook();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        superVanishIntegration.unhook();
    }

    @Override
    public Integration.Supervanish config() {
        return fileResolver.getIntegration().getSupervanish();
    }

    @Override
    public Permission.Integration.Supervanish permission() {
        return fileResolver.getPermission().getIntegration().getSupervanish();
    }

    public boolean isVanished(FEntity sender) {
        if (isModuleDisabledFor(sender)) return false;

        Player player = Bukkit.getPlayer(sender.getUuid());
        if (player != null) {
            return player
                    .getMetadata("vanished").stream()
                    .anyMatch(MetadataValue::asBoolean);
        }

        // offline check
        return superVanishIntegration.isVanished(sender);
    }
}
