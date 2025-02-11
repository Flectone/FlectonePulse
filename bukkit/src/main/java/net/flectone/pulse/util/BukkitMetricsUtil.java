package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.Module;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitMetricsUtil implements MetricsUtil {

    private final int PLUGIN_ID = 21076;

    private final Plugin plugin;
    private final FileManager fileManager;
    private final Module module;

    @Inject
    public BukkitMetricsUtil(Plugin plugin,
                             FileManager fileManager,
                             Module module) {
        this.plugin = plugin;
        this.fileManager = fileManager;
        this.module = module;
    }

    public void setup() {
        Config config = fileManager.getConfig();

        Metrics metrics = new Metrics(plugin, PLUGIN_ID);
        metrics.addCustomChart(new SimplePie("plugin_language", config::getLanguage));
        metrics.addCustomChart(new SimplePie("proxy_mode", () -> config.isBungeecord()
                ? "BungeeCord"
                : config.isVelocity()
                    ? "Velocity"
                    : "false"
        ));

        metrics.addCustomChart(new SimplePie("database", () -> config.getDatabase().getType().name()));
        metrics.addCustomChart(new AdvancedPie("modules", module::collectModuleStatuses));
    }
}
