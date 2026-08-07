package net.flectone.pulse.module.command.whitelist;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The /whitelist command, which controls who may join.
 * @author TheFaser
 */
public interface WhitelistModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    Localization.Command.Whitelist localization(FPlayer fPlayer);

    @Override
    ModuleName name();

    @Override
    Command.Whitelist config();

    @Override
    Permission.Command.Whitelist permission();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Nullable Moderation add(@NonNull FPlayer fModerator, @NonNull FPlayer fTarget, long time, @Nullable String reason);

    /**
     * Whether the whitelist is enforced.
     *
     * @return true if only whitelisted players may join
     */
    boolean isTurnedOn();

    /**
     * Whether a player is on the whitelist.
     *
     * @param fPlayer the player
     * @return true if they may join
     */
    boolean isWhitelisted(FPlayer fPlayer);

    /**
     * Disconnects everyone who is no longer whitelisted.
     *
     * @param moderation the entry that caused the change
     */
    void kickOnlinePlayers(@NonNull Moderation moderation);

    /**
     * Disconnects one player removed from the whitelist.
     *
     * @param fModerator the moderator
     * @param fTarget the player to remove
     */
    void kickPlayer(FEntity fModerator, FPlayer fTarget);

    /**
     * What was done to the whitelist.
     */
    enum Action {
        ADD,
        LIST,
        OFF,
        ON,
        IMPORT,
        REMOVE
    }

}
