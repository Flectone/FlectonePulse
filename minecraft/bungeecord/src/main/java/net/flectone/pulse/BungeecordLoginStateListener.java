package net.flectone.pulse;


import net.flectone.pulse.util.constant.LoginStatus;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BungeecordLoginStateListener implements Listener {

    private final Map<UUID, LoginStatus> loginStates = new ConcurrentHashMap<>();

    public LoginStatus getLoginStatus(UUID uuid) {
        return loginStates.get(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent event) {
        loginStates.put(event.getConnection().getUniqueId(), LoginStatus.PRE_LOGIN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        loginStates.put(uuid, LoginStatus.LOGIN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        loginStates.put(uuid, LoginStatus.POST_LOGIN);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerConnected(ServerConnectedEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        loginStates.put(uuid, LoginStatus.CONNECTED);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        loginStates.remove(uuid);
    }
}