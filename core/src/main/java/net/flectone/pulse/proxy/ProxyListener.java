package net.flectone.pulse.proxy;

import com.google.common.io.ByteArrayDataOutput;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.MessageTag;

import java.util.function.Consumer;

public interface ProxyListener {

    boolean sendMessage(MessageTag tag, FEntity sender, Consumer<ByteArrayDataOutput> interfaceProxyDataOutput);

}
