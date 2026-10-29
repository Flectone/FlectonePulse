package net.flectone.pulse.platform.registry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.mapper.BukkitFPlayerMapper;
import net.flectone.pulse.platform.handler.CommandExceptionHandler;
import net.flectone.pulse.platform.registry.cloud.PaperBrigadierListener;
import net.flectone.pulse.platform.registry.cloud.PaperRegistrationHandler;
import net.flectone.pulse.platform.registry.cloud.PlatformRegistrationHandler;
import net.flectone.pulse.resolver.ReflectionResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@Singleton
public class ModernBukkitCommandRegistry extends LegacyBukkitCommandRegistry implements BrigadierCommandRegistry {

    private PaperRegistrationHandler paperRegistrationHandler;

    @Inject
    public ModernBukkitCommandRegistry(FileFacade fileFacade,
                                       PermissionChecker permissionChecker,
                                       CommandExceptionHandler commandExceptionHandler,
                                       Plugin plugin,
                                       ReflectionResolver reflectionResolver,
                                       TaskScheduler taskScheduler,
                                       BukkitFPlayerMapper fPlayerMapper) {
        super(fileFacade, permissionChecker, commandExceptionHandler, plugin, reflectionResolver, taskScheduler, fPlayerMapper);
    }

    @Override
    protected PlatformRegistrationHandler<CommandSender> createRegistrationHandler() {
        if (reflectionResolver.hasClass("io.papermc.paper.command.brigadier.CommandSourceStack")) {
            this.paperRegistrationHandler = new PaperRegistrationHandler(plugin);
            return paperRegistrationHandler;
        }

        return super.createRegistrationHandler();
    }

    @Override
    public void init() {
        if (this.manager != null) return;

        super.init();

        if (paperRegistrationHandler != null) {
            setupBrigadierManager(paperRegistrationHandler.getBrigadierManager());
            return;
        }

        if (reflectionResolver.hasClass("com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent")) {
            PaperBrigadierListener paperBrigadierListener = new PaperBrigadierListener(plugin, manager);
            Bukkit.getPluginManager().registerEvents(paperBrigadierListener, plugin);

            setupBrigadierManager(paperBrigadierListener.getBrigadierManager());
        }
    }

}
