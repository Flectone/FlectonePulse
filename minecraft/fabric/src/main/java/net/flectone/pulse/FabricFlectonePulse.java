package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.mojang.brigadier.tree.CommandNode;
import io.github.retrooper.packetevents.PacketEventsServerMod;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.platform.controller.DialogController;
import net.flectone.pulse.platform.controller.InventoryController;
import net.flectone.pulse.processing.resolver.FabricLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Singleton
public class FabricFlectonePulse implements PreLaunchEntrypoint, ModInitializer, FlectonePulse {

	@Setter private MinecraftServer minecraftServer;

	private FLogger fLogger;
	private Injector injector;

	@Override
	public void onPreLaunch() {
		// configure packetevents api
        System.setProperty("packetevents.nbt.default-max-size", "2097152");
		PacketEventsServerMod.constructApi(BuildConfig.PROJECT_MOD_ID).init();
	}

	@Override
	public void onInitialize() {
		// initialize custom logger
		Logger logger = LoggerFactory.getLogger(BuildConfig.PROJECT_MOD_ID);
		fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()));
        fLogger.logEnabling();

		// set up library resolver for dependency loading
		LibraryResolver libraryResolver = new FabricLibraryResolver(logger);
		libraryResolver.addLibraries();
		libraryResolver.resolveRepositories();
		libraryResolver.loadLibraries();

        // create guice injector for dependency injection
        injector = Guice.createInjector(Stage.PRODUCTION, new FabricInjector(this, libraryResolver, fLogger));

		onEnable();
	}

	@Override
	public void onEnable() {
		if (!isReady()) return;

        // idk why, but this does not work in ListenerRegistry,
        // for some reason 5ms decide whether commands will be deleted normally
        removeDefaultFabricCommands();

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
        PacketEvents.getAPI().init();
    }

    @Override
    public void terminateFailedPacketAdapter() {
        try {
            PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
            if (!packetEventsAPI.isInitialized()) {
                packetEventsAPI.getInjector().uninject();
            }
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Override
    public void terminatePacketAdapter() {
        PacketEvents.getAPI().terminate();
    }

    @Override
    public void closeUIs() {
        // close all open inventories
        injector.getInstance(InventoryController.class).closeAll();
        injector.getInstance(DialogController.class).closeAll();
    }

    private void removeDefaultFabricCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<ServerCommandSource> root = dispatcher.getRoot();

            for (String command : injector.getInstance(FileFacade.class).config().command().disabledFabric()) {
                root.getChildren().removeIf(node -> node.getName().equals(command));
            }
        });
    }
}