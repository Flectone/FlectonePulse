package net.flectone.pulse.platform.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.RandomUtil;
import net.flectone.pulse.util.constant.PlatformType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class HytaleServerAdapter implements PlatformServerAdapter {

    private final RandomUtil randomUtil;
    private final @Named("projectPath") Path projectPath;
    private final FLogger fLogger;

    @Override
    public void dispatchCommand(@NonNull String command) {
        HytaleServer hytaleServer = HytaleServer.get();
        if (hytaleServer == null) return;

        hytaleServer.getCommandManager().handleCommand(ConsoleSender.INSTANCE, command);
    }

    @Override
    public @NonNull String getTPS() {
        Universe universe = Universe.get();
        if (universe == null) return "";

        World world = universe.getDefaultWorld();
        if (world == null) return "";

        return String.valueOf(world.getTps());
    }

    @Override
    public int getMaxPlayers() {
        HytaleServer hytaleServer = HytaleServer.get();
        if (hytaleServer == null) return -1;

        return hytaleServer.getConfig().getMaxPlayers();
    }

    @Override
    public int getOnlinePlayerCount() {
        Universe universe = Universe.get();
        return universe == null ? 0 : universe.getPlayerCount();
    }

    @Override
    public int generateEntityId() {
        return randomUtil.nextInt(Integer.MAX_VALUE);
    }

    @Override
    public @NonNull String getServerCore() {
        return "hytale";
    }

    @Override
    public @NonNull String getServerUUID() {
        Universe universe = Universe.get();
        if (universe == null) return UUID.randomUUID().toString();

        World world = universe.getDefaultWorld();
        if (world != null) {
            return world.getWorldConfig().getUuid().toString();
        }

        Collection<World> worlds = universe.getWorlds().values();
        if (worlds.isEmpty()) return UUID.randomUUID().toString();

        return worlds.iterator().next().getWorldConfig().getUuid().toString();
    }

    @Override
    public String getServerVersionName() {
        return "Hytale v" + HytaleServerConfig.VERSION;
    }

    @Override
    public @NonNull PlatformType getPlatformType() {
        return PlatformType.HYTALE;
    }

    @Override
    public @NonNull JsonElement getMOTD() {
        HytaleServer hytaleServer = HytaleServer.get();
        if (hytaleServer == null) return new JsonObject();

        String motd = hytaleServer.getConfig().getMotd();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", motd);
        return jsonObject;
    }

    @Override
    public boolean hasProject(@NonNull String projectName) {
        HytaleServer hytaleServer = HytaleServer.get();
        if (hytaleServer == null) return false;

        return hytaleServer.getPluginManager().hasPlugin(PluginIdentifier.fromString(projectName + ":" + projectName), SemverRange.WILDCARD);
    }

    @Override
    public boolean isOnlineMode() {
        return ServerAuthManager.getInstance().getAuthMode() != ServerAuthManager.AuthMode.NONE;
    }

    @Override
    public boolean isPrimaryThread() {
        // its ok?
        return false;
    }

    @Override
    public @NonNull String getItemName(@NonNull Object item) {
        if (!(item instanceof ItemStack itemStack)) return "";

        return itemStack.getItemId();
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
        if (!(item instanceof ItemStack itemStack)) return Component.empty();

        return translatable
                ? Component.translatable("server.items." + itemStack.getItemId() + ".name")
                : Component.text(itemStack.getItemId());
    }

    @Override
    public @NonNull String buildItemStack(@NonNull FPlayer fPlayer, @NonNull String material, @NonNull String title, @NonNull String lore) {
        return "";
    }

    @Override
    public @NonNull String buildItemStack(@NonNull FPlayer fPlayer, @NonNull String material, @NonNull String title, @NotNull @NonNull String[] lore) {
        return "";
    }
}
