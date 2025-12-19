package net.flectone.pulse.config.setting;

import net.flectone.pulse.model.util.Cooldown;
import net.flectone.pulse.model.util.Sound;

import java.util.List;

public interface CommandSetting extends EnableSetting {

    List<String> aliases();

    Cooldown cooldown();

    Sound sound();

}
