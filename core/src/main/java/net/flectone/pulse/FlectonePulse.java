package net.flectone.pulse;

import net.flectone.pulse.exception.ReloadException;

public interface FlectonePulse {

    <T> T get(Class<T> type);

    boolean isReady();

    void onEnable();

    void onDisable();

    void reload() throws ReloadException;

}
