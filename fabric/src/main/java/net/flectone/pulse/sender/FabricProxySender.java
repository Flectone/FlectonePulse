package net.flectone.pulse.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.DataConsumer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.logging.FLogger;

import java.io.DataOutputStream;

@Singleton
public class FabricProxySender extends ProxySender {

    @Inject
    public FabricProxySender(FileManager fileManager,
                             FLogger fLogger) {
        super(fileManager, fLogger);
    }

    @Override
    public void disable() {

    }

    @Override
    public boolean sendMessage(FEntity sender, MessageTag tag, DataConsumer<DataOutputStream> output) {
        return false;
    }
}
