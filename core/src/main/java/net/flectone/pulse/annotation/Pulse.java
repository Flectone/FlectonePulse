package net.flectone.pulse.annotation;

import net.flectone.pulse.model.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pulse {
    Event.Priority priority() default Event.Priority.NORMAL;
    boolean ignoreCancelled() default false;
}
