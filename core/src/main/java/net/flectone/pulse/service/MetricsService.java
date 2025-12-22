package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.dto.MetricsDTO;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.sender.MetricsSender;
import net.flectone.pulse.util.file.FileFacade;

import java.time.Instant;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MetricsService {

    private final TaskScheduler taskScheduler;
    private final MetricsSender metricsSender;
    private final PlatformServerAdapter platformServerAdapter;
    private final PacketProvider packetProvider;
    private final FileFacade fileFacade;
    private final ModuleController moduleController;

    public void reload() {
        taskScheduler.runAsyncTimer(this::send, 0L, 20L * 60 * 60);
    }

    public void send() {
        Config config = fileFacade.config();

        MetricsDTO metricsDTO = MetricsDTO.builder()
                .serverUUID(platformServerAdapter.getServerUUID())
                .serverCore(platformServerAdapter.getServerCore())
                .serverVersion(packetProvider.getServerVersion().getReleaseName())
                .osName(getOsName())
                .osArchitecture(getOsArch())
                .osVersion(getOsVersion())
                .javaVersion(getJavaVersion())
                .cpuCores(Runtime.getRuntime().availableProcessors())
                .totalRAM(Runtime.getRuntime().maxMemory())
                .projectVersion(config.version())
                .projectLanguage(config.language().type())
                .onlineMode(booleanToString(platformServerAdapter.isOnlineMode()))
                .proxyMode(getProxyMode())
                .databaseMode(config.database().type().name())
                .playerCount(platformServerAdapter.getOnlinePlayerCount())
                .modules(moduleController.collectModuleStatuses())
                .createdAt(Instant.now().toString())
                .build();

        metricsSender.sendMetrics(metricsDTO);
    }

    private String booleanToString(boolean value) {
        return value ? "True" : "False";
    }

    private String getProxyMode() {
        Config.Proxy config = fileFacade.config().proxy();
        if (config.bungeecord()) return "BungeeCord";
        if (config.velocity()) return "Velocity";
        if (config.redis().enable()) return "Redis";

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
