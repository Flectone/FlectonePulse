package net.flectone.pulse.annotation;

import net.flectone.pulse.model.event.Event;

import java.lang.annotation.*;

/**
 * Annotation for methods that listen to and handle FlectonePulse events.
 *
 * @author TheFaser
 * @since 1.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Pulse {

    /**
     * The execution priority of this event listener.
     * Listeners with lower priority values are executed first.
     * Default is {@link Event.Priority#NORMAL}.
     *
     * @return the listener priority
     * @see Event.Priority
     */
    Event.Priority priority() default Event.Priority.NORMAL;

    /**
     * Whether this listener should ignore already cancelled events.
     * If true, the listener will not be called if the event was cancelled earlier.
     *
     * @return true to ignore cancelled events
     */
    boolean ignoreCancelled() default false;

}