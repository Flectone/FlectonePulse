package net.flectone.pulse.platform.adapter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.constant.PlatformType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.UUID;

public interface PlatformServerAdapter {

    /**
     * Dispatches a command to the server console
     *
     * @param command The command string to execute
     */
    void dispatchCommand(String command);

    /**
     * Gets the current server TPS (ticks per second)
     *
     * @return Formatted TPS string or empty string if unavailable
     */
    @NotNull String getTPS();

    /**
     * Gets the maximum player count for the server
     *
     * @return Maximum allowed players
     */
    int getMaxPlayers();

    /**
     * Gets the current online player count
     *
     * @return Number of online players
     */
    int getOnlinePlayerCount();

    /**
     * Return new entity id
     *
     * @return entity id
     */
    int generateEntityId();

    /**
     * Returns the name of the server core
     *
     * @return A string representing the name of the server core
     */
    @NotNull String getServerCore();

    /**
     * Returns server UUID
     *
     * @return A string representing server UUID
     */
    @NotNull String getServerUUID();

    PlatformType getPlatformType();

    /**
     * Gets the server MOTD
     *
     * @return MOTD as JsonElement
     */
    @NotNull JsonElement getMOTD();

    /**
     * Checks if a project/resource is available on the server
     *
     * @param projectName Name of the project/resource to check
     * @return true if the project is available, false otherwise
     */
    boolean hasProject(String projectName);

    /**
     * Checks if the server is currently in online mode
     *
     * @return {@code true} if the server is in online mode, {@code false} otherwise
     */
    boolean isOnlineMode();

    boolean isPrimaryThread();

    /**
     * Gets the Minecraft name of an item
     *
     * @param item The platform-specific item object
     * @return Localized item name or empty string if invalid
     */
    @NotNull String getItemName(Object item);


    /**
     * Gets an input stream for the specified resource path.
     *
     * @param path Resource path (classpath relative)
     * @return InputStream or null if not found
     */
    @Nullable InputStream getResource(String path);

    void saveResource(String path);

    @NotNull Component translateItemName(Object item, UUID messageUUID, boolean translatable);

    @NotNull ItemStack buildItemStack(
            FPlayer fPlayer,
            String material,
            String title,
            String lore
    );

    @NotNull ItemStack buildItemStack(
            FPlayer fPlayer,
            String material,
            String title,
            String[] lore
    );

}
