package net.flectone.pulse.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDTO {

    private String serverCore;
    private String serverVersion;
    private String osName;
    private String osVersion;
    private String osArchitecture;
    private String javaVersion;
    private int cpuCores;
    private long totalRAM;
    private String location;
    private String projectVersion;
    private String projectLanguage;
    private String onlineMode;
    private String proxyMode;
    private String databaseMode;
    private int playerCount;
    private Map<String, String> modules;
    private String createdAt;

}
