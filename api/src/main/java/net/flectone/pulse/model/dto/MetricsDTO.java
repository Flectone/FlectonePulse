package net.flectone.pulse.model.dto;

import lombok.Builder;

import java.util.Map;

/**
 * One anonymous statistics report.
 *
 * @param serverUUID the id identifying this install between reports
 * @param serverCore the server software
 * @param serverVersion the server version
 * @param osName the operating system
 * @param osVersion the operating system version
 * @param osArchitecture the cpu architecture
 * @param javaVersion the java version
 * @param cpuCores how many cores the machine has
 * @param totalRAM how much memory the machine has, in bytes
 * @param location the server region
 * @param projectVersion the plugin version
 * @param projectLanguage the configured language
 * @param onlineMode whether the server authenticates with Mojang
 * @param proxyMode which proxy transport is in use
 * @param databaseMode which database is in use
 * @param playerCount how many players were online
 * @param modules which modules are switched on
 * @param createdAt when the report was taken
 */
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
) {
}
