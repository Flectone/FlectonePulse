package net.flectone.pulse.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.MessageTag;

import java.util.function.Consumer;

@Singleton
public class FabricProxyManager extends ProxyManager {

    @Inject
    public FabricProxyManager(FileManager fileManager, FLogger fLogger) {
        super(fileManager, fLogger);
    }

    @Override
    public void reloadChannel() {

    }

    @Override
    public void disable() {

    }

    @Override
    public boolean sendMessage(FEntity sender, MessageTag tag, Consumer<ByteArrayDataOutput> outputConsumer) {
        return false;
    }
}
