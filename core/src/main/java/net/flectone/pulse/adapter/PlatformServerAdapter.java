package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.google.gson.JsonElement;
import net.flectone.pulse.configuration.Command;
import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * Returns the name of the server core
     *
     * @return A string representing the name of the server core
     */
    @NotNull String getServerCore();

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

    /**
     * Gets the Minecraft name of an item
     *
     * @param item The platform-specific item object
     * @return Localized item name or empty string if invalid
     */
    @NotNull String getItemName(Object item);

    /**
     * Gets the translated display name of an item in Minecraft's
     *
     * @param item The platform-specific item object
     * @param translatable Use translatable item name
     * @return Localized item name or empty string if invalid
     */
    @NotNull Component translateItemName(Object item, boolean translatable);

    /**
     * Builds an ItemStack from configuration settings
     *
     * @param settingIndex The index of the setting
     * @param fPlayer The player context for the item
     * @param messages List of messages to include
     * @param settingItem The item configuration
     * @return Configured ItemStack
     */
    @NotNull ItemStack buildItemStack(
            int settingIndex,
            FPlayer fPlayer,
            List<List<String>> messages,
            Command.Chatsetting.SettingItem settingItem
    );
}
