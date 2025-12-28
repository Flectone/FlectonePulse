package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.PlatformType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.util.UUID;

/**
 * Platform adapter for server-related operations in FlectonePulse.
 * Abstracts platform-specific server APIs for cross-platform compatibility.
 *
 * @author TheFaser
 * @since 0.8.1
 */
public interface PlatformServerAdapter {

    /**
     * Dispatches a command to the server console.
     *
     * @param command the command string to execute
     */
    void dispatchCommand(@NonNull String command);

    /**
     * Gets the current server TPS (ticks per second).
     *
     * @return formatted TPS string or empty string if unavailable
     */
    @NonNull String getTPS();

    /**
     * Gets the maximum player count for the server.
     *
     * @return maximum allowed players
     */
    int getMaxPlayers();

    /**
     * Gets the current online player count.
     *
     * @return number of online players
     */
    int getOnlinePlayerCount();

    /**
     * Generates a new entity ID.
     *
     * @return entity ID
     */
    int generateEntityId();

    /**
     * Returns the name of the server core.
     *
     * @return a string representing the name of the server core
     */
    @NonNull String getServerCore();

    /**
     * Returns server UUID.
     *
     * @return a string representing server UUID
     */
    @NonNull String getServerUUID();

    /**
     * Gets the platform type.
     *
     * @return the platform type
     */
    @NonNull PlatformType getPlatformType();

    /**
     * Gets the server MOTD.
     *
     * @return MOTD as JsonElement
     */
    @NonNull JsonElement getMOTD();

    /**
     * Checks if a project/resource is available on the server.
     *
     * @param projectName name of the project/resource to check
     * @return true if the project is available, false otherwise
     */
    boolean hasProject(@NonNull String projectName);

    /**
     * Checks if the server is currently in online mode.
     *
     * @return true if the server is in online mode, false otherwise
     */
    boolean isOnlineMode();

    /**
     * Checks if the current thread is the primary server thread.
     *
     * @return true if on primary thread
     */
    boolean isPrimaryThread();

    /**
     * Gets the Minecraft name of an item.
     *
     * @param item the platform-specific item object
     * @return localized item name or empty string if invalid
     */
    @NonNull String getItemName(@NonNull Object item);

    /**
     * Gets an input stream for the specified resource path.
     *
     * @param path resource path (classpath relative)
     * @return InputStream or null if not found
     */
    @Nullable InputStream getResource(@NonNull String path);

    /**
     * Saves a resource to the plugin data folder.
     *
     * @param path the resource path
     */
    void saveResource(@NonNull String path);

    /**
     * Translates an item name with optional localization.
     *
     * @param item the platform-specific item object
     * @param messageUUID the message UUID for caching
     * @param translatable whether the name should be translated
     * @return translated item name component
     */
    @NonNull Component translateItemName(@NonNull Object item, @NonNull UUID messageUUID, boolean translatable);

    /**
     * Builds an item stack with lore.
     *
     * @param fPlayer the player receiving the item
     * @param material the item material
     * @param title the item title
     * @param lore the item lore
     * @return the built item stack
     */
    @NonNull ItemStack buildItemStack(
            @NonNull FPlayer fPlayer,
            @NonNull String material,
            @NonNull String title,
            @NonNull String lore
    );

    /**
     * Builds an item stack with lore.
     *
     * @param fPlayer the player receiving the item
     * @param material the item material
     * @param title the item title
     * @param lore the item lore lines
     * @return the built item stack
     */
    @NonNull ItemStack buildItemStack(
            @NonNull FPlayer fPlayer,
            @NonNull String material,
            @NonNull String title,
            @NonNull String[] lore
    );
}