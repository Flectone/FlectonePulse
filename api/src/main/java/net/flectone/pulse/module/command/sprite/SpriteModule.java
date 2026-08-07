package net.flectone.pulse.module.command.sprite;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

/**
 * The /sprite command, which lists the sprites a player may insert into chat.
 * @author TheFaser
 */
public interface SpriteModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Sprite config();

    @Override
    Permission.Command.Sprite permission();

    @Override
    Localization.Command.Sprite localization(FPlayer fPlayer);

}
