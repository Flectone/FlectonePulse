package net.flectone.pulse.module.message.death.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Death {

    private final String key;

    private boolean isPlayer;

    private String targetName;
    private String targetType;
    private UUID targetUUID;

    private Death killer;
    private String item;

    public Death(String key) {
        this.key = key;
    }
}
