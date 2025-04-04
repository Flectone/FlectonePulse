package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class PlatformPlayerAdapter {

    public abstract int getEntityId(UUID uuid);

    public int getEntityId(FPlayer fPlayer) {
        return getEntityId(fPlayer.getUuid());
    }

    public abstract String getName(UUID uuid);

    public String getName(FPlayer fPlayer) {
        return getName(fPlayer.getUuid());
    }

    public abstract UUID getUUID(Object player);

    public abstract Object convertToPlatformPlayer(FPlayer fPlayer);

    public abstract String getName(Object player);

    public abstract String getWorldName(FPlayer fPlayer);

    public abstract String getWorldEnvironment(FPlayer fPlayer);

    public abstract String getIp(FPlayer fPlayer);

    public abstract Object getItem(@NotNull UUID uuid);

    public abstract Component getPlayerListHeader(FPlayer fPlayer);

    public abstract Component getPlayerListFooter(FPlayer fPlayer);

    public abstract int getObjectiveScore(UUID uuid, ObjectiveMode objectiveValueType);

    public abstract double distance(FPlayer first, FPlayer second);

    public abstract GameMode getGamemode(FPlayer fPlayer);

    public abstract List<UUID> getOnlinePlayers();

    public abstract boolean isConsole(Object player);

    public abstract boolean hasPlayedBefore(FPlayer fPlayer);

    public abstract long getFirstPlayed(FPlayer fPlayer);

    public abstract long getLastPlayed(FPlayer fPlayer);

    public abstract long getAllTimePlayed(FPlayer fPlayer);

    public abstract void clear(FPlayer fPlayer);

    public abstract void update(FPlayer fPlayer);
}
