package net.flectone.pulse.module.command.ban;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;

/**
 * The /ban command.
 * @author TheFaser
 */
public interface BanModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Ban config();

    @Override
    Permission.Command.Ban permission();

    @Override
    Localization.Command.Ban localization(FPlayer fPlayer);

    /**
     * Bans a player and announces it.
     *
     * @param fPlayer the moderator
     * @param target the player to ban
     * @param time when the ban expires in milliseconds, or -1 for permanent
     * @param reason the stated reason
     */
    void ban(FPlayer fPlayer, String target, long time, String reason);

    /**
     * Disconnects a player who has just been banned.
     *
     * @param ban the ban that was issued
     */
    void kick(@NonNull Moderation ban);

}
