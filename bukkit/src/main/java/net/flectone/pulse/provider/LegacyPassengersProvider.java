package net.flectone.pulse.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Singleton
public class LegacyPassengersProvider implements PassengersProvider {

    @Inject
    public LegacyPassengersProvider() {
    }

    @Override
    public List<Integer> getPassengers(Player player) {
        return Collections.emptyList();
    }

}
