package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.dto.MetricsDTO;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.MetricsSender;
import net.flectone.pulse.processing.resolver.FileResolver;

import java.time.Instant;

@Singleton
public class MetricsService {

    private final TaskScheduler taskScheduler;
    private final MetricsSender metricsSender;
    private final PlatformServerAdapter platformServerAdapter;
    private final PacketProvider packetProvider;
    private final FileResolver fileResolver;
    private final Module module;

    @Inject
    public MetricsService(TaskScheduler taskScheduler,
                          MetricsSender metricsSender,
                          PlatformServerAdapter platformServerAdapter,
                          PacketProvider packetProvider,
                          FileResolver fileResolver,
                          Module module) {
        this.taskScheduler = taskScheduler;
        this.metricsSender = metricsSender;
        this.platformServerAdapter = platformServerAdapter;
        this.packetProvider = packetProvider;
        this.fileResolver = fileResolver;
        this.module = module;
    }

    public void reload() {
        taskScheduler.runAsyncTimer(this::send, 0L, 20L * 60 * 60);
    }

    public void send() {
        MetricsDTO metricsDTO = new MetricsDTO();
        metricsDTO.setServerCore(platformServerAdapter.getServerCore());

        metricsDTO.setServerVersion(packetProvider.getServerVersion().getReleaseName());
        metricsDTO.setOsName(getOsName());
        metricsDTO.setOsArchitecture(getOsArch());
        metricsDTO.setOsVersion(getOsVersion());
        metricsDTO.setJavaVersion(getJavaVersion());
        metricsDTO.setCpuCores(Runtime.getRuntime().availableProcessors());
        metricsDTO.setTotalRAM(Runtime.getRuntime().maxMemory());

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
