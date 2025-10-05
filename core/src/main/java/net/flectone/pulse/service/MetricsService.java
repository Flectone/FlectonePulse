package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.dto.MetricsDTO;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
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
    private final ModuleController moduleController;

    @Inject
    public MetricsService(TaskScheduler taskScheduler,
                          MetricsSender metricsSender,
                          PlatformServerAdapter platformServerAdapter,
                          PacketProvider packetProvider,
                          FileResolver fileResolver,
                          ModuleController moduleController) {
        this.taskScheduler = taskScheduler;
        this.metricsSender = metricsSender;
        this.platformServerAdapter = platformServerAdapter;
        this.packetProvider = packetProvider;
        this.fileResolver = fileResolver;
        this.moduleController = moduleController;
    }

    public void reload() {
        taskScheduler.runAsyncTimer(this::send, 0L, 20L * 60 * 60);
    }

    public void send() {
        MetricsDTO metricsDTO = new MetricsDTO();

        metricsDTO.setServerUUID(platformServerAdapter.getServerUUID());
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
        metricsDTO.setProjectLanguage(config.getLanguage().getType());
        metricsDTO.setOnlineMode(platformServerAdapter.isOnlineMode() ? "True" : "False");
        metricsDTO.setProxyMode(getProxyMode());
        metricsDTO.setDatabaseMode(config.getDatabase().getType().name());
        metricsDTO.setPlayerCount(platformServerAdapter.getOnlinePlayerCount());
        metricsDTO.setModules(moduleController.collectModuleStatuses());
        metricsDTO.setCreatedAt(Instant.now().toString());

        metricsSender.sendMetrics(metricsDTO);
    }

    private String getProxyMode() {
        Config.Proxy config = fileResolver.getConfig().getProxy();
        if (config.isBungeecord()) return "BungeeCord";
        if (config.isVelocity()) return "Velocity";
        if (config.getRedis().isEnable()) return "Redis";

        return "None";
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
