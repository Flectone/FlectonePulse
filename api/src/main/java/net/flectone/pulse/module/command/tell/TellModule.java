package net.flectone.pulse.module.command.tell;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

/**
 * The /tell command, which sends a private message.
 * @author TheFaser
 */
public interface TellModule extends ModuleCommand {

    @Override
    Command.Tell config();

    @Override
    Permission.Command.Tell permission();

    @Override
    Localization.Command.Tell localization(FPlayer fPlayer);

    /**
     * Sends a private message.
     *
     * @param fPlayer the sender
     * @param playerName the addressee
     * @param message the text
     */
    void send(FPlayer fPlayer, String playerName, String message);

    void send(FEntity sender,
              FPlayer target,
              FPlayer fReceiver,
              Function<Localization.Command.Tell, String> format,
              String string,
              UUID metadataUUID);

    /**
     * Remembers who a player last wrote to, so /reply knows where to go.
     *
     * @param player the sender
     * @param receiver the addressee
     */
    void saveReceiver(UUID player, String receiver);

    /**
     * Forgets who a player last wrote to.
     *
     * @param fPlayer the sender
     */
    void removeReceiver(FPlayer fPlayer);

    @Nullable String getReceiverFor(FPlayer fPlayer);

}
