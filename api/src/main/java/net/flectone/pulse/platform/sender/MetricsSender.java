package net.flectone.pulse.platform.sender;

import net.flectone.pulse.model.dto.MetricsDTO;

/**
 * Sends anonymous metrics data to FlectonePulse servers.
 *
 * @author TheFaser
 * @since 0.8.1
 */
public interface MetricsSender {

    /**
     * Sends metrics data to FlectonePulse API.
     * Failures are silently ignored to prevent affecting server performance.
     *
     * @param metrics the metrics data to send
     */
    void sendMetrics(MetricsDTO metrics);

}