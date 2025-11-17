package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Sound {

    private final boolean enable;
    private final float volume;
    private final float pitch;
    private final SoundCategory category;
    private final String name;

    // runtime values
    @Setter private String permission = "";
    private com.github.retrooper.packetevents.protocol.sound.Sound packet;

    public Sound(boolean enable, float volume, float pitch, SoundCategory category, String name) {
        this.enable = enable;
        this.volume = volume;
        this.pitch = pitch;
        this.category = category;
        this.name = name;
    }

    public Sound() {
        this(false, 1f, 1f, SoundCategory.BLOCK, "minecraft:block.note_block.bell");
    }

    @NotNull
    public com.github.retrooper.packetevents.protocol.sound.Sound getPacket() {
        if (this.packet == null) {
            this.packet = Sounds.getByNameOrCreate(name);
        }

        return this.packet;
    }

    @JsonValue
    public Map<String, Object> toJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enable", this.enable);

        if (this.enable) {
            map.put("volume", this.volume);
            map.put("pitch", this.pitch);
            map.put("category", this.category);
            map.put("name", this.name);
        }

        return map;
    }

    @JsonCreator
    public static Sound fromJson(Map<String, Object> map) {
        boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
        if (!isEnable) return new Sound();

        Object volume = map.get("volume");
        float floatVolume = volume == null ? 1f : Float.parseFloat(String.valueOf(volume));

        Object pitch = map.get("pitch");
        float floatPitch = pitch == null ? 1f : Float.parseFloat(String.valueOf(pitch));

        Object category = map.get("category");
        SoundCategory stringCategory = category == null ? SoundCategory.BLOCK : Arrays.stream(SoundCategory.values())
                .filter(categoryValue -> categoryValue.name().equalsIgnoreCase(String.valueOf(category)))
                .findAny()
                .orElse(SoundCategory.MASTER);

        Object name = map.get("name");
        String stringName = name == null ? Sounds.BLOCK_NOTE_BLOCK_BELL.getName().toString() : String.valueOf(name);

        return new Sound(true, floatVolume, floatPitch, stringCategory, stringName);
    }
}
