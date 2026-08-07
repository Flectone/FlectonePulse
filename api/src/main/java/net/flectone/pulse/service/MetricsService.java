package net.flectone.pulse.service;

import net.flectone.pulse.model.dto.MetricsDTO;

/**
 * Collects anonymous usage statistics and reports them, if the server owner left metrics on.
 * @author TheFaser
 */
public interface MetricsService {

    /**
     * Starts the reporting schedule, replacing any schedule already running.
     */
    void start();

    /**
     * Takes a snapshot of the current statistics.
     *
     * @return the snapshot
     */
    MetricsDTO createMetrics();

    /**
     * Sends one snapshot now, outside the schedule.
     */
    void send();

}
