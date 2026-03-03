package net.flectone.pulse.platform.proxy;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.util.constant.ModuleName;

public interface Proxy {

    boolean isEnable();

    void onEnable();

    void onDisable();

    boolean sendMessage(FEntity sender, ModuleName tag, byte[] message);

}