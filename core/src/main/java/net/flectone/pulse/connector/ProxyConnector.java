package net.flectone.pulse.connector;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import net.flectone.pulse.config.Config;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.MessageTag;

import java.util.function.Consumer;

public abstract class ProxyConnector {

    private final Config config;
    private final FLogger fLogger;

    @Getter private String channel;

    public ProxyConnector(FileManager fileManager,
                          FLogger fLogger) {
        this.fLogger = fLogger;

        config = fileManager.getConfig();
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
    public abstract boolean sendMessage(FEntity sender, MessageTag tag, Consumer<ByteArrayDataOutput> output);
}
