package net.flectone.pulse.manager;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.util.MessageTag;

import java.util.function.Consumer;

public abstract class ProxyManager {

    private final Config config;
    private final FLogger fLogger;

    @Getter
    private String channel;

    public ProxyManager(FileManager fileManager,
                        FLogger fLogger) {
        this.fLogger = fLogger;

        config = fileManager.getConfig();
    }

    public boolean isEnabledProxy() {
        return (config.isBungeecord() || config.isVelocity()) && config.getDatabase().getType() == Database.Type.MYSQL;
    }

    public void reload() {
        if (config.isBungeecord()) {
            channel = "BungeeCord";
        } else if (config.isVelocity()) {
            channel = "flectonepulse:main";
        } else return;

        if (config.getDatabase().getType() == Database.Type.SQLITE) {
            fLogger.warning("SQLITE database and Proxy are incompatible");
            return;
        }

        reloadChannel();
    }

    public abstract void reloadChannel();
    public abstract void disable();
    public abstract boolean sendMessage(FEntity sender, MessageTag tag, Consumer<ByteArrayDataOutput> output);
}
