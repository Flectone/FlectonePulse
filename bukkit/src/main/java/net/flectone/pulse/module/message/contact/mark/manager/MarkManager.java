package net.flectone.pulse.module.message.contact.mark.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.contact.mark.model.FMark;
import net.flectone.pulse.util.RandomUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.megavex.scoreboardlibrary.api.team.ScoreboardTeam;
import net.megavex.scoreboardlibrary.api.team.TeamDisplay;
import net.megavex.scoreboardlibrary.api.team.TeamManager;
import net.megavex.scoreboardlibrary.api.team.enums.CollisionRule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class MarkManager {

    private final Map<UUID, FMark> playerMarkMap = new HashMap<>();

    private final TeamManager teamManager;
    private final ThreadManager threadManager;
    private final FPlayerManager fPlayerManager;
    private final RandomUtil randomUtil;

    @Inject
    public MarkManager(TeamManager teamManager,
                       ThreadManager threadManager,
                       FPlayerManager fPlayerManager,
                       RandomUtil randomUtil) {
        this.teamManager = teamManager;
        this.threadManager = threadManager;
        this.fPlayerManager = fPlayerManager;
        this.randomUtil = randomUtil;
    }

    public boolean create(FPlayer fPlayer, FMark fMark, NamedTextColor color) {
        Entity entity = getRayTracedEntity(fPlayer, fMark.getRange());

        Runnable removeMarkRunnable;

        if (entity == null) {
            Location location = getRayTracedLocation(fPlayer, fMark.getRange());
            if (location == null) return false;

            fMark.create(fPlayerManager, location, randomUtil);

            ScoreboardTeam scoreboardTeam = teamManager.createIfAbsent(color.toString());
            TeamDisplay teamDisplay = scoreboardTeam.defaultDisplay();
            teamDisplay.playerColor(color);

            teamDisplay.addEntry(fMark.getUuid().toString());
            teamDisplay.collisionRule(CollisionRule.NEVER);

            removeMarkRunnable = () -> {
                fMark.setGlowing(location);
                threadManager.runSyncLater(fMark::remove, fMark.getDuration());
            };

        } else {
            fMark.create(fPlayerManager, entity);

            if (entity instanceof Player target) {
                FPlayer fReceiver = fPlayerManager.get(target);

                ScoreboardTeam scoreboardTeam = teamManager.createIfAbsent(fPlayerManager.getSortedName(fReceiver));
                TeamDisplay teamDisplay = scoreboardTeam.defaultDisplay();
                teamDisplay.playerColor(color);

                teamDisplay.addEntry(target.getName());

                NamedTextColor lastColor = teamDisplay.playerColor();

                removeMarkRunnable = () -> {
                    fMark.setGlowing(true);

                    threadManager.runSyncLater(() -> {
                        teamDisplay.playerColor(lastColor);
                        fMark.setAlive(false);
                        fMark.setGlowing(false);

                    }, fMark.getDuration());
                };

            } else {

                ScoreboardTeam scoreboardTeam = teamManager.createIfAbsent(color.toString());
                TeamDisplay teamDisplay = scoreboardTeam.defaultDisplay();
                teamDisplay.playerColor(color);

                teamDisplay.addEntry(fMark.getUuid().toString());

                removeMarkRunnable = () -> {
                    fMark.setGlowing(true);

                    threadManager.runAsyncLater(() -> {
                        fMark.setAlive(false);
                        fMark.setGlowing(false);
                        teamDisplay.removeEntry(fMark.getUuid().toString());
                    }, fMark.getDuration());
                };
            }
        }

        // color update takes too long and there is a visual bug when color changes from white to specified color
        // a delay of 2 ticks fixes that
        threadManager.runAsyncLater(removeMarkRunnable, 2);

        playerMarkMap.put(fPlayer.getUuid(), fMark);
        return true;
    }

    @Sync
    public Location getRayTracedLocation(FPlayer fPlayer, int range) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) {
            return null;
        }

        RayTraceResult rayTraceResult = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
                player.getLocation().getDirection(), range);

        if (rayTraceResult == null) {
            return null;
        }

        Vector vector = rayTraceResult.getHitPosition();

        return new Location(player.getWorld(), vector.getX(), vector.getY(), vector.getZ());
    }

    @Sync
    public Entity getRayTracedEntity(FPlayer fPlayer, int range) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) {
            return null;
        }

        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getLocation().getDirection(), range, 0.25, entity -> {
                    // ignoring executor
                    if (player.equals(entity)) return false;
                    return player.hasLineOfSight(entity);
                });

        return rayTraceResult != null ? rayTraceResult.getHitEntity() : null;
    }

    public boolean contains(FPlayer fPlayer) {
        FMark fMark = playerMarkMap.get(fPlayer.getUuid());
        return fMark != null && fMark.isAlive();
    }

    public void reload() {
        playerMarkMap.clear();
    }

}
