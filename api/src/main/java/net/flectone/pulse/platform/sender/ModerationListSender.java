package net.flectone.pulse.platform.sender;

import net.flectone.pulse.config.setting.ModerationListLocalizationSetting;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Moderation;
import net.flectone.pulse.module.ModuleCommand;
import org.incendo.cloud.context.CommandContext;

import java.util.Optional;
import java.util.function.Function;

/**
 * Renders the paged output of the punishment list commands, such as banlist and mutelist.
 * @author TheFaser
 */
public interface ModerationListSender {

    /**
     * Sends one page of punishments to the player who asked for it.
     *
     * @param module the command that was run
     * @param fPlayer who ran it
     * @param localization picks the wording for the reader
     * @param commandContext the parsed arguments
     * @param type the punishment type to list
     * @param firstArgumentIndex where this command's own arguments start
     * @param perPage how many entries fit on a page
     * @param nextPageCommand the command run by the next-page button
     * @param unmoderationCommand the command run by the revoke button
     */
    void send(ModuleCommand module, FPlayer fPlayer, Function<FPlayer, ModerationListLocalizationSetting> localization, CommandContext<FPlayer> commandContext, Moderation.Type type, int firstArgumentIndex, int perPage, String nextPageCommand, Function<FPlayer, String> unmoderationCommand);

    /**
     * Reads the optional player and page number off the command line.
     *
     * @param command the command that was run
     * @param commandContext the parsed arguments
     * @param startIndex where this command's own arguments start
     * @return the target and page, or empty if the arguments do not make sense
     */
    Optional<ListArgument> getListArgument(ModuleCommand command, CommandContext<FPlayer> commandContext, int startIndex);

    /**
     * The arguments a list command was called with.
     *
     * @param target the player to list punishments for, or null for everyone
     * @param page the requested page, starting at 1
     */
    record ListArgument(
            FPlayer target,
            int page
    ){}


}
