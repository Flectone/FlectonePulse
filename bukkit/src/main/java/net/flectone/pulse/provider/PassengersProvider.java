package net.flectone.pulse.provider;

import org.bukkit.entity.Player;

import java.util.List;

public interface PassengersProvider {

    List<Integer> getPassengers(Player player);

}
