package net.flectone.pulse.module.message.book;

import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.util.constant.ModuleName;

import java.util.Optional;

/**
 * Formats the text a player writes into a book.
 * @author TheFaser
 */
public interface BookModule extends ModuleSimple {

    @Override
    ModuleName name();

    @Override
    Message.Book config();

    @Override
    Permission.Message.Book permission();

    /**
     * Formats a book on servers whose api carries plain strings rather than components.
     *
     * @param fPlayer the writer
     * @param string the raw text
     * @return the formatted text, or empty if the module is off for this player
     */
    Optional<String> legacyFormat(FPlayer fPlayer, String string);

    /**
     * Formats a book on servers whose api carries components rather than plain strings.
     *
     * @param fPlayer the writer
     * @param string the serialized component
     * @return the formatted text, or empty if the module is off for this player
     */
    Optional<String> paperFormat(FPlayer fPlayer, String string);

}
