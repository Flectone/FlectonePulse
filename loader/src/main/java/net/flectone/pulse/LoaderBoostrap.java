package net.flectone.pulse;

import net.flectone.pulse.util.constant.HookType;

public interface LoaderBoostrap {

    void onLoad();

    default void onEnable() {}

    default void onDisable() {}

    <T> T get(Class<T> type);

    void hook(HookType type, Object... args);

}
