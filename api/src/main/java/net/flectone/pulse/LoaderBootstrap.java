package net.flectone.pulse;

import net.flectone.pulse.util.constant.HookType;

public interface LoaderBootstrap {

    void onLoad();

    default void onEnable() {
    }

    default void onDisable() {
    }

    void hook(HookType type, Object... args);

}