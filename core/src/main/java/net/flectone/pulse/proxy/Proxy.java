package net.flectone.pulse.proxy;

import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.model.FEntity;

public interface Proxy {

    boolean isEnable();

    void onEnable();

    void onDisable();

    boolean sendMessage(FEntity sender, MessageType tag, byte[] message);

}