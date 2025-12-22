package net.flectone.pulse.model.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record MetricsDTO(
        String serverUUID,
        String serverCore,
        String serverVersion,
        String osName,
        String osVersion,
        String osArchitecture,
        String javaVersion,
        int cpuCores,
        long totalRAM,
        String location,
        String projectVersion,
        String projectLanguage,
        String onlineMode,
        String proxyMode,
        String databaseMode,
        int playerCount,
        Map<String, String> modules,
        String createdAt
) {}
