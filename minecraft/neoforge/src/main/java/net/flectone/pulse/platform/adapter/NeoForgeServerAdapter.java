package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.NeoForgeFlectonePulse;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.processing.converter.IconConvertor;
import net.flectone.pulse.processing.convertor.AdventureHoverConvertor;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.NeoForgeTpsTracker;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.decorator.ComponentDecorator;
import net.flectone.pulse.util.generator.RandomGenerator;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NeoForgeServerAdapter implements PlatformServerAdapter {

    private final NeoForgeFlectonePulse neoForgeFlectonePulse;
    private final Provider<FPlayerService> fPlayerServiceProvider;
    private final Provider<MessagePipeline> messagePipelineProvider;
    private final Provider<SocialService> socialServiceProvider;
    private final MinecraftPacketProvider packetProvider;
    private final AdventureHoverConvertor adventureHoverConvertor;
    private final @Named("projectPath") Path projectPath;
    private final NeoForgeTpsTracker tpsTracker;
    private final FLogger fLogger;
    private final ComponentDecorator componentDecorator;
    private final RandomGenerator randomGenerator;
    private final IconConvertor iconConvertor;

    private String serverIcon;

    @Override
    public void dispatchCommand(@NonNull String command) {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return;

        try {
            minecraftServer.getCommands().getDispatcher().execute(command, minecraftServer.createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            fLogger.warning(e);
        }
    }

    @Override
    public @NonNull String getTPS(FEntity entity) {
        return String.format("%.2f", tpsTracker.getTPS());
    }

    @Override
    public int getMaxPlayers() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return 0;

        return minecraftServer.getMaxPlayers();
    }

    @Override
    public int getOnlinePlayerCount() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return 0;

        return (int) fPlayerServiceProvider.get().getOnlineFPlayers().stream()
                    .filter(fPlayer -> !fPlayer.isUnknown())
                    .filter(fPlayer -> !socialServiceProvider.get().isVanished(fPlayer))
                    .count();
    }

    @Override
    public int getPlatformPlayerCount() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        return minecraftServer != null ? minecraftServer.getPlayerCount() : 0;
    }

    @Override
    public int generateEntityId() {
        return randomGenerator.nextInt(Integer.MAX_VALUE);
    }

    @Override
    public @NonNull String getServerCore() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return "neoforge";

        return minecraftServer.getServerModName();
    }

    @Override
    public @NonNull String getServerUUID() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return "";

        for (ServerLevel serverLevel : minecraftServer.getAllLevels()) {
            return UUID.nameUUIDFromBytes(String.valueOf(serverLevel.getSeed()).getBytes(StandardCharsets.UTF_8)).toString();
        }

        return "";
    }

    @Override
    public String getServerVersionName() {
        return packetProvider.getApi().getServerManager().getVersion().getReleaseName();
    }

    @Override
    public @NonNull PlatformType getPlatformType() {
        return PlatformType.NEOFORGE;
    }

    @Override
    public @NonNull JsonElement getMOTD() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return new JsonObject();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", minecraftServer.getMotd());
        return jsonObject;
    }

    @Override
    public @Nullable String getIcon() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return null;

        if (serverIcon == null) {
            File iconFile = minecraftServer.getFile("server-icon.png").toFile();

            if (iconFile.exists()) {
                serverIcon = iconConvertor.convert(iconFile);
            }

            // empty string is an indicator that it is already initialized
            if (serverIcon == null) {
                serverIcon = "";
            }

        }

        return StringUtils.isNotEmpty(serverIcon) ? serverIcon : null;
    }

    @Override
    public @NonNull File getWhitelistFile() {
        return neoForgeFlectonePulse.getModContainer().getModInfo().getOwningFile().getFile().getFilePath().getParent().resolve("whitelist.json").toFile();
    }

    @Override
    public boolean hasProject(@NonNull String projectName) {
        return ModList.get().isLoaded(projectName.toLowerCase());
    }

    @Override
    public boolean isOnlineMode() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return minecraftServer.usesAuthentication();
    }

    @Override
    public boolean isOnlyPlayerOnline(UUID uuid) {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        List<ServerPlayer> onlinePlayers = minecraftServer.getPlayerList().getPlayers();
        if (onlinePlayers.isEmpty()) return true;

        return onlinePlayers.stream().allMatch(serverPlayer -> serverPlayer.getUUID().equals(uuid));
    }

    @Override
    public boolean isPrimaryThread() {
        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer == null) return false;

        return minecraftServer.isSameThread();
    }

    @Override
    public @NonNull String getItemName(@NonNull Object item) {
        return item instanceof net.minecraft.world.item.ItemStack itemStack ? itemStack.getItemName().getString() : "";
    }

    @Override
    public @Nullable InputStream getResource(@NonNull String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    @Override
    public void saveResource(@NonNull String path) {
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
    public @NonNull Component translateItemName(@NonNull Object item, @NonNull UUID messageUUID, boolean translatable) {
        if (!(item instanceof net.minecraft.world.item.ItemStack itemStack)) return Component.empty();
        if (getItemName(item).equalsIgnoreCase("air")) return Component.translatable("block.minecraft.air");

        Component component = itemStack.getCustomName() == null
                || itemStack.getCustomName().getString().isBlank()
                ? createTranslatableItemName(itemStack, translatable)
                : createItemMetaName(itemStack);

        MinecraftServer minecraftServer = neoForgeFlectonePulse.getMinecraftServer();
        if (minecraftServer != null) {
            // convert neoforge itemStack to packetevents
            ItemStack packetItemStack = fromMinecraftStack(itemStack, minecraftServer.registryAccess());

            // translation for checking component
            String translationKey = getTranslationKey(itemStack);

            // first try custom name
            Optional<Component> itemName = packetItemStack.getComponent(ComponentTypes.CUSTOM_NAME);
            if (itemName.isPresent() && !isTranslatableItemComponent(itemName.get(), translationKey)) {
                component = itemName.get();
            } else {
                // else try item name
                itemName = packetItemStack.getComponent(ComponentTypes.ITEM_NAME);
                if (itemName.isPresent() && !isTranslatableItemComponent(itemName.get(), translationKey)) {
                    component = itemName.get();
                }
            }

            // final translatable component
            if (!isTranslatableItemComponent(component, translationKey)) {
                component = componentDecorator.decorateIfAbsent(component, TextDecoration.ITALIC, TextDecoration.State.TRUE);
            }

            return componentDecorator.hoverIfAbsent(component, adventureHoverConvertor.convert(packetItemStack));
        }

        Key key = Key.key(itemStack.getItem().builtInRegistryHolder().key().identifier().getPath());
        return componentDecorator.hoverIfAbsent(component, HoverEvent.showItem(key, itemStack.getCount()));
    }

    private boolean isTranslatableItemComponent(Component component, String translationKey) {
        return component instanceof TranslatableComponent translatableComponent && translatableComponent.key().equals(translationKey);
    }

    // https://github.com/retrooper/packetevents/pull/1147/changes#diff-9647df572bdd365fa3ce0333c7491ea491ee6b602bfadcb0f46d8660b580f419R142
    private ItemStack fromMinecraftStack(net.minecraft.world.item.ItemStack stack, RegistryAccess registries) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
        try {
            net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(
                    new RegistryFriendlyByteBuf(buf, registries), stack);
            return PacketWrapper.createUniversalPacketWrapper(buf).readItemStack();
        } finally {
            buf.release();
        }
    }

    private Component createItemMetaName(net.minecraft.world.item.ItemStack itemStack) {
        net.minecraft.network.chat.Component customName = itemStack.getCustomName();
        if (customName == null) return Component.empty();

        String clearedDisplayName = messagePipelineProvider.get().buildPlain(MessageContext.builder()
                .message(customName.getString())
                .build()
        );

        return Component.text(clearedDisplayName).decorate(TextDecoration.ITALIC);
    }

    private Component createTranslatableItemName(net.minecraft.world.item.ItemStack itemStack, boolean translatable) {
        Component itemComponent = Component.translatable(getTranslationKey(itemStack));

        return translatable
                ? itemComponent
                : GlobalTranslator.render(itemComponent, Locale.ROOT);
    }

    private String getTranslationKey(net.minecraft.world.item.ItemStack itemStack) {
        return itemStack.getItem().getDescriptionId();
    }

    @Override
    public @NonNull ItemStack buildItemStack(@NonNull FPlayer fPlayer, @NonNull String material, @NonNull String title, @NonNull String lore) {
        String[] stringsLore = lore.split("<br>");

        return buildItemStack(fPlayer, material, title, stringsLore.length == 0 ? new String[]{lore} : stringsLore);
    }

    @Override
    public @NonNull ItemStack buildItemStack(@NonNull FPlayer fPlayer, @NonNull String material, @NonNull String title, String[] lore) {
        ItemType itemMaterial = ItemTypes.getByName(material.toLowerCase());
        if (itemMaterial == null) {
            itemMaterial = ItemTypes.DIAMOND_BLOCK;
        }

        Component componentName = buildItemNameComponent(fPlayer, title);

        List<Component> componentLore = lore.length == 0
                ? List.of()
                : Arrays.stream(lore)
                .map(message -> messagePipelineProvider.get()
                                .build(MessageContext.builder()
                                       .sender(fPlayer)
                                       .message(message)
                                       .build()
                                )
                                .decoration(TextDecoration.ITALIC, false)
                )
                .toList();

        return new ItemStack.Builder()
                .type(itemMaterial)
                .component(ComponentTypes.ITEM_NAME, componentName)
                .component(ComponentTypes.LORE, new ItemLore(componentLore))
                .build();
    }

    private @NonNull Component buildItemNameComponent(@NonNull FPlayer fPlayer, @NonNull String title) {
        if (title.isEmpty()) return Component.empty();

        return messagePipelineProvider.get().build(MessageContext.builder()
                .sender(fPlayer)
                .message(title)
                .build()
        );
    }
}
