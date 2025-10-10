package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.fabricmc.loader.api.FabricLoader;
import net.flectone.pulse.FabricFlectonePulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.TpsTracker;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FabricServerAdapter implements PlatformServerAdapter {

    private final FabricFlectonePulse fabricFlectonePulse;
    private final Provider<MessagePipeline> messagePipelineProvider;
    private final @Named("projectPath") Path projectPath;
    private final TpsTracker tpsTracker;
    private final FLogger fLogger;

    @Override
    public void dispatchCommand(String command) {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return;

        try {
            minecraftServer.getCommandManager().getDispatcher().execute(command, minecraftServer.getCommandSource());
        } catch (CommandSyntaxException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public @NotNull String getTPS() {
        return String.format("%.2f", tpsTracker.getTPS());
    }

    @Override
    public int getMaxPlayers() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return 0;

        return minecraftServer.getMaxPlayerCount();
    }

    @Override
    public int getOnlinePlayerCount() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return 0;

        return minecraftServer.getCurrentPlayerCount();
    }

    @Override
    public @NotNull String getServerCore() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return "fabric";

        return minecraftServer.getServerModName();
    }

    @Override
    public @NotNull String getServerUUID() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return "";

        for (ServerWorld serverWorld : minecraftServer.getWorlds()) {
            return serverWorld.getRegistryKey().getValue().toString();
        }

        return "";
    }

    @Override
    public PlatformType getPlatformType() {
        return PlatformType.FABRIC;
    }

    @Override
    public @NotNull JsonElement getMOTD() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return new JsonObject();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", minecraftServer.getServerMotd());
        return jsonObject;
    }

    @Override
    public boolean hasProject(String projectName) {
        return FabricLoader.getInstance().isModLoaded(projectName.toLowerCase());
    }

    @Override
    public boolean isOnlineMode() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return minecraftServer.isOnlineMode();
    }

    @Override
    public boolean isPrimaryThread() {
        MinecraftServer minecraftServer = fabricFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return minecraftServer.isOnThread();
    }

    @Override
    public @NotNull String getItemName(Object item) {
        return item instanceof net.minecraft.item.ItemStack itemStack ? itemStack.getItemName().getString() : "";
    }

    @Override
    public @Nullable InputStream getResource(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    @Override
    public void saveResource(String path) {
        InputStream resource = getResource(path);
        if (resource == null) return;

        try {
            Path targetPath = projectPath.resolve(path);

            if (Files.exists(targetPath)) {
                return;
            }

            Path parentDir = targetPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            Files.copy(resource, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            fLogger.warning(e);
        }
    }

    @Override
    public @NotNull Component translateItemName(Object item, boolean translatable) {
        if (!(item instanceof net.minecraft.item.ItemStack itemStack)) return Component.empty();
        if (getItemName(item).equalsIgnoreCase("air")) return Component.translatable("block.minecraft.air");

        Component component = itemStack.getCustomName() == null
                || itemStack.getCustomName().getString().isBlank()
                ? createTranslatableItemName(itemStack, translatable)
                : createItemMetaName(itemStack);

        Key key = Key.key(itemStack.getRegistryEntry().getIdAsString());
        return component.hoverEvent(HoverEvent.showItem(key, itemStack.getCount()));
    }


    private Component createItemMetaName(net.minecraft.item.ItemStack itemStack) {
        String displayName = itemStack.getCustomName().getString();
        if (displayName == null) return Component.empty();

        Component componentName = messagePipelineProvider.get().builder(displayName).build();
        String clearedDisplayName = PlainTextComponentSerializer.plainText().serialize(componentName);

        return Component.text(clearedDisplayName).decorate(TextDecoration.ITALIC);
    }

    private Component createTranslatableItemName(net.minecraft.item.ItemStack itemStack, boolean translatable) {
        Component itemComponent = Component.translatable(itemStack.getItem().getTranslationKey());

        return translatable
                ? itemComponent
                : GlobalTranslator.render(itemComponent, Locale.ROOT);
    }

    @Override
    public @NotNull ItemStack buildItemStack(FPlayer fPlayer, String material, String title, String lore) {
        String[] stringsLore = lore.split("<br>");

        return buildItemStack(fPlayer, material, title, stringsLore.length == 0 ? new String[]{lore} : stringsLore);
    }

    @Override
    public @NotNull ItemStack buildItemStack(FPlayer fPlayer, String material, String title, String[] lore) {
        ItemType itemMaterial = ItemTypes.getByName(material.toLowerCase());
        if (itemMaterial == null) {
            itemMaterial = ItemTypes.DIAMOND_BLOCK;
        }

        Component componentName = buildItemNameComponent(fPlayer, title);

        List<Component> componentLore = lore.length == 0
                ? Collections.emptyList()
                : Arrays.stream(lore)
                .map(message -> messagePipelineProvider.get().builder(fPlayer, message).build().decoration(TextDecoration.ITALIC, false))
                .toList();

        return new ItemStack.Builder()
                .type(itemMaterial)
                .component(ComponentTypes.ITEM_NAME, componentName)
                .component(ComponentTypes.LORE, new ItemLore(componentLore))
                .build();
    }

    private @NotNull Component buildItemNameComponent(@NotNull FPlayer fPlayer, @NotNull String title) {
        return title.isEmpty()
                ? Component.empty()
                : messagePipelineProvider.get().builder(fPlayer, title).build();
    }
}
