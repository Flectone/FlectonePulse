package net.flectone.pulse.platform.adapter;

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
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.PaperItemStackUtil;
import net.flectone.pulse.util.constant.PlatformType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.type.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitServerAdapter implements PlatformServerAdapter {

    private final Plugin plugin;
    private final Provider<IntegrationModule> integrationModuleProvider;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<MessagePipeline> messagePipelineProvider;
    private final PacketProvider packetProvider;
    private final ReflectionResolver reflectionResolver;

    private Pair<MethodHandle, Object> getTPSMethodPair;

    @Sync
    @Override
    public void dispatchCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public @NotNull String getTPS() {
        if (getTPSMethodPair == null) {
            getTPSMethodPair = findGetTPSMethod();
        }

        try {
            double[] recentTps = (double[]) getTPSMethodPair.first().invoke(getTPSMethodPair.second());
            double tps = Math.min(Math.round(recentTps[0] * 10.0) / 10.0, 20.0);
            return String.valueOf(tps);
        } catch (Throwable ignored) {
            return "";
        }
    }

    public Pair<MethodHandle, Object> findGetTPSMethod() {
        Object minecraftServer = Bukkit.getServer();
        MethodHandle getTPS = reflectionResolver.unreflectMethod(Server.class, "getTPS");
        if (getTPS == null) {
            try {
                minecraftServer = getLegacyMinecraftServer();
                Field recentTpsField = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
                getTPS = reflectionResolver.unreflect(lookup -> lookup.unreflectGetter(recentTpsField));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        return Pair.of(getTPS, minecraftServer);
    }

    private Object getLegacyMinecraftServer() throws ReflectiveOperationException {
        Server server = Bukkit.getServer();
        try {
            Field consoleField = server.getClass().getDeclaredField("console");
            consoleField.setAccessible(true);
            return consoleField.get(server);
        } catch (NoSuchFieldException e) {
            Method getServerMethod = server.getClass().getMethod("getServer");
            return getServerMethod.invoke(server);
        }
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
        return (int) fPlayerServiceProvider.get().getOnlineFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isUnknown())
                .filter(fPlayer -> !integrationModuleProvider.get().isVanished(fPlayer))
                .count();
    }

    @Override
    public @NotNull String getServerCore() {
        return Bukkit.getServer().getName();
    }

    @Override
    public @NotNull String getServerUUID() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) return "";

        return worlds.getFirst().getUID().toString();
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.BUKKIT;
    }

    @Override
    public boolean hasProject(String projectName) {
        return Bukkit.getPluginManager().getPlugin(projectName) != null;
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode();
    }

    @Override
    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public @NotNull ItemStack buildItemStack(FPlayer fPlayer, String material, String title, String lore) {
        String[] stringsLore = lore.split("<br>");

        return buildItemStack(fPlayer, material, title, stringsLore.length == 0 ? new String[]{lore} : stringsLore);
    }

    @Override
    public @NotNull ItemStack buildItemStack(FPlayer fPlayer, String material, String title, String[] lore) {
        Material itemMaterial;
        try {
            itemMaterial = Material.valueOf(material);
        } catch (IllegalArgumentException e) {
            itemMaterial = Material.DIAMOND_BLOCK;
        }

        Component componentName = buildItemNameComponent(fPlayer, title);

        List<Component> componentLore = lore.length == 0
                ? Collections.emptyList()
                : Arrays.stream(lore)
                .map(message -> messagePipelineProvider.get().builder(fPlayer, message).build().decoration(TextDecoration.ITALIC, false))
                .toList();

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            return buildModernItemStack(itemMaterial, componentName, componentLore);
        }

        return buildLegacyItemStack(itemMaterial, componentName, componentLore);
    }

    private @NotNull Component buildItemNameComponent(@NotNull FPlayer fPlayer, @NotNull String title) {
        return title.isEmpty()
                ? Component.empty()
                : messagePipelineProvider.get().builder(fPlayer, title).build();
    }

    private @NotNull ItemStack buildModernItemStack(@NotNull Material material, @NotNull Component name, @NotNull List<Component> lore) {
        return new ItemStack.Builder()
                .type(SpigotConversionUtil.fromBukkitItemMaterial(material))
                .component(ComponentTypes.ITEM_NAME, name)
                .component(ComponentTypes.LORE, new ItemLore(lore))
                .build();
    }

    private @NotNull ItemStack buildLegacyItemStack(@NotNull Material material, @NotNull Component name, @NotNull List<Component> lore) {
        org.bukkit.inventory.ItemStack legacyItem = new org.bukkit.inventory.ItemStack(material);
        ItemMeta meta = legacyItem.getItemMeta();

        LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.legacySection();

        meta.setDisplayName(legacyComponentSerializer.serialize(name));
        meta.setLore(lore.stream()
                .map(component -> legacyComponentSerializer.serialize(name))
                .toList());

        legacyItem.setItemMeta(meta);
        return SpigotConversionUtil.fromBukkitItemStack(legacyItem);
    }

    @Override
    public @NotNull String getItemName(@Nullable Object itemStack) {
        if (!(itemStack instanceof org.bukkit.inventory.ItemStack bukkitItem)) {
            return "";
        }

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
            return getModernItemName(bukkitItem.getType());
        }

        return getLegacyItemName(bukkitItem);
    }

    @Override
    public @Nullable InputStream getResource(String path) {
        return plugin.getResource(path);
    }

    @Override
    public void saveResource(String path) {
        plugin.saveResource(path, false);
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

        Component component = itemStack.getItemMeta() == null
                || itemStack.getItemMeta().getDisplayName() == null // support legacy versions
                || itemStack.getItemMeta().getDisplayName().isEmpty()
                ? createTranslatableItemName(itemStack, translatable)
                : createItemMetaName(itemStack);

        if (itemStack.getType() == Material.AIR) return component;

        Key key = Key.key(itemStack.getType().name().toLowerCase());
        int amount = itemStack.getAmount();

        try {
            // it's not really working full
            // data components are not displayed
            // waiting for PacketEvents to implement this https://github.com/retrooper/packetevents/pull/1277
            if (reflectionResolver.isPaper()) {
                HoverEvent.ShowItem showItem = HoverEvent.ShowItem.showItem(key, amount, PaperItemStackUtil.getDataComponents(itemStack));
                return component.hoverEvent(HoverEvent.showItem(showItem));
            }
        } catch (Exception ignored) {
            // ignore incorrect hover
        }

        return component.hoverEvent(HoverEvent.showItem(key, amount));
    }

    private Component createItemMetaName(org.bukkit.inventory.ItemStack itemStack) {
        String displayName = itemStack.getItemMeta().getDisplayName();
        if (displayName == null) return Component.empty();

        Component componentName = messagePipelineProvider.get().builder(displayName).build();
        String clearedDisplayName = PlainTextComponentSerializer.plainText().serialize(componentName);

        return Component.text(clearedDisplayName).decorate(TextDecoration.ITALIC);
    }

    private Component createTranslatableItemName(org.bukkit.inventory.ItemStack itemStack, boolean translatable) {
        String itemName = getItemName(itemStack);
        Component itemComponent = Component.translatable(itemName);

        return translatable
                ? itemComponent
                : GlobalTranslator.render(itemComponent, Locale.ROOT);
    }
}
