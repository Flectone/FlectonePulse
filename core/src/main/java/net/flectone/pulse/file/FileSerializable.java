package net.flectone.pulse.file;

import lombok.Getter;
import net.elytrium.serializer.SerializerConfig;
import net.elytrium.serializer.annotations.Transient;
import net.elytrium.serializer.custom.ClassSerializer;
import net.elytrium.serializer.language.object.YamlSerializable;
import net.flectone.pulse.file.model.Sound;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public abstract class FileSerializable extends YamlSerializable {

    private static final SerializerConfig CONFIG = new SerializerConfig
            .Builder()
            .setBackupOnErrors(true)
            .registerSerializer(new ClassSerializer<Sound, Map<String, Object>>() {

                @Override
                public Map<String, Object> serialize(Sound sound) {
                    Map<String, Object> map = new LinkedHashMap<>();

                    map.put("enable", sound.isEnable());

                    if (sound.isEnable()) {
                        map.put("volume", sound.getVolume());
                        map.put("pitch", sound.getPitch());
                        map.put("type", sound.getType());
                    }

                    return map;
                }

                @Override
                public Sound deserialize(Map<String, Object> map) {
                    boolean isEnable = Boolean.parseBoolean(String.valueOf(map.get("enable")));
                    if (!isEnable) return new Sound();

                    Object volume = map.get("volume");
                    float floatVolume = volume == null ? 1f : Float.parseFloat(String.valueOf(volume));

                    Object pitch = map.get("pitch");
                    float floatPitch = pitch == null ? 1f : Float.parseFloat(String.valueOf(pitch));

                    Object type = map.get("type");
                    String stringType = "BLOCK_NOTE_BLOCK_BELL";
                    if (type != null) {
                        stringType = String.valueOf(type);

                        // older version check (0.1.0 and older)
                        // type:volume:pitch
                        String[] legacySound = stringType.split(":");
                        if (legacySound.length > 1) {
                            stringType = legacySound[0];
                        }
                    }

                    return new Sound(true, floatVolume, floatPitch, stringType);
                }
            })
            .build();

    @Transient
    private final Path path;

    public FileSerializable(Path path) {
        super(CONFIG);
        this.path = path;
    }
}
