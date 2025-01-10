package net.flectone.pulse.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import com.google.inject.Inject;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.Config;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.util.PacketEventsUtil;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class FPlayerManager {

    private final String WEBSITE_AVATAR_URL = "https://mc-heads.net/avatar/<skin>/8.png";
    private final String WEBSITE_BODY_URL = "https://mc-heads.net/player/<skin>/16";

    private final HashMap<UUID, FPlayer> fPlayers = new HashMap<>();

    private final Config config;

    @Inject
    private ThreadManager threadManager;

    @Inject
    private IntegrationModule integrationModule;

    @Inject
    private PacketEventsUtil packetEventsUtil;

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
    public abstract String getWorldName(FPlayer fPlayer);
    public abstract String getWorldEnvironment(FPlayer fPlayer);
    public abstract Object getItem(@NotNull UUID uuid);
    public abstract Component getPlayerListHeader(FPlayer fPlayer);
    public abstract Component getPlayerListFooter(FPlayer fPlayer);
    public abstract int getObjectiveScore(UUID uuid, ObjectiveMode objectiveValueType);
    public abstract int getPing(FPlayer fPlayer);
    public abstract double distance(FPlayer first, FPlayer second);
    public abstract GameMode getGamemode(FPlayer fPlayer);
    public abstract boolean hasPlayedBefore(FPlayer fPlayer);
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

    public String getSortedName(@NotNull FPlayer fPlayer) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_17)) {
            return fPlayer.getName();
        }

        int weight = integrationModule.getGroupWeight(fPlayer);

        String paddedRank = String.format("%010d", Integer.MAX_VALUE - weight);
        String paddedName = String.format("%-16s", fPlayer.getName());
        return paddedRank + paddedName;
    }

    public String getSkin(FEntity sender) {
        String replacement = integrationModule.getTextureUrl(sender);
        return replacement == null ? sender.getUuid().toString() : replacement;
    }

    public String getAvatarURL(FEntity sender) {
        return WEBSITE_AVATAR_URL.replace("<skin>", getSkin(sender));
    }

    public String getBodyURL(FEntity sender) {
        return WEBSITE_BODY_URL.replace("<skin>", getSkin(sender));
    }

    public List<FPlayer> getFPlayers() {
        return fPlayers.values().stream().filter(FPlayer::isOnline).toList();
    }

    public void kick(FPlayer fPlayer, Component reason) {
        packetEventsUtil.sendPacket(fPlayer, new WrapperPlayServerDisconnect(reason));
    }

    public List<FPlayer> getFPlayersWithConsole() {
        return fPlayers.values().stream().filter(fPlayer -> fPlayer.isOnline() || fPlayer.isUnknown()).toList();
    }
}
