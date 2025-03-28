package net.flectone.pulse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class BukkitCommandUtil extends CommandUtil {

    private final FPlayerService fPlayerService;
    private final IntegrationModule integrationModule;

    @Inject
    public BukkitCommandUtil(FPlayerService fPlayerService,
                             IntegrationModule integrationModule) {
        this.fPlayerService = fPlayerService;
        this.integrationModule = integrationModule;
    }

    @Sync
    @Override
    public void unregister(String command) {
        CommandAPI.unregister(command);
        CommandAPIBukkit.unregister(command, true, true);
    }

    @Sync
    @Override
    public void dispatch(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public String getString(int index, Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            return commandArguments.getRaw(index);
        }

        return "";
    }

    @Override
    public String getText(int index, Object arguments) {
        String string = getString(index, arguments);
        if (string.isEmpty()) return "";

        if (string.startsWith("\"") && string.endsWith("\"")
                || string.startsWith("'") && string.endsWith("'")) {
            return string.substring(1, string.length() - 1);
        }

        return string;
    }

    @Override
    public String getFull(Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            return commandArguments.fullInput();
        }

        return "";
    }

    @Override
    public Integer getInteger(int index, Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            return commandArguments.getByClassOrDefault(index, Integer.class, -1);
        }

        return -1;
    }

    @Override
    public Optional<Object> getOptional(int index, Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            return commandArguments.getOptional(index);
        }

        return Optional.empty();
    }

    @Override
    public String getLiteral(int index, Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            String[] strings = commandArguments.fullInput().split(" ");

            if (strings.length <= index+1) return "";

            return strings[index+1];
        }

        return "";
    }

    @Override
    public Boolean getBoolean(int index, Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            return commandArguments.getByClassOrDefault(index, Boolean.class, false);
        }

        return false;
    }

    @Override
    public <T> T getByClassOrDefault(int index, Class<T> clazz, T defaultValue, Object arguments) {
        if (arguments instanceof CommandArguments commandArguments) {
            return commandArguments.getByClassOrDefault(index, clazz, defaultValue);
        }

        return defaultValue;
    }

    public ArgumentSuggestions<CommandSender> argumentFPlayers(boolean offlinePlayers) {
        return ArgumentSuggestions.stringCollectionAsync(info -> CompletableFuture.supplyAsync(() -> {
            if (offlinePlayers) {
                return fPlayerService.findAllFPlayers().stream()
                        .filter(player -> !integrationModule.isVanished(player))
                        .filter(player -> !player.isUnknown())
                        .map(FEntity::getName)
                        .toList();
            }

            return fPlayerService.findOnlineFPlayers().stream()
                    .filter(player -> !integrationModule.isVanished(player))
                    .map(FEntity::getName)
                    .toList();
        }));
    }

    public Argument<Integer> timeArgument(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            String string = info.input();
            try {
                int time = Integer.parseInt(string.substring(0, string.length() - 1));

                string = string.substring(string.length() - 1);
                CommandUtil.TimeType timeType = CommandUtil.TimeType.fromString(string);

                return timeType.convertToRealTime(time);
            } catch (NumberFormatException e) {
                return -1;
            }
        }).includeSuggestions(ArgumentSuggestions.strings(info -> {
            if (!info.currentArg().isEmpty() && isNumeric(info.currentArg())) {
                return Arrays.stream(CommandUtil.TimeType.values())
                        .map(value -> info.currentArg() + value.getFormat())
                        .toArray(String[]::new);
            }

            return new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        }));
    }

    private boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ignored) {}

        return false;
    }
}
