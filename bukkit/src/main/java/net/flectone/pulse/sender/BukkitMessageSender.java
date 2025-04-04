package net.flectone.pulse.sender;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Toast;
import net.flectone.pulse.module.integration.BukkitIntegrationModule;
import net.flectone.pulse.provider.PacketProvider;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.serializer.PacketSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class BukkitMessageSender extends MessageSender {

    private final Map<UUID, Long> playerCacheMap = new HashMap<>();

    private final Plugin plugin;
    private final TaskScheduler taskScheduler;
    private final BukkitIntegrationModule integrationModule;

    @Inject
    public BukkitMessageSender(Plugin plugin,
                               TaskScheduler taskScheduler,
                               BukkitIntegrationModule integrationModule,
                               PlatformPlayerAdapter platformPlayerAdapter,
                               PacketSerializer packetSerializer,
                               PacketSender packetSender,
                               PacketProvider packetProvider,
                               FLogger fLogger) {
        super(taskScheduler, platformPlayerAdapter, packetSerializer, packetSender, packetProvider, fLogger);

        this.plugin = plugin;
        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component) {
        if (integrationModule.sendMessageWithInteractiveChat(fPlayer, component)) return;

        super.sendMessage(fPlayer, component);
    }

    @Override
    public void sendToast(FPlayer fPlayer, Component title, Toast toast) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        long lastCache = playerCacheMap.containsKey(fPlayer.getUuid())
                // cooldown 60 seconds
                ? Math.abs(playerCacheMap.get(fPlayer.getUuid()) - System.currentTimeMillis()) > 60 * 1000
                    ? System.currentTimeMillis()
                    : playerCacheMap.get(fPlayer.getUuid())
                : System.currentTimeMillis();

        String key = fPlayer.getUuid().toString() + lastCache;
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);

        createToast(namespacedKey, title, toast);
        grantToast(namespacedKey, player);
        revokeToast(namespacedKey, player);

        playerCacheMap.put(fPlayer.getUuid(), lastCache);
    }

    private void createToast(NamespacedKey key, Component title, Toast toast) {
        JsonObject jsonObject = new JsonObject();

        JsonObject criteriaObject = new JsonObject();
        JsonObject triggerObject = new JsonObject();
        triggerObject.addProperty("trigger", "minecraft:impossible");

        criteriaObject.add("trigger", triggerObject);

        jsonObject.add("criteria", criteriaObject);

        JsonObject displayObject = new JsonObject();

        JsonObject iconObject = new JsonObject();

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            iconObject.addProperty("id", toast.icon());
        } else {
            iconObject.addProperty("item", toast.icon());
        }

        displayObject.add("icon", iconObject);

        displayObject.add("title", GsonComponentSerializer.gson().serializeToTree(title));
        displayObject.add("description", GsonComponentSerializer.gson().serializeToTree(Component.empty()));
        displayObject.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/adventure.png");
        displayObject.addProperty("frame", toast.style().name().toLowerCase());
        displayObject.addProperty("announce_to_chat", false);
        displayObject.addProperty("show_toast", true);
        displayObject.addProperty("hidden", true);

        jsonObject.add("display", displayObject);

        JsonArray requirementsElements = new JsonArray();

        JsonArray jsonElements = new JsonArray();
        jsonElements.add("trigger");

        requirementsElements.add(jsonElements);

        jsonObject.add("requirements", requirementsElements);

        String jsonToast = jsonObject.toString();

        taskScheduler.runSync(() -> {
            if (Bukkit.getServer().getAdvancement(key) != null) return;

            Bukkit.getUnsafe().loadAdvancement(key, jsonToast);
        });
    }

    private void grantToast(NamespacedKey key, Player player) {
        taskScheduler.runSyncLater(() -> {
            Advancement advancement = Bukkit.getAdvancement(key);
            if (advancement == null) return;

            player.getAdvancementProgress(advancement).awardCriteria("trigger");
        }, 2);
    }

    private void revokeToast(NamespacedKey key, Player player) {
        taskScheduler.runSyncLater(() -> {
            Advancement advancement = Bukkit.getAdvancement(key);
            if (advancement == null) return;

            player.getAdvancementProgress(advancement).revokeCriteria("trigger");
        }, 4);
    }
}
