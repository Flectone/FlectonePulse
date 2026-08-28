package net.flectone.pulse.module.command.chatcolor;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;

import java.util.UUID;

/**
 * The /chatcolor command, which lets a player pick the colors their own messages are drawn in.
 * @author TheFaser
 */
public interface ChatcolorModule extends ModuleCommand {

    @Override
    Command.Chatcolor config();

    @Override
    Permission.Command.Chatcolor permission();

    @Override
    Localization.Command.Chatcolor localization(FPlayer fPlayer);

    /**
     * The color settings this command edits.
     *
     * @return the color settings
     */
    Message.Format.FColor fColorConfig();

    /**
     * Confirms the new colors to the player.
     *
     * @param fPlayer the player whose colors changed
     * @param metadataUUID the shared message id
     */
    void sendMessageWithUpdatedColors(FPlayer fPlayer, UUID metadataUUID);

}
