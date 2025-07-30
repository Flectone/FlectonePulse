package net.flectone.pulse.model;

import net.flectone.pulse.constant.MessageType;

public interface Proxy {

    boolean isEnable();

    void onEnable();

    void onDisable();

    boolean sendMessage(FEntity sender, MessageType tag, byte[] message);

}