package net.flectone.pulse.platform.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitModernPassengersProvider implements BukkitPassengersProvider {

    private final ReflectionResolver reflectionResolver;
    private final TaskScheduler taskScheduler;
    private final com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler platformTaskScheduler;

    @Override
    public List<Integer> getPassengers(Player player) {
        List<Entity> passengers = player.getPassengers();
        if (passengers.isEmpty()) return List.of();

        if (reflectionResolver.isFolia()) {
            CompletableFuture<List<Integer>> completableFuture = new CompletableFuture<>();

            platformTaskScheduler.runTask(player, () ->
                    completableFuture.complete(passengers.stream()
                            .map(Entity::getEntityId)
                            .toList()
                    )
            );

            return taskScheduler.await(completableFuture, List.of(), "Passengers of " + player.getName());
        }

        return passengers.stream()
                .map(Entity::getEntityId)
                .toList();
    }

}
