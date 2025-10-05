package net.flectone.pulse.model.event.message;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.flectone.pulse.model.event.Event;

@Getter
public class StatusResponseEvent extends Event {

    private final JsonObject response;

    public StatusResponseEvent(JsonObject response) {
        this.response = response;
    }

}
