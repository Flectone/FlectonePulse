package net.flectone.pulse.model;

import lombok.Getter;
import net.flectone.pulse.file.Config;

@Getter
public class FSound {

    private final String sound;
    private final float volume;
    private final float pitch;
    private final String permission;
    private final boolean enable;

    public FSound(Config.Sound configSound, String permission) {
        String[] args = configSound.getType().split(":");

        if (args.length != 3) {
            throw new RuntimeException("Incorrect sound: " + configSound.getType());
        }

        sound = args[0].toUpperCase();
        volume = Float.parseFloat(args[1]);
        pitch = Float.parseFloat(args[2]);
        enable = configSound.isEnable();

        this.permission = permission;
    }
}
