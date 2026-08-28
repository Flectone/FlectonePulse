package net.flectone.pulse.module.message.greeting;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;

/**
 * Greets a player when they join.
 * @author TheFaser
 */
public interface GreetingModule extends ModuleLocalization {

    @Override
    Message.Greeting config();

    @Override
    Permission.Message.Greeting permission();

    @Override
    Localization.Message.Greeting localization(FPlayer fPlayer);

    /**
     * Greets a player.
     *
     * @param fPlayer the joining player
     */
    void send(FPlayer fPlayer);

}
