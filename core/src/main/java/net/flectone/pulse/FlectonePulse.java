package net.flectone.pulse;

import com.google.inject.Injector;
import net.flectone.pulse.exception.ReloadException;

public interface FlectonePulse {

    Injector getInjector();

    void onEnable();

    void onDisable();

    void reload() throws ReloadException;

}
