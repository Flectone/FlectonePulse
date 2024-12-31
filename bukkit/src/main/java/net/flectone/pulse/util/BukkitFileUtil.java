package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitFileUtil extends FileUtil {

    private final Plugin plugin;

    @Inject
    public BukkitFileUtil(Plugin plugin, FLogger fLogger) {
        super(fLogger);

        this.plugin = plugin;
    }

    public void saveResource(String path) {
        plugin.saveResource(path, false);
    }

}
