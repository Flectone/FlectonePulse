package net.flectone.pulse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Sound {

    private final boolean enable;
    private final float volume;
    private final float pitch;
    private final String category;
    private final String name;

    @Setter
    private String permission = "";

    public Sound(boolean enable, float volume, float pitch, String category, String name) {
        this.enable = enable;
        this.volume = volume;
        this.pitch = pitch;
        this.category = category;
        this.name = name;
    }

    public Sound() {
        this(false, 1f, 1f, "BLOCK", "minecraft:block.note_block.bell");
    }
}
