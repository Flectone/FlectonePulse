package net.flectone.pulse.platform.provider;

import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitModernPassengersProvider implements BukkitPassengersProvider {

    private final ReflectionResolver reflectionResolver;

    // UniversalScheduler implementation
    private final TaskScheduler taskScheduler;

    @Override
    public List<Integer> getPassengers(Player player) {
        List<Entity> passengers = player.getPassengers();
        if (passengers.isEmpty()) return List.of();

        if (reflectionResolver.isFolia()) {
            CompletableFuture<List<Integer>> completableFuture = new CompletableFuture<>();

            taskScheduler.runTask(player, () ->
                    completableFuture.complete(passengers.stream()
                            .map(Entity::getEntityId)
                            .toList()
                    )
            );

            return completableFuture.join();
        }

        return passengers.stream()
                .map(Entity::getEntityId)
                .toList();
    }

}
