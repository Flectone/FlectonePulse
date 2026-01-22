package net.flectone.pulse.platform.provider;

import org.bukkit.entity.Player;

import java.util.List;

public interface PassengersProvider {

    List<Integer> getPassengers(Player player);

}
