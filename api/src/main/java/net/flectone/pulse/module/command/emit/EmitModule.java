package net.flectone.pulse.module.command.emit;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /emit command, which sends a raw formatted message to a chosen destination.
 * @author TheFaser
 */
public interface EmitModule extends ModuleCommand {

    @Override
    Command.Emit config();

    @Override
    Permission.Command.Emit permission();

    @Override
    Localization.Command.Emit localization(FPlayer fPlayer);

    /**
     * Reads the destination out of the raw argument and returns what is left as the message.
     *
     * @param destination the destination that was parsed
     * @param string the raw argument
     * @return the message text
     */
    String parseMessage(Destination destination, String string);

}
