package net.flectone.pulse.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Singleton
public class ModernPassengersProvider implements PassengersProvider {

    @Inject
    public ModernPassengersProvider() {
    }

    @Override
    public List<Integer> getPassengers(Player player) {
        List<Entity> passengers = player.getPassengers();
        if (passengers.isEmpty()) return Collections.emptyList();

        return passengers.stream()
                .map(Entity::getEntityId)
                .toList();
    }

}
