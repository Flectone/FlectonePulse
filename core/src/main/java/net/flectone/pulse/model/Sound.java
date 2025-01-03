package net.flectone.pulse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Sound {

    private final boolean enable;
    private final float volume;
    private final float pitch;
    private final String type;

    @Setter
    private String permission = "";

    public Sound(boolean enable, float volume, float pitch, String type) {
        this.enable = enable;
        this.volume = volume;
        this.pitch = pitch;
        this.type = type;
    }

    public Sound() {
        this(false, 1f, 1f, "BLOCK_NOTE_BLOCK_BELL");
    }
}
