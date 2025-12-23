package net.flectone.pulse.model.event.message;

import com.google.gson.JsonObject;
import lombok.With;
import net.flectone.pulse.model.event.Event;

@With
public record StatusResponseEvent(
        boolean cancelled,
        JsonObject response
) implements Event {

    public StatusResponseEvent(JsonObject response) {
        this(false, response);
    }

}