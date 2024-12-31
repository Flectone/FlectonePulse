package net.flectone.pulse.platform.bukkit;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.logging.adapters.JDKLogAdapter;
import com.google.inject.Singleton;
import net.flectone.pulse.BuildConfig;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.platform.BukkitBaseDependency;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitDependency extends BukkitBaseDependency {

    public BukkitDependency(Plugin plugin, FLogger fLogger) {
        super(new BukkitLibraryManager(plugin, "libraries", new JDKLogAdapter(fLogger)));
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();

        addDependency("net.kyori", "adventure-platform-bukkit", BuildConfig.ADVENTURE_PLATFORM_BUKKIT_VERSION);
        addDependency("net.kyori", "adventure-text-minimessage", BuildConfig.ADVENTURE_API);
        addDependency("net.kyori", "adventure-text-serializer-plain", BuildConfig.ADVENTURE_API);
        addDependency("net.kyori", "adventure-text-serializer-ansi", BuildConfig.ADVENTURE_API);
        addDependency("net.kyori", "adventure-text-serializer-legacy", BuildConfig.ADVENTURE_API);
        addDependency("net.kyori", "adventure-text-serializer-gson", BuildConfig.ADVENTURE_API);
        addDependency("net.kyori", "adventure-nbt", BuildConfig.ADVENTURE_API);
    }
}
