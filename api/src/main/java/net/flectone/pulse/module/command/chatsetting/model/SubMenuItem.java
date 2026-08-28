package net.flectone.pulse.module.command.chatsetting.model;

import net.flectone.pulse.config.setting.PermissionSetting;

import java.util.Map;

/**
 * Represents an item in the chatsetting submenu.
 *
 * @param name the item name
 * @param material the item material
 * @param colors the map of color indexes to color values
 * @param perm the permission setting for this item
 */
public record SubMenuItem(
        String name,
        String material,
        Map<Integer, String> colors,
        PermissionSetting perm
) {
}