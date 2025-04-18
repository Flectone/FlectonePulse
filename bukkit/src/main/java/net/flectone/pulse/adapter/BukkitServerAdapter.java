package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.service.FPlayerService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class BukkitServerAdapter implements PlatformServerAdapter {

    public final static boolean IS_FOLIA;
    public final static boolean IS_PAPER;
    public final static boolean IS_1_20_5_OR_NEWER;

    static {
        IS_FOLIA = detectFolia();
        IS_PAPER = detectPaper();
        IS_1_20_5_OR_NEWER = parseBukkitVersion() >= 20.5;
    }

    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<MessagePipeline> messagePipelineProvider;

    @Inject
    public BukkitServerAdapter(Provider<IntegrationModule> integrationModuleProvider,
                               Provider<FPlayerService> fPlayerServiceProvider,
                               Provider<MessagePipeline> messagePipelineProvider) {
        this.integrationModuleProvider = integrationModuleProvider;
        this.fPlayerServiceProvider = fPlayerServiceProvider;
        this.messagePipelineProvider = messagePipelineProvider;
    }

    @Sync
    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public @NotNull String getTPS() {
        try {
            double[] recentTps = getRecentTps();
            double tps = Math.min(Math.round(recentTps[0] * 10.0) / 10.0, 20.0);
            return String.valueOf(tps);
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private double[] getRecentTps() throws ReflectiveOperationException {
        Server server = Bukkit.getServer();
        Object minecraftServer = getMinecraftServer(server);
        Field recentTpsField = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
        recentTpsField.setAccessible(true);
        return (double[]) recentTpsField.get(minecraftServer);
    }

    private Object getMinecraftServer(@NotNull Server server) throws ReflectiveOperationException {
        Field consoleField = server.getClass().getDeclaredField("console");
        consoleField.setAccessible(true);
        return consoleField.get(server);
    }

    @Override
    public @NotNull JsonElement getMOTD() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", Bukkit.getServer().getMotd());
        return jsonObject;
    }

    @Override
    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public int getOnlinePlayerCount() {
        return (int) fPlayerServiceProvider.get().getFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isUnknown())
                .filter(fPlayer -> !integrationModuleProvider.get().isVanished(fPlayer))
                .count();
    }

    @Override
    public boolean hasProject(String projectName) {
        return Bukkit.getPluginManager().getPlugin(projectName) != null;
    }

    @Override
    public @NotNull ItemStack buildItemStack(int settingIndex, @NotNull FPlayer fPlayer,
                                             @NotNull List<List<String>> messages,
                                             @NotNull Command.Chatsetting.SettingItem settingItem) {
        List<String> itemMessages = messages.get(settingIndex);

        Component name = buildItemNameComponent(fPlayer, itemMessages);
        List<Component> lore = buildItemLoreComponents(fPlayer, itemMessages);

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            return buildModernItemStack(settingIndex, settingItem, name, lore);
        }

        return buildLegacyItemStack(settingIndex, settingItem, name, lore);
    }

    private @NotNull Component buildItemNameComponent(@NotNull FPlayer fPlayer, @NotNull List<String> itemMessages) {
        return itemMessages.isEmpty()
                ? Component.empty()
                : messagePipelineProvider.get().builder(fPlayer, itemMessages.get(0)).build();
    }

    private @NotNull List<Component> buildItemLoreComponents(@NotNull FPlayer fPlayer, @NotNull List<String> itemMessages) {
        List<Component> lore = new ArrayList<>();
        if (itemMessages.size() > 1) {
            itemMessages.stream()
                    .skip(1)
                    .forEach(message -> lore.add(
                            messagePipelineProvider.get().builder(fPlayer, replaceChatPlaceholder(message, fPlayer)).build()
                    ));
        }

        return lore;
    }

    private String replaceChatPlaceholder(@NotNull String message, @NotNull FPlayer fPlayer) {
        return message.replace("<chat>", String.valueOf(fPlayer.getSettingValue(FPlayer.Setting.CHAT)));
    }

    private @NotNull ItemStack buildModernItemStack(int settingIndex, @NotNull Command.Chatsetting.SettingItem settingItem,
                                                    @NotNull Component name, @NotNull List<Component> lore) {
        return new ItemStack.Builder()
                .type(SpigotConversionUtil.fromBukkitItemMaterial(getItemMaterial(settingIndex, settingItem)))
                .component(ComponentTypes.ITEM_NAME, name)
                .component(ComponentTypes.LORE, new ItemLore(lore))
                .build();
    }

    private @NotNull ItemStack buildLegacyItemStack(int settingIndex, @NotNull Command.Chatsetting.SettingItem settingItem,
                                                    @NotNull Component name, @NotNull List<Component> lore) {
        org.bukkit.inventory.ItemStack legacyItem = new org.bukkit.inventory.ItemStack(getItemMaterial(settingIndex, settingItem));
        ItemMeta meta = legacyItem.getItemMeta();

        meta.setDisplayName(serializeComponent(name));
        meta.setLore(lore.stream()
                .map(this::serializeComponent)
                .toList());

        legacyItem.setItemMeta(meta);
        return SpigotConversionUtil.fromBukkitItemStack(legacyItem);
    }

    private @NotNull Material getItemMaterial(int settingIndex, @NotNull Command.Chatsetting.SettingItem settingItem) {
        return Material.valueOf(settingItem.getMaterials().get(settingIndex));
    }

    private @NotNull String serializeComponent(@NotNull Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    @Override
    public @NotNull String getItemName(@Nullable Object itemStack) {
        if (!(itemStack instanceof org.bukkit.inventory.ItemStack bukkitItem)) {
            return "";
        }

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
            return getModernItemName(bukkitItem.getType());
        }

        return getLegacyItemName(bukkitItem);
    }

    private @NotNull String getModernItemName(@NotNull Material material) {
        return (material.isBlock() ? "block" : "item") + ".minecraft." + material.toString().toLowerCase();
    }

    private @NotNull String getLegacyItemName(@NotNull org.bukkit.inventory.ItemStack itemStack) {
        try {
            Object nmsStack = itemStack.getClass()
                    .getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class)
                    .invoke(null, itemStack);

            Object item = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);
            return (String) item.getClass().getMethod("getName").invoke(item);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public @NotNull Component translateItemName(Object item, boolean translatable) {
        if (!(item instanceof org.bukkit.inventory.ItemStack itemStack)) return Component.empty();

        Component component = itemStack.getItemMeta() == null || itemStack.getItemMeta().getDisplayName().isEmpty()
                ? createTranslatableItemName(itemStack, translatable)
                : Component.text(itemStack.getItemMeta().getDisplayName()).decorate(TextDecoration.ITALIC);

        if (itemStack.getType() == Material.AIR) return component;

        Key key = Key.key(itemStack.getType().name().toLowerCase());
        return component.hoverEvent(HoverEvent.showItem(key, itemStack.getAmount()));
    }

    private Component createTranslatableItemName(org.bukkit.inventory.ItemStack itemStack, boolean translatable) {
        if (translatable) {
            String itemName = getItemName(itemStack);

            return Component.translatable(itemName);
        }

        String itemName = itemStack.getType().name().toLowerCase().replace("_", " ");

        return Component.text(itemName);
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
            return true;
        } catch (ClassNotFoundException ignored) {}

        return false;
    }

    private static boolean detectPaper() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            return true;
        } catch (ClassNotFoundException ignored) {}

        return false;
    }

    private static double parseBukkitVersion() {
        double finalVersion = 0.0;
        Matcher m = Pattern.compile("1\\.(\\d+(\\.\\d+)?)").matcher(Bukkit.getVersion());
        if (m.find()) {
            try {
                finalVersion = Double.parseDouble(m.group(1));
            } catch (Exception ignored) {}
        }

        return finalVersion;
    }
}
