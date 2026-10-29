package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.mapper.BukkitFPlayerMapper;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.BukkitRegistrationHandler;
import net.flectone.pulse.platform.registry.cloud.PaperAsyncTabCompleteListener;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

@Singleton
public class LegacyBukkitCommandRegistry extends CommandRegistryImpl<CommandSender> {

    protected final Plugin plugin;
    protected final ReflectionResolver reflectionResolver;
    protected final TaskScheduler taskScheduler;

    private final FileFacade fileFacade;

    private CommandMap commandMap;
    private Map<String, org.bukkit.command.Command> knownCommands;
    private BukkitRegistrationHandler bukkitRegistrationHandler;

    @Inject
    public LegacyBukkitCommandRegistry(FileFacade fileFacade,
                                       PermissionChecker permissionChecker,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       ReflectionResolver reflectionResolver,
                                       TaskScheduler taskScheduler,
                                       BukkitFPlayerMapper fPlayerMapper) {
        super(permissionChecker, commandExceptionHandler, taskScheduler, fPlayerMapper);

        this.fileFacade = fileFacade;
        this.plugin = plugin;
        this.reflectionResolver = reflectionResolver;
        this.taskScheduler = taskScheduler;
    }

    @Override
    protected PlatformRegistrationHandler<CommandSender> createRegistrationHandler() {
        this.bukkitRegistrationHandler = new BukkitRegistrationHandler(plugin, commandMap, knownCommands, taskScheduler, reflectionResolver);
        return bukkitRegistrationHandler;
    }

    @Override
    public void init() {
        if (this.manager != null) return;

        loadCommandMap();

        super.init();

        if (reflectionResolver.hasClass("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent")
                && !reflectionResolver.hasClass("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent")) {
            PaperAsyncTabCompleteListener paperAsyncTabCompleteListener = new PaperAsyncTabCompleteListener(plugin, manager, bukkitRegistrationHandler::isRegisteredCommand);
            Bukkit.getPluginManager().registerEvents(paperAsyncTabCompleteListener, plugin);
        }

        unregisterVanillaCommands();
    }

    @Override
    public void unregisterCommand(String name) {
        manager.deleteRootCommand(name);
    }

    @Override
    public void onDisable() {
        if (!fileFacade.config().internal().unregisterCommandOnReload()) return;

        manager.commands().stream()
                .map(command -> command.rootComponent().name())
                .distinct()
                .toList() // fix concurrent modification
                .forEach(this::unregisterCommand);
    }

    public void unregisterVanillaCommands() {
        // skip deleting commands for default config, because they will be deleted anyway.
        // if user has changed the value, then he knows what he is doing
        if (fileFacade.config().internal().vanillaCommandsToRemove().equals(Set.of("msg", "banlist", "kick", "w", "tell", "me", "pardon", "whitelist", "ban"))) {
            return;
        }

        fileFacade.config().internal().vanillaCommandsToRemove().forEach(commandName -> {
            org.bukkit.command.Command bukkitCommand = knownCommands.remove(commandName);
            if (bukkitCommand != null) {
                bukkitCommand.unregister(commandMap);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadCommandMap() {
        try {
            Method getCommandMap = Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);
            this.commandMap = (CommandMap) getCommandMap.invoke(Bukkit.getServer());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            this.knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to get bukkit command map", e);
        }
    }

}
