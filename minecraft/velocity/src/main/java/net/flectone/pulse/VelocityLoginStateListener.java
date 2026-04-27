package net.flectone.pulse;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import net.flectone.pulse.util.constant.LoginStatus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VelocityLoginStateListener {

    private final Map<UUID, LoginStatus> loginStates = new ConcurrentHashMap<>();

    public LoginStatus getLoginStatus(UUID uuid) {
        return loginStates.get(uuid);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event) {
        loginStates.put(event.getUniqueId(), LoginStatus.PRE_LOGIN);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLogin(LoginEvent event) {
        loginStates.put(event.getPlayer().getUniqueId(), LoginStatus.LOGIN);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        loginStates.put(event.getPlayer().getUniqueId(), LoginStatus.POST_LOGIN);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        loginStates.put(event.getPlayer().getUniqueId(), LoginStatus.CONNECTED);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        loginStates.remove(event.getPlayer().getUniqueId());
    }
}