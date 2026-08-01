package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.mojang.brigadier.tree.CommandNode;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.platform.controller.MinecraftDialogController;
import net.flectone.pulse.platform.controller.MinecraftInventoryController;
import net.flectone.pulse.processing.resolver.FabricLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Singleton
public class FabricFlectonePulse implements DedicatedServerModInitializer, FlectonePulse {

    @Setter private MinecraftServer minecraftServer;

    private FLogger fLogger;
    private Injector injector;

    @Override
    public void onInitializeServer() {
        // initialize custom logger
        Logger logger = LoggerFactory.getLogger(BuildConfig.PROJECT_MOD_ID);
        fLogger = new FLogger(
                logRecord -> logger.info(logRecord.getMessage()),
                () -> injector == null ? null : injector.getInstance(FileFacade.class)
        );
        fLogger.logEnabling();

        // set up library resolver for dependency loading
        LibraryResolver libraryResolver = new FabricLibraryResolver(logger);
        libraryResolver.addLibraries();
        libraryResolver.resolveRepositories();
        libraryResolver.loadLibraries();

        WrapperPacketEvents.load();

        try {
            // create guice injector for dependency injection
            injector = Guice.createInjector(Stage.PRODUCTION, new FabricInjector(this, libraryResolver, fLogger));
        } catch (Exception e) {
            throwInitException(e);
        }

        // we need to call enable right now, because the commands must be registered before server is fully started
        onEnable();
    }

    @Override
    public void onEnable() {
        if (!isReady()) return;

        removeDefaultFabricCommands();

        // get scheduler
        TaskScheduler taskScheduler = get(TaskScheduler.class);

        // create executor
        taskScheduler.start();

        // update tick
        ServerTickEvents.START_SERVER_TICK.register(_ -> taskScheduler.onTick());

        injector.getInstance(FlectonePulseAPI.class).onEnable();
    }

    @Override
    public void onDisable() {
        if (!isReady()) {
            terminateFailedPacketAdapter();
            return;
        }

        get(FlectonePulseAPI.class).onDisable();
    }

    @Override
    public void reload() throws ReloadException {
        if (!isReady()) return;

        get(FlectonePulseAPI.class).reload();
    }

    @Override
    public void initPacketAdapter() {
        WrapperPacketEvents.init();
    }

    @Override
    public void terminateFailedPacketAdapter() {
        WrapperPacketEvents.terminateFailed();
    }

    @Override
    public void terminatePacketAdapter() {
        WrapperPacketEvents.terminate();
    }

    @Override
    public void closeUIs() {
        // close all open inventories
        injector.getInstance(MinecraftInventoryController.class).closeAll();
        injector.getInstance(MinecraftDialogController.class).closeAll();
    }

    private void removeDefaultFabricCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            CommandNode<CommandSourceStack> root = dispatcher.getRoot();

            for (String command : injector.getInstance(FileFacade.class).config().internal().vanillaCommandsToRemove()) {
                root.getChildren().removeIf(node -> node.getName().equals(command));
            }
        });
    }
}