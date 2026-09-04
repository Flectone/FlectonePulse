package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.constant.PlatformType;
import net.flectone.pulse.converter.AdventureHoverConverter;
import net.flectone.pulse.converter.IconConverter;
import net.flectone.pulse.decorator.ComponentDecorator;
import net.flectone.pulse.exception.ReflectionException;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.provider.PaperItemNameProvider;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.serializer.ComponentSerializer;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.LazyInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.CachedServerIcon;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitServerAdapter implements PlatformServerAdapter {

    private static final String UNKNOWN_TPS = "";

    private final LazyInstance<FPlayerService> fPlayerService;
    private final LazyInstance<MessagePipeline> messagePipeline;
    private final LazyInstance<SocialService> socialService;
    private final MinecraftPacketProvider packetProvider;
    private final AdventureHoverConverter adventureHoverConverter;
    private final ReflectionResolver reflectionResolver;
    private final PaperItemNameProvider paperItemNameProvider;
    private final TaskScheduler taskScheduler;
    private final ComponentDecorator componentDecorator;
    private final ComponentSerializer componentSerializer;
    private final IconConverter iconConverter;

    private String serverIcon;
    private Pair<MethodHandle, Object> getTPSMethodPair;
    private Boolean isModernItemStack;

    @Override
    public void dispatchCommand(@NonNull String command) {
        taskScheduler.runSync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    @Override
    public @NonNull String getTPS(FEntity entity) {
        if (reflectionResolver.isFolia()) {
            FPlayer regionFPlayer = entity instanceof FPlayer fPlayer && Bukkit.getPlayer(entity.uuid()) != null
                    ? fPlayer
                    : fPlayerService.get().getRandomFPlayer();

            CompletableFuture<String> completableFuture = new CompletableFuture<>();

            taskScheduler.runRegion(regionFPlayer, () -> completableFuture.complete(getTPS()));

            return taskScheduler.await(completableFuture, UNKNOWN_TPS, "TPS of the region of " + regionFPlayer.name());
        }

        return getTPS();
    }

    private String getTPS() {
        if (getTPSMethodPair == null) {
            getTPSMethodPair = findGetTPSMethod();
        }

        try {
            double[] recentTps = (double[]) getTPSMethodPair.getLeft().invoke(getTPSMethodPair.getRight());
            double tps = Math.min(Math.round(recentTps[0] * 10.0) / 10.0, 20.0);
            return String.valueOf(tps);
        } catch (Throwable _) {
            return UNKNOWN_TPS;
        }
    }

    private Pair<MethodHandle, Object> findGetTPSMethod() {
        Object minecraftServer = Bukkit.getServer();
        MethodHandle getTPS = reflectionResolver.unreflectMethod(Server.class, "getTPS");
        if (getTPS == null) {
            try {
                minecraftServer = getLegacyMinecraftServer();
                Field recentTpsField = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
                getTPS = reflectionResolver.unreflect(lookup -> lookup.unreflectGetter(recentTpsField));
            } catch (ReflectiveOperationException e) {
                throw new ReflectionException("Cannot read the server TPS field", e);
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
        } catch (NoSuchFieldException _) {
            Method getServerMethod = server.getClass().getMethod("getServer");
            return getServerMethod.invoke(server);
        }
    }

    @Override
    public @NonNull JsonElement getMOTD() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", Bukkit.getServer().getMotd());
        return jsonObject;
    }

    @Override
    public @Nullable String getIcon() {
        if (serverIcon == null) {
            // empty string is an indicator that it is already initialized
            serverIcon = getServerIcon().orElse("");
        }

        return StringUtils.isNotEmpty(serverIcon) ? serverIcon : null;
    }

    @Override
    public @NonNull File getWhitelistFile() {
        return new File(Bukkit.getWorldContainer(), "whitelist.json");
    }

    private Optional<String> getServerIcon() {
        CachedServerIcon cachedServerIcon = Bukkit.getServerIcon();
        if (cachedServerIcon == null) return Optional.empty();

        File iconFile = new File(Bukkit.getWorldContainer(), "server-icon.png");
        if (!iconFile.exists()) return Optional.empty();

        return Optional.ofNullable(iconConverter.convert(iconFile));
    }

    @Override
    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public int getOnlinePlayerCount() {
        FPlayerService fPlayerServiceInstance = fPlayerService.get();
        SocialService socialServiceInstance = socialService.get();
        return (int) fPlayerServiceInstance.getOnlineFPlayers().stream()
                .filter(fPlayer -> !fPlayer.isUnknown())
                .filter(fPlayer -> !socialServiceInstance.isVanished(fPlayer))
                .count();
    }

    @Override
    public int getPlatformPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public int generateEntityId() {
        return SpigotReflectionUtil.generateEntityId();
    }

    @Override
    public @NonNull String getServerCore() {
        return Bukkit.getServer().getName();
    }

    @Override
    public @NonNull String getServerUUID() {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) return "";

        return worlds.getFirst().getUID().toString();
    }

    @Override
    public String getServerVersionName() {
        return packetProvider.getApi().getServerManager().getVersion().getReleaseName();
    }

    @Override
    public @NonNull PlatformType getPlatformType() {
        return PlatformType.BUKKIT;
    }

    @Override
    public boolean hasProject(@NonNull String projectName) {
        return Bukkit.getPluginManager().isPluginEnabled(projectName);
    }

    @Override
    public boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode();
    }

    @Override
    public boolean isOnlyPlayerOnline(UUID uuid) {
        return Bukkit.getOnlinePlayers().stream().allMatch(player -> player.getUniqueId().equals(uuid));
    }

    @Override
    public boolean isPrimaryThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public @NonNull ItemStack buildItemStack(@NonNull FPlayer fPlayer, @NonNull String material, @NonNull String title, @NonNull String lore) {
        String[] stringsLore = lore.split("<br>");

        return buildItemStack(fPlayer, material, title, stringsLore.length == 0 ? new String[]{lore} : stringsLore);
    }

    @Override
    public @NonNull ItemStack buildItemStack(@NonNull FPlayer fPlayer, @NonNull String material, @NonNull String title, String[] lore) {
        Material itemMaterial;
        try {
            itemMaterial = Material.valueOf(material);
        } catch (IllegalArgumentException _) {
            itemMaterial = Material.DIAMOND_BLOCK;
        }

        Component componentName = buildItemNameComponent(fPlayer, title);

        MessagePipeline messagePipelineInstance = messagePipeline.get();
        List<Component> componentLore = lore.length == 0
                ? List.of()
                : Arrays.stream(lore)
                .map(message -> messagePipelineInstance
                        .build(MessageContext.builder()
                                .sender(fPlayer)
                                .message(message)
                                .build()
                        )
                        .decoration(TextDecoration.ITALIC, false)
                )
                .toList();

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            return buildModernItemStack(itemMaterial, componentName, componentLore);
        }

        return buildLegacyItemStack(itemMaterial, componentName, componentLore);
    }

    private @NonNull Component buildItemNameComponent(@NonNull FPlayer fPlayer, @NonNull String title) {
        if (title.isEmpty()) return Component.empty();

        return messagePipeline.get().build(MessageContext.builder()
                .sender(fPlayer)
                .message(title)
                .build()
        );
    }

    private @NonNull ItemStack buildModernItemStack(@NonNull Material material, @NonNull Component name, @NonNull List<Component> lore) {
        return new ItemStack.Builder()
                .type(SpigotConversionUtil.fromBukkitItemMaterial(material))
                .component(ComponentTypes.ITEM_NAME, name)
                .component(ComponentTypes.LORE, new ItemLore(lore))
                .build();
    }

    private @NonNull ItemStack buildLegacyItemStack(@NonNull Material material, @NonNull Component name, @NonNull List<Component> lore) {
        org.bukkit.inventory.ItemStack legacyItem = new org.bukkit.inventory.ItemStack(material);
        ItemMeta meta = legacyItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(componentSerializer.toLegacy(name));
            meta.setLore(lore.stream()
                    .map(componentSerializer::toLegacy)
                    .toList()
            );

            legacyItem.setItemMeta(meta);
        }

        return SpigotConversionUtil.fromBukkitItemStack(legacyItem);
    }

    @Override
    public @NonNull String getItemName(@NonNull Object itemStack) {
        if (!(itemStack instanceof org.bukkit.inventory.ItemStack bukkitItem)) {
            return "";
        }

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
            return getModernItemName(bukkitItem.getType());
        }

        if (packetProvider.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
            return getLegacyItemName(bukkitItem);
        }

        return getLegacyItemName(bukkitItem) + ".name";
    }

    @Override
    public @NonNull Component translateItemName(@NonNull Object item, @NonNull UUID messageUUID, boolean translatable) {
        if (!(item instanceof org.bukkit.inventory.ItemStack itemStack)) return Component.empty();

        Component component = itemStack.getItemMeta() == null
                || itemStack.getItemMeta().getDisplayName() == null // support legacy versions
                || itemStack.getItemMeta().getDisplayName().isEmpty()
                ? createTranslatableItemName(itemStack, translatable)
                : createItemMetaName(itemStack);

        if (itemStack.getType() != Material.AIR) {
            ItemStack packetItemStack = SpigotConversionUtil.fromBukkitItemStack(itemStack);
            return componentDecorator.hoverIfAbsent(component, adventureHoverConverter.convert(packetItemStack));
        }

        return component;
    }

    private Component createItemMetaName(org.bukkit.inventory.ItemStack itemStack) {
        // lazy init
        if (isModernItemStack == null) {
            isModernItemStack = reflectionResolver.hasMethod(org.bukkit.inventory.ItemStack.class, "displayName");
        }

        if (isModernItemStack) {
            String jsonDisplayName = paperItemNameProvider.get(itemStack);
            if (jsonDisplayName != null) {
                return componentDecorator.decorateIfAbsent(componentSerializer.fromJson(jsonDisplayName), TextDecoration.ITALIC, TextDecoration.State.TRUE);
            }
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta == null ? null : itemMeta.getDisplayName();
        if (displayName == null) return Component.empty();

        MessagePipeline messagePipelineInstance = messagePipeline.get();
        String clearedDisplayName = messagePipelineInstance.buildPlain(MessageContext.builder()
                .message(displayName)
                .build()
        );

        return Component.text(clearedDisplayName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.TRUE);
    }

    private Component createTranslatableItemName(org.bukkit.inventory.ItemStack itemStack, boolean translatable) {
        String itemName = getItemName(itemStack);
        Component itemComponent = Component.translatable(itemName);

        return translatable ? itemComponent : GlobalTranslator.render(itemComponent, Locale.ROOT);
    }

    private @NonNull String getModernItemName(@NonNull Material material) {
        return (material.isBlock() ? "block" : "item") + ".minecraft." + material.toString().toLowerCase();
    }

    private @NonNull String getLegacyItemName(org.bukkit.inventory.@NonNull ItemStack itemStack) {
        try {
            Object nmsStack = itemStack.getClass()
                    .getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class)
                    .invoke(null, itemStack);

            Object item = nmsStack.getClass().getMethod("getItem").invoke(nmsStack);
            return (String) item.getClass().getMethod("getName").invoke(item);
        } catch (Exception _) {
            return "";
        }
    }

}