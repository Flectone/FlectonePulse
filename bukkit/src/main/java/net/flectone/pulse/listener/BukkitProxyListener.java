package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.handler.ProxyMessageHandler;
import net.flectone.pulse.sender.ProxySender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BukkitProxyListener implements PluginMessageListener {

    private final ProxySender proxySender;
    private final ProxyMessageHandler proxyMessageHandler;

    @Inject
    public BukkitProxyListener(ProxySender proxySender,
                               ProxyMessageHandler proxyMessageHandler) {
        this.proxySender = proxySender;
        this.proxyMessageHandler = proxyMessageHandler;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] bytes) {
        if (!channel.equals(proxySender.getChannel()) || !proxySender.isEnable()) {
            return;
        }

        proxyMessageHandler.handleProxyMessage(bytes);
    }
}
