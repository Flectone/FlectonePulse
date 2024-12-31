package net.flectone.pulse.platform.paper;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.logging.adapters.JDKLogAdapter;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.platform.BukkitBaseDependency;
import org.bukkit.plugin.Plugin;

@Singleton
public class PaperDependency extends BukkitBaseDependency {

    public PaperDependency(Plugin plugin, FLogger fLogger) {
        super(new BukkitLibraryManager(plugin, "libraries", new JDKLogAdapter(fLogger)));
    }

}