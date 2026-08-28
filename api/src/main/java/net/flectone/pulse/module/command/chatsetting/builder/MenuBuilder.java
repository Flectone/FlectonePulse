package net.flectone.pulse.module.command.chatsetting.builder;

import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.chatsetting.model.SubMenuItem;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The builder for constructing the chatsetting menu.
 * @author TheFaser
 */
public interface MenuBuilder {

    /**
     * Opens the main chatsetting menu for the given player.
     *
     * @param fPlayer the player
     * @param fTargetUUID the UUID of the target player whose settings are being modified
     */
    void open(FPlayer fPlayer, UUID fTargetUUID);

    /**
     * Opens a submenu within the chatsetting menu.
     *
     * @param fPlayer the player
     * @param fTargetUUID the UUID of the target player whose settings are being modified
     * @param header the submenu header
     * @param closeConsumer the callback executed when the submenu is closed
     * @param items the list of submenu items
     * @param getItemMessage the function to retrieve the display message for an item
     * @param onSelect the callback executed when an item is selected
     * @param id the unique identifier for the submenu, may be null
     */
    void openSubMenu(FPlayer fPlayer, UUID fTargetUUID, Component header, Runnable closeConsumer, List<SubMenuItem> items, Function<SubMenuItem, String> getItemMessage, Consumer<SubMenuItem> onSelect, @Nullable String id);

}