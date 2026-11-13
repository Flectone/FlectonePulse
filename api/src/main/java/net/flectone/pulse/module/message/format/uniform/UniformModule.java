package net.flectone.pulse.module.message.format.uniform;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.module.ModuleSimple;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Parses messages sent by other plugins.
 * @author TheFaser
 */
public interface UniformModule extends ModuleSimple {

    /**
     * Remembers a component FlectonePulse has already rendered.
     *
     * @param component the rendered component
     */
    void markFormatted(@Nullable Component component);

    /**
     * Checks whether FlectonePulse has already rendered a component.
     *
     * @param component the component to check
     * @return true if the component came from FlectonePulse
     */
    boolean isFormatted(@Nullable Component component);

    @Override
    Message.Format.Uniform config();

    @Override
    Permission.Message.Format.Uniform permission();

}
