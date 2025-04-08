package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class BukkitServerAdapter extends PlatformServerAdapter {

    public final static boolean IS_FOLIA;
    public final static boolean IS_PAPER;
    public final static boolean IS_1_20_5_OR_NEWER;

    static {
        IS_FOLIA = isFolia();
        IS_PAPER = isPaper();
        IS_1_20_5_OR_NEWER = getBukkitVersion() >= 20.5;
    }

    private final Injector injector;

    @Inject
    public BukkitServerAdapter(Injector injector) {
        this.injector = injector;
    }

    @Sync
    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public ItemStack buildItemStack(int settingIndex, FPlayer fPlayer, List<String> itemMessages, Command.Chatsetting.SettingItem settingItem) {
        MessagePipeline messagePipeline = injector.getInstance(MessagePipeline.class);

        Component name = itemMessages.isEmpty()
                ? Component.empty()
                : messagePipeline.builder(fPlayer, itemMessages.get(0)).build();

        List<Component> lore = new ArrayList<>();
        if (itemMessages.size() > 1) {
            itemMessages.stream()
                    .skip(1)
                    .forEach(string -> lore.add(
                                    messagePipeline
                                            .builder(fPlayer, string.replace("<chat>", String.valueOf(fPlayer.getSettingValue(FPlayer.Setting.CHAT))))
                                            .build()
                            )
                    );
        }

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            return new ItemStack.Builder()
                    .type(SpigotConversionUtil.fromBukkitItemMaterial(Material.valueOf(settingItem.getMaterials().get(settingIndex))))
                    .component(ComponentTypes.ITEM_NAME, name)
                    .component(ComponentTypes.LORE, new ItemLore(lore))
                    .build();
        }

        org.bukkit.inventory.ItemStack legacyItemStack = new org.bukkit.inventory.ItemStack(Material.valueOf(settingItem.getMaterials().get(settingIndex)));

        ItemMeta itemMeta = legacyItemStack.getItemMeta();
        itemMeta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(name));
        itemMeta.setLore(lore.stream()
                .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                .toList()
        );

        legacyItemStack.setItemMeta(itemMeta);

        return SpigotConversionUtil.fromBukkitItemStack(legacyItemStack);
    }

    @Override
    public String getMinecraftName(Object itemStack) {
        if (!(itemStack instanceof org.bukkit.inventory.ItemStack is)) return "";

        try {

            if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
                Material material = is.getType();
                return (material.isBlock() ? "block" : "item") + ".minecraft." + material.toString().toLowerCase();
            }

            Object nmsStack = is.getClass().getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class).invoke(null, is);

            assert nmsStack != null;
            Object item = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);

            return (String) item.getClass().getMethod("getName").invoke(item);
        } catch (Exception ex) {
            return "";
        }
    }

    @Override
    public String getTPS() {
        try {
            Server server = Bukkit.getServer();

            Field consoleField = server.getClass().getDeclaredField("console");
            consoleField.setAccessible(true);

            Object minecraftServer = consoleField.get(server);

            Field recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
            recentTps.setAccessible(true);

            double tps = Math.round(((double[]) recentTps.get(minecraftServer))[0] * 10.0)/10.0;

            return String.valueOf(Math.min(tps, 20.0));
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public JsonElement getMOTD() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", Bukkit.getServer().getMotd());
        return jsonObject;
    }

    @Override
    public int getMax() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public int getOnlineCount() {
        IntegrationModule integrationModule = injector.getInstance(IntegrationModule.class);
        FPlayerService fPlayerService = injector.getInstance(FPlayerService.class);

        return (int) fPlayerService.getFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isUnknown())
                .filter(fPlayer -> !integrationModule.isVanished(fPlayer))
                .count();
    }

    @Override
    public boolean hasProject(String projectName) {
        return Bukkit.getPluginManager().getPlugin(projectName) != null;
    }

    private static double getBukkitVersion() {
        double finalVersion = 0.0;
        Matcher m = Pattern.compile("1\\.(\\d+(\\.\\d+)?)").matcher(Bukkit.getVersion());
        if (m.find()) {
            try {
                finalVersion = Double.parseDouble(m.group(1));
            } catch (Exception ignored) {}
        }

        return finalVersion;
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
            return true;
        } catch (ClassNotFoundException ignored) {}

        return false;
    }

    private static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            return true;
        } catch (ClassNotFoundException ignored) {}

        return false;
    }

}
