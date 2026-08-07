package net.flectone.pulse.platform.formatter;

import net.flectone.pulse.config.setting.MessageChannelSetting;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.event.IntegrationMessageFormat;
import net.flectone.pulse.model.event.message.context.MessageContext;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Create format for external integrations (Discord, Telegram, Twitch, etc.)
 *
 * @author TheFaser
 * @since 1.10.0
 */
public interface IntegrationFormatter {

    /**
     * Checks if the event sender is in vanish
     *
     * @param messageContext The message context containing sender information and optional vanish state
     * @return true if the sender is vanished and vanish should not be ignored, false otherwise
     */
    boolean isVanished(MessageContext messageContext);

    /**
     * Retrieves a list of message names that have corresponding non-empty channel configurations.
     *
     * @param moduleName The module name to check for existence in message channels
     * @param integrationMessageFormat Contains the collection of message names to validate
     * @param messageChannelSetting Configuration providing the mapping of channel names to message lists
     * @return A list of message names that have non-empty channel configurations, including the module name if applicable
     */
    @NonNull List<String> getExistedMessageNames(@NonNull ModuleName moduleName, @NonNull IntegrationMessageFormat integrationMessageFormat, MessageChannelSetting messageChannelSetting);

    /**
     * Creates a format function that processes and replaces placeholders in message templates.
     *
     * @param integrationMessageFormat Provides integration-specific format transformations
     * @param messageContext The message context containing sender information and flags for message processing
     * @return A unary operator that takes an input string and returns the formatted message with all placeholders resolved
     */
    @NonNull UnaryOperator<String> createFormat(@NonNull IntegrationMessageFormat integrationMessageFormat, @NonNull MessageContext messageContext);

}
