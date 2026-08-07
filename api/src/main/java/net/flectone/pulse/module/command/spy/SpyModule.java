package net.flectone.pulse.module.command.spy;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The /spy command, which mirrors other players' chat, commands and book or sign edits to a moderator.
 * @author TheFaser
 */
public interface SpyModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Spy config();

    @Override
    Permission.Command.Spy permission();

    @Override
    Localization.Command.Spy localization(FPlayer fPlayer);

    /**
     * Reports an anvil rename to the watching moderators.
     *
     * @param player who renamed the item
     * @param itemName the new name, may be null
     */
    void checkAnvil(@NonNull UUID player, @Nullable String itemName);

    /**
     * Reports a sign edit to the watching moderators.
     *
     * @param player who edited the sign
     * @param lines the lines written, may be null
     */
    void checkSign(@NonNull UUID player, @Nullable String[] lines);

    /**
     * Reports a book edit to the watching moderators.
     *
     * @param player who wrote the book
     * @param title the book title, may be null
     * @param pages the pages written, may be null
     */
    void checkBook(@NonNull UUID player, @Nullable String title, @Nullable List<String> pages);

    /**
     * Reports a command to the watching moderators.
     *
     * @param player who ran it
     * @param command the command line, may be null
     */
    void checkCommand(@NonNull UUID player, @Nullable String command);

    /**
     * Reports a chat message to the moderators who did not already receive it.
     *
     * @param player who wrote it
     * @param message the text, may be null
     * @param receivers who already saw it
     */
    void checkChat(@NonNull UUID player, @Nullable String message, @NonNull List<UUID> receivers);

    /**
     * Reports a chat message, naming the chat it was written in.
     *
     * @param fPlayer who wrote it
     * @param chat the chat name
     * @param message the text
     * @param receivers who already saw it
     */
    void checkChat(@NonNull FPlayer fPlayer, @NonNull String chat, @NonNull String message, @NonNull Set<FPlayer> receivers);

    /**
     * Sends one spy report to every watching moderator.
     *
     * @param fPlayer who was spied on
     * @param action what they did
     * @param message the text they entered
     */
    void spy(@NonNull FPlayer fPlayer, @NonNull String action, @NonNull String message);

    /**
     * Sends one spy report, skipping moderators who already saw the original.
     *
     * @param fPlayer who was spied on
     * @param action what they did
     * @param message the text they entered
     * @param receivers who already saw it
     */
    void spy(@NonNull FPlayer fPlayer, @NonNull String action, @NonNull String message, @NonNull Set<FPlayer> receivers);

    /**
     * Which moderators should receive a report.
     *
     * @param fPlayer who was spied on
     * @param receivers who already saw the original
     * @return the receiver filter
     */
    Predicate<FPlayer> createFilter(FPlayer fPlayer, Set<FPlayer> receivers);

}
