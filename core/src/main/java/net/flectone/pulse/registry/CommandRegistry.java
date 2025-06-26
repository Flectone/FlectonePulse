package net.flectone.pulse.registry;

import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.Pair;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public abstract class CommandRegistry {

    private final Set<String> registeredCommands = new HashSet<>();

    private final CommandParserRegistry parsers;
    private final PermissionChecker permissionChecker;

    public CommandRegistry(CommandParserRegistry parsers,
                           PermissionChecker permissionChecker) {
        this.parsers = parsers;
        this.permissionChecker = permissionChecker;
    }

    public void reload() {
        registeredCommands.forEach(this::unregisterCommand);
        registeredCommands.clear();
    }

    public void addCommand(String command) {
        registeredCommands.add(command);
    }

    public boolean containsCommand(String command) {
        return registeredCommands.contains(command);
    }

    public abstract void registerCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder);

    public abstract void unregisterCommand(String name);

    public @NonNull ParserDescriptor<FPlayer, String> playerParser(boolean offlinePlayers) {
        return parsers.playerParser(offlinePlayers);
    }

    public @NonNull ParserDescriptor<FPlayer, String> offlinePlayerParser() {
        return parsers.offlinePlayerParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> playerParser() {
        return parsers.playerParser();
    }

    public @NonNull ParserDescriptor<FPlayer, Boolean> booleanParser() {
        return parsers.booleanParser();
    }

    public @NonNull ParserDescriptor<FPlayer, Integer> integerParser() {
        return parsers.integerParser();
    }

    public @NonNull ParserDescriptor<FPlayer, Integer> integerParser(int min, int max) {
        return parsers.integerParser(min, max);
    }

    public @NonNull ParserDescriptor<FPlayer, Pair<Long, String>> durationReasonParser() {
        return parsers.durationReasonParser();
    }

    public @NonNull ParserDescriptor<FPlayer, Duration> durationParser() {
        return parsers.durationParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> nativeSingleMessageParser() {
        return parsers.singleStringParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> nativeMessageParser() {
        return parsers.greedyStringParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> messageParser() {
        return parsers.messageParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> singleMessageParser() {
        return parsers.singleMessageParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> bannedParser() {
        return parsers.bannedParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> mutedParser() {
        return parsers.mutedParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> warnedParser() {
        return parsers.warnedParser();
    }

    public @NonNull ParserDescriptor<FPlayer, String> colorParser() {
        return parsers.colorParser();
    }

    public @NonNull BlockingSuggestionProvider<FPlayer> playerSuggestionPermission(Permission.IPermission permission) {
        return (context, input) -> {
            if (!permissionChecker.check(context.sender(), permission)) return Collections.emptyList();

            return playerParser().parser().suggestionProvider().suggestionsFuture(context, input).join();
        };
    }
}
