package net.flectone.pulse.config.setting;

import java.util.List;
import java.util.Map;

/**
 * Configuration setting interface for message channels
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface MessageChannelSetting {

    /**
     * Retrieves the message channel
     *
     * @return A map where keys represent channel names and values are lists of messages associated with each channel
     */
    Map<String, List<String>> messageChannel();

}
