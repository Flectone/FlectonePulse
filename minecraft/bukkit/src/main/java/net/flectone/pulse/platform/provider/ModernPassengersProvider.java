package net.flectone.pulse.platform.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ModernPassengersProvider implements PassengersProvider {

    @Override
    public List<Integer> getPassengers(Player player) {
        List<Entity> passengers = player.getPassengers();
        if (passengers.isEmpty()) return Collections.emptyList();

        return passengers.stream()
                .map(Entity::getEntityId)
                .toList();
    }

}
