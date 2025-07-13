package net.flectone.pulse.model;

import net.flectone.pulse.util.MessageTag;

public interface Proxy {

    boolean isEnable();

    void onEnable();

    void onDisable();

    boolean sendMessage(FEntity sender, MessageTag tag, byte[] message);

}