package net.flectone.pulse.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.platform.handler.ProxyMessageHandler;
import net.flectone.pulse.platform.proxy.BukkitProxy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jspecify.annotations.NonNull;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitProxyListener implements PluginMessageListener {

    private final BukkitProxy bukkitProxy;
    private final ProxyMessageHandler proxyMessageHandler;

    @Override
    public void onPluginMessageReceived(@NonNull String channel, @NonNull Player player, byte[] bytes) {
        if (!channel.equals(bukkitProxy.getChannel()) || !bukkitProxy.isEnable()) {
            return;
        }

        proxyMessageHandler.handleProxyMessage(bytes);
    }
}
