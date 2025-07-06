package net.flectone.pulse.service;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.database.dto.MetricsDTO;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.MetricsSender;

import java.time.Instant;

@Singleton
public class MetricsService {

    private final TaskScheduler taskScheduler;
    private final MetricsSender metricsSender;
    private final PlatformServerAdapter platformServerAdapter;
    private final FileResolver fileResolver;
    private final Module module;

    @Inject
    public MetricsService(TaskScheduler taskScheduler,
                          MetricsSender metricsSender,
                          PlatformServerAdapter platformServerAdapter,
                          FileResolver fileResolver,
                          Module module) {
        this.taskScheduler = taskScheduler;
        this.metricsSender = metricsSender;
        this.platformServerAdapter = platformServerAdapter;
        this.fileResolver = fileResolver;
        this.module = module;
    }

    public void reload() {
        taskScheduler.runAsyncTimer(this::send, 0L, 20L * 60 * 60);
    }

    public void send() {
        MetricsDTO metricsDTO = new MetricsDTO();
        metricsDTO.setServerCore(platformServerAdapter.getServerCore());

        metricsDTO.setServerVersion(PacketEvents.getAPI().getServerManager().getVersion().getReleaseName());
        metricsDTO.setOsName(getOsName());
        metricsDTO.setOsArchitecture(getOsArch());
        metricsDTO.setOsVersion(getOsVersion());
        metricsDTO.setJavaVersion(getJavaVersion());
        metricsDTO.setCpuCores(Runtime.getRuntime().availableProcessors());
        metricsDTO.setTotalRAM(Runtime.getRuntime().totalMemory());

        Config config = fileResolver.getConfig();

        metricsDTO.setProjectVersion(config.getVersion());
        metricsDTO.setProjectLanguage(config.getLanguage());
        metricsDTO.setOnlineMode(platformServerAdapter.isOnlineMode() ? "True" : "False");
        metricsDTO.setProxyMode(config.isBungeecord() ? "BungeeCord" : config.isVelocity() ? "Velocity" : "None");
        metricsDTO.setDatabaseMode(config.getDatabase().getType().name());
        metricsDTO.setPlayerCount(platformServerAdapter.getOnlinePlayerCount());
        metricsDTO.setModules(module.collectModuleStatuses());
        metricsDTO.setCreatedAt(Instant.now().toString());

        metricsSender.sendMetrics(metricsDTO);
    }

    private String getOsName() {
        return System.getProperty("os.name");
    }

    private String getOsArch() {
        return System.getProperty("os.arch");
    }

    private String getOsVersion() {
        return System.getProperty("os.version");
    }

    private String getJavaVersion() {
        return System.getProperty("java.version");
    }

}
