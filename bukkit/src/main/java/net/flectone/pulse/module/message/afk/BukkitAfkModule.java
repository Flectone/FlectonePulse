package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.afk.listener.AfkListener;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class BukkitAfkModule extends AfkModule {

    private final Map<UUID, Pair<Integer, Vector>> PLAYER_BLOCK = new HashMap<>();

    @Getter private final Message.Afk message;

    private final BukkitListenerRegistry bukkitListenerManager;
    private final FPlayerService fPlayerService;

    @Inject
    public BukkitAfkModule(FileManager fileManager,
                           BukkitListenerRegistry bukkitListenerRegistry,
                           FPlayerService fPlayerService) {
        super(fileManager);

        this.bukkitListenerManager = bukkitListenerRegistry;
        this.fPlayerService = fPlayerService;

        message = fileManager.getMessage().getAfk();
    }

    @Override
    public void reload() {
        PLAYER_BLOCK.clear();

        super.reload();

        bukkitListenerManager.register(AfkListener.class, EventPriority.NORMAL);
    }

    @Async
    @Override
    public void remove(@NotNull String action, FPlayer fPlayer) {
        if (action.isEmpty()) {
            fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
            PLAYER_BLOCK.remove(fPlayer.getUuid());
            fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX);
            return;
        }

        if (checkModulePredicates(fPlayer)) return;
        if (message.getIgnore().contains(action)) return;

        PLAYER_BLOCK.put(fPlayer.getUuid(), new Pair<>(0, new Vector()));
        check(fPlayer);
    }

    @Async
    @Override
    public void check(@NotNull FPlayer fPlayer) {
        if (!fPlayer.isOnline()) {
            String afkSuffix = fPlayer.getSettingValue(FPlayer.Setting.AFK_SUFFIX);

            fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
            PLAYER_BLOCK.remove(fPlayer.getUuid());

            if (afkSuffix != null) {
                send(fPlayer);
            }

            return;
        }

        if (checkModulePredicates(fPlayer)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        int time = (int) (System.currentTimeMillis()/1000);

        Pair<Integer, Vector> timeVector = PLAYER_BLOCK.get(fPlayer.getUuid());
        if (timeVector == null || !timeVector.getValue().equals(getVector(player))) {

            if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) {
                fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
                PLAYER_BLOCK.remove(fPlayer.getUuid());
                fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX);
                send(fPlayer);
            }

            PLAYER_BLOCK.put(fPlayer.getUuid(), new Pair<>(time, getVector(player)));
            return;
        }

        if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) return;
        if (time - timeVector.getKey() < message.getDelay()) return;

        setAfk(fPlayer);
    }

    private Vector getVector(Player player) {
        Location location = player.getLocation();

        return new Vector(location.getX(), location.getY(), location.getZ());
    }
}
