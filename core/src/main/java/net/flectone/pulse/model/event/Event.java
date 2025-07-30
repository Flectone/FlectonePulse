package net.flectone.pulse.model.event;

import lombok.Getter;

@Getter
public class Event {

    private final Type type;

    public Event(Type type) {
        this.type = type;
    }

    public enum Type {
        PLAYER_PRE_LOGIN,
        PLAYER_LOAD,
        PLAYER_JOIN,
        PLAYER_QUIT,
        PLAYER_PERSIST_AND_DISPOSE,
        RECEIVE_TRANSLATABLE_MESSAGE,
        SENDER_TO_RECEIVER_MESSAGE,
    }
}
