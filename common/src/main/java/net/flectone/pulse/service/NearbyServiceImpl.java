package net.flectone.pulse.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NearbyServiceImpl implements NearbyService {

    private static final int CELL_SHIFT = 5;

    private volatile Map<String, WorldIndex> worlds = Map.of();

    private final TaskScheduler taskScheduler;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final FPlayerService fPlayerService;

    private record PlayerLocation(
            UUID player,
            PlatformPlayerAdapter.Coordinates coordinates) {
    }

    private record WorldIndex(
            Map<Long, List<PlayerLocation>> cells,
            List<PlayerLocation> all
    ) {
    }

    @Override
    public void reload() {
        taskScheduler.runAsyncTimer(() -> {
            Map<String, WorldIndex> nextWorlds = new HashMap<>();

            fPlayerService.getPlatformFPlayers().forEach(fPlayer -> {
                PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(fPlayer);
                if (coordinates == null) return;

                WorldIndex index = nextWorlds.computeIfAbsent(platformPlayerAdapter.getWorldName(fPlayer),
                        _ -> new WorldIndex(new HashMap<>(), new ArrayList<>()));

                PlayerLocation location = new PlayerLocation(fPlayer.uuid(), coordinates);
                index.all().add(location);
                index.cells().computeIfAbsent(key(cell(coordinates.x()), cell(coordinates.z())), _ -> new ArrayList<>(4))
                        .add(location);
            });

            worlds = nextWorlds;
        }, 20L, 20L);
    }

    @Override
    public @NonNull Set<UUID> getNearbyPlayers(UUID player, double radius) {
        PlatformPlayerAdapter.Coordinates center = platformPlayerAdapter.getCoordinates(player);
        if (center == null) return Set.of();

        WorldIndex index = worlds.get(platformPlayerAdapter.getWorldName(player));
        if (index == null) return Set.of();

        double radiusSquared = radius * radius;
        Set<UUID> result = new HashSet<>();

        int minX = cell(center.x() - radius);
        int maxX = cell(center.x() + radius);
        int minZ = cell(center.z() - radius);
        int maxZ = cell(center.z() + radius);

        long cellCount = (long) (maxX - minX + 1) * (maxZ - minZ + 1);
        if (cellCount >= index.all().size()) {
            collect(index.all(), center, radiusSquared, result);
            return result;
        }

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                List<PlayerLocation> cellPlayers = index.cells().get(key(x, z));
                if (cellPlayers != null) {
                    collect(cellPlayers, center, radiusSquared, result);
                }
            }
        }

        return result;
    }

    private void collect(List<PlayerLocation> locations,
                         PlatformPlayerAdapter.Coordinates center,
                         double radiusSquared,
                         Set<UUID> result) {
        for (PlayerLocation location : locations) {
            if (location.coordinates().distanceSquared(center) <= radiusSquared) {
                result.add(location.player());
            }
        }
    }

    private int cell(double coordinate) {
        return (int) Math.floor(coordinate) >> CELL_SHIFT;
    }

    private long key(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}