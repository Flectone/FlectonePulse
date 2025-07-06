package net.flectone.pulse.sender;

import lombok.Getter;
import net.flectone.pulse.configuration.Config;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.DataConsumer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.logging.FLogger;

import java.io.DataOutputStream;

public abstract class ProxySender {

    private final Config config;
    private final FLogger fLogger;

    @Getter private String channel;

    public ProxySender(FileResolver fileResolver,
                       FLogger fLogger) {
        this.fLogger = fLogger;

        config = fileResolver.getConfig();
    }

    public boolean isEnable() {
        return (config.isBungeecord() || config.isVelocity()) && config.getDatabase().getType() == Config.Database.Type.MYSQL;
    }

    public void reload() {
        if (config.isBungeecord()) {
            channel = "BungeeCord";
        } else if (config.isVelocity()) {
            channel = "flectonepulse:main";
        } else return;

        if (config.getDatabase().getType() == Config.Database.Type.SQLITE) {
            fLogger.warning("SQLITE database and Proxy are incompatible");
        }
    }

    public abstract void disable();

    public abstract boolean sendMessage(FEntity sender, MessageTag tag, DataConsumer<DataOutputStream> output);

}
