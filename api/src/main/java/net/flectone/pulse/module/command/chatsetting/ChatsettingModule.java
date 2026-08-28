package net.flectone.pulse.module.command.chatsetting;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.module.command.chatsetting.builder.MenuBuilder;

/**
 * The /chatsetting command, which opens the menu where a player toggles their personal chat options.
 * @author TheFaser
 */
public interface ChatsettingModule extends ModuleCommand {

    @Override
    Command.Chatsetting config();

    @Override
    Permission.Command.Chatsetting permission();

    @Override
    Localization.Command.Chatsetting localization(FPlayer fPlayer);

    /**
     * Stores an on/off setting.
     *
     * @param fPlayer the player
     * @param messageType which setting
     * @param value the new state
     */
    void saveSetting(FPlayer fPlayer, String messageType, boolean value);

    /**
     * Stores a text setting, such as the chosen chat or locale.
     *
     * @param fPlayer the player
     * @param settingText which setting
     * @param value the new value
     */
    void saveSetting(FPlayer fPlayer, SettingText settingText, String value);

    /**
     * Returns the menu builder
     *
     * @return the menu builder
     */
    MenuBuilder getMenuBuilder();

    /**
     * The chat a player currently writes to.
     *
     * @param fTarget the player
     * @return the chat name
     */
    String getPlayerChat(FPlayer fTarget);

    /**
     * The item used to draw a checkbox in the menu.
     *
     * @param enabled whether the option is on
     * @return the item id
     */
    String getCheckboxMaterial(boolean enabled);

    /**
     * The title of one menu entry.
     *
     * @param fPlayer the reader
     * @param setting which setting
     * @param enabled whether the option is on
     * @return the title
     */
    String getCheckboxTitle(FPlayer fPlayer, String setting, boolean enabled);

    /**
     * The hint shown under a menu entry.
     *
     * @param fPlayer the reader
     * @param enabled whether the option is on
     * @return the hint
     */
    String getCheckboxLore(FPlayer fPlayer, boolean enabled);

}
