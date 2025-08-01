package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.processing.resolver.BukkitLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Singleton
public class BukkitFlectonePulse extends JavaPlugin implements FlectonePulse {

    private FLogger fLogger;
    private LibraryResolver libraryResolver;
    private Injector injector;

    @Override
    public void onLoad() {
        // initialize custom logger
        fLogger = new FLogger(this.getLogger());
        fLogger.enableFilter();
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        libraryResolver = new BukkitLibraryResolver(this, fLogger);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        // configure packetevents api
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .reEncodeByDefault(false)
                .checkForUpdates(false)
                .debug(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(new BukkitInjector(this, this, libraryResolver, fLogger));
        } catch (RuntimeException e) {
            fLogger.warning("FAILED TO ENABLE");
            fLogger.warning(e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        injector.getInstance(FlectonePulseAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (injector == null) return;

        injector.getInstance(FlectonePulseAPI.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (injector == null) return;

        injector.getInstance(FlectonePulseAPI.class).reload();
    }
}