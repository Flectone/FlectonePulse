package net.flectone.pulse.manager;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.google.inject.Inject;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.model.Sound;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class FPlayerManager {

    private final HashMap<UUID, FPlayer> fPlayers = new HashMap<>();

    private final Config config;

    @Inject
    private ThreadManager threadManager;

    public FPlayerManager(FileManager fileManager) {
        config = fileManager.getConfig();
    }

    @NotNull
    public abstract FPlayer get(Object player);

    @NotNull
    public abstract FPlayer getOnline(String playerName);

    public abstract int getEntityId(FPlayer fPlayer);

    @NotNull
    public FPlayer get(UUID uuid) {
        return fPlayers.getOrDefault(uuid, FPlayer.UNKNOWN);
    }

    public void put(FPlayer fPlayer) {
        fPlayers.put(fPlayer.getUuid(), fPlayer);
    }

    public abstract FPlayer convert(Object sender);
    public abstract FPlayer put(Database database, UUID uuid, int entityId, String name, String ip) throws SQLException;
    public abstract void remove(Database database, FPlayer fPlayer) throws SQLException;
    public abstract String getIP(UUID uuid);
    public abstract String getSortedName(@NotNull FPlayer fPlayer);
    public abstract String getSkin(FEntity sender);
    public abstract String getAvatarURL(FEntity sender);
    public abstract String getBodyURL(FEntity sender);
    public abstract String getWorldName(FPlayer fPlayer);
    public abstract String getWorldEnvironment(FPlayer fPlayer);
    public abstract Object getItem(@NotNull UUID uuid);
    public abstract int getObjectiveScore(UUID uuid, ObjectiveMode objectiveValueType);
    public abstract int getPing(FPlayer fPlayer);
    public abstract double distance(FPlayer first, FPlayer second);
    public abstract GameMode getGamemode(FPlayer fPlayer);
    public abstract boolean hasPlayedBefore(FPlayer fPlayer);
    public abstract void playSound(Sound fSound, FPlayer fPlayer);
    public abstract void playSound(Sound fSound, FPlayer fPlayer, Object location);
    public abstract void kick(FPlayer fPlayer, Component reason);
    public abstract void loadOnlinePlayers();

    public void reload() {
        fPlayers.clear();

        FPlayer console = new FPlayer(config.getConsole());
        fPlayers.put(console.getUuid(), console);

        threadManager.runDatabase(database -> {
            database.insertFPlayer(console);
        });

        loadOnlinePlayers();
    }

    public List<FPlayer> getFPlayers() {
        return fPlayers.values().stream().filter(FPlayer::isOnline).toList();
    }

    public List<FPlayer> getFPlayersWithConsole() {
        return fPlayers.values().stream().filter(fPlayer -> fPlayer.isOnline() || fPlayer.isUnknown()).toList();
    }
}
