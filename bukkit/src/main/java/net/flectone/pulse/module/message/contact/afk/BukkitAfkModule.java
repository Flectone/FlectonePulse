package net.flectone.pulse.module.message.contact.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.afk.listener.AfkListener;
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

    @Getter private final Message.Contact.Afk message;

    private final FPlayerDAO fPlayerDAO;
    private final BukkitListenerManager bukkitListenerManager;

    @Inject
    public BukkitAfkModule(FileManager fileManager,
                           FPlayerDAO fPlayerDAO,
                           BukkitListenerManager bukkitListenerManager) {
        super(fileManager, fPlayerDAO);

        this.fPlayerDAO = fPlayerDAO;
        this.bukkitListenerManager = bukkitListenerManager;

        message = fileManager.getMessage().getContact().getAfk();
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
            String databaseAfkSuffix = fPlayer.getAfkSuffix();

            fPlayer.setAfkSuffix(null);
            PLAYER_BLOCK.remove(fPlayer.getUuid());

            if (databaseAfkSuffix != null) {
                fPlayerDAO.updateFPlayer(fPlayer);
            }

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
            String afkSuffix = fPlayer.getAfkSuffix();

            fPlayer.setAfkSuffix(null);
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

            if (fPlayer.getAfkSuffix() != null) {
                fPlayer.setAfkSuffix(null);
                send(fPlayer);
                fPlayerDAO.updateFPlayer(fPlayer);
            }

            PLAYER_BLOCK.put(fPlayer.getUuid(), new Pair<>(time, getVector(player)));
            return;
        }

        if (fPlayer.getAfkSuffix() != null) return;
        if (time - timeVector.getKey() < message.getDelay()) return;

        setAfk(fPlayer);
    }

    private Vector getVector(Player player) {
        Location location = player.getLocation();

        return new Vector(location.getX(), location.getY(), location.getZ());
    }
}
