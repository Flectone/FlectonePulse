package net.flectone.pulse.listener.player;

import net.flectone.pulse.model.event.player.PlayerPreLoginEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Runs the checks that decide whether a connecting player is let in, namely the whitelist, bans and maintenance.
 * @author TheFaser
 */
public interface PlayerPreLoginProcessor {

    /**
     * Checks a connecting player and reports the outcome.
     *
     * @param uuid the account id
     * @param name the player name
     * @param kickConsumer receives the event, and is only called when the player should be refused
     */
    void processLogin(UUID uuid, String name, Consumer<PlayerPreLoginEvent> kickConsumer);

}
