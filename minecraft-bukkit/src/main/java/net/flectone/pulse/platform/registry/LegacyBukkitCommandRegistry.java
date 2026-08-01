package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.processing.mapper.BukkitFPlayerMapper;
import net.flectone.pulse.processing.resolver.ReflectionResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.setting.ManagerSetting;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Singleton
public class LegacyBukkitCommandRegistry implements CommandRegistry {

    private final FileFacade fileFacade;
    private final Plugin plugin;
    private final ReflectionResolver reflectionResolver;
    private final TaskScheduler taskScheduler;
    private final BukkitFPlayerMapper fPlayerMapper;
    private final CommandExceptionHandler commandExceptionHandler;
    private final FLogger fLogger;

    protected LegacyPaperCommandManager<FPlayer> manager;

    @Inject
    public LegacyBukkitCommandRegistry(FileFacade fileFacade,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       ReflectionResolver reflectionResolver,
                                       TaskScheduler taskScheduler,
                                       BukkitFPlayerMapper fPlayerMapper,
                                       FLogger fLogger) {
        this.fileFacade = fileFacade;
        this.plugin = plugin;
        this.fPlayerMapper = fPlayerMapper;
        this.taskScheduler = taskScheduler;
        this.reflectionResolver = reflectionResolver;
        this.commandExceptionHandler = commandExceptionHandler;
        this.fLogger = fLogger;
    }

    @Override
    public void init() {
        this.manager = new LegacyPaperCommandManager<>(plugin, ExecutionCoordinator.<FPlayer>builder().executor(taskScheduler.getExecutorService()).build(), fPlayerMapper);

        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);

        manager.exceptionController().registerHandler(ArgumentParseException.class, commandExceptionHandler::handleArgumentParseException);
        manager.exceptionController().registerHandler(InvalidSyntaxException.class, commandExceptionHandler::handleInvalidSyntaxException);
        manager.exceptionController().registerHandler(NoPermissionException.class, commandExceptionHandler::handleNoPermissionException);
        manager.exceptionController().registerHandler(CommandExecutionException.class, commandExceptionHandler::handleCommandExecutionException);

        unregisterVanillaCommands();
    }

    @Override
    public void registerCommand(Function<CommandManager<FPlayer>, Command.Builder<FPlayer>> builder) {
        Command<FPlayer> command = builder.apply(manager).build();

        // root name
        String commandName = command.rootComponent().name();

        boolean isCloudCommand = manager.commands().stream()
                .anyMatch(fPlayerCommand -> fPlayerCommand.rootComponent().name().equals(commandName));

        boolean needUnregister = plugin.getServer().getPluginCommand(commandName) != null
                || fileFacade.config().internal().unregisterCommandOnReload() && isCloudCommand;

        if (needUnregister) {
            unregisterCommand(commandName);
        } else if (isCloudCommand) {
            return;
        }

        // register new command
        if (reflectionResolver.isPaper()) {
            registerCommand(command);
        } else {
            taskScheduler.runSync(() -> registerCommand(command));
        }
    }

    @Override
    public void unregisterCommand(String name) {
        if (reflectionResolver.isPaper()) {
            deleteRootCommand(name);
        } else {
            taskScheduler.runSync(() -> deleteRootCommand(name));
        }
    }

    @Override
    public void onDisable() {
        if (!fileFacade.config().internal().unregisterCommandOnReload()) return;

        if (reflectionResolver.isPaper()) {
            unregisterCommands();
        } else {
            taskScheduler.runSync(this::unregisterCommands);
        }
    }

    public void deleteRootCommand(String name) {
        manager.deleteRootCommand(name);
    }

    public void registerCommand(Command<FPlayer> command) {
        manager.command(command);
    }

    public void unregisterCommands() {
        manager.commands().stream()
                .map(command -> command.rootComponent().name())
                .toList() // fix concurrent modification
                .forEach(this::unregisterCommand);
    }

    public void unregisterVanillaCommands() {
        // skip deleting commands for default config, because they will be deleted anyway.
        // if user has changed the value, then he knows what he is doing
        if (fileFacade.config().internal().vanillaCommandsToRemove().equals(Set.of("msg", "banlist", "kick", "w", "tell", "me", "pardon", "whitelist", "ban"))) {
            return;
        }

        try {
            Field declaredField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            declaredField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) declaredField.get(Bukkit.getServer());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);

            fileFacade.config().internal().vanillaCommandsToRemove().forEach(commandName -> {
                org.bukkit.command.Command bukkitCommand = knownCommands.remove(commandName);
                if (bukkitCommand != null) {
                    bukkitCommand.unregister(commandMap);
                }

                knownCommands.remove(commandName);
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fLogger.warning("Failed to remove vanilla commands: %s", e.getMessage());
        }
    }

}
