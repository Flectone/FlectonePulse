package net.flectone.pulse.model.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

public record Sound(
        boolean enable,
        float volume,
        float pitch,
        String category,
        String name
) {

    public static final Sound DEFAULT = new Sound(false, 1f, 1f, "BLOCK", "minecraft:block.note_block.bell");

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
        if (!isEnable) return DEFAULT;

        Object volume = map.get("volume");
        float floatVolume = volume == null ? 1f : Float.parseFloat(String.valueOf(volume));

        Object pitch = map.get("pitch");
        float floatPitch = pitch == null ? 1f : Float.parseFloat(String.valueOf(pitch));

        Object category = map.get("category");
        String stringCategory = category == null ? DEFAULT.category() : String.valueOf(category);

        Object name = map.get("name");
        String stringName = name == null ? DEFAULT.name() : String.valueOf(name);

        return new Sound(true, floatVolume, floatPitch, stringCategory, stringName);
    }

}
