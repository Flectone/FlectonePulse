package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.retrooper.packetevents.PacketEventsServerMod;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.*;
import net.flectone.pulse.platform.FabricDependencyResolver;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricFlectonePulse implements ModInitializer, FlectonePulse {

	private final String MOD_ID = "flectonepulse";

	@Getter
	private static MinecraftServer minecraftServer;

	private FLogger fLogger;
	private Injector injector;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			minecraftServer = server;
			onEnable();
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			onDisable();
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (injector == null) return;
			injector.getInstance(FabricThreadManager.class).tick();
		});
	}


	public void onEnable() {
		Logger logger = LoggerFactory.getLogger(MOD_ID);
		fLogger = new FLogger(null, logRecord -> logger.info(logRecord.getMessage()));

		fLogger.logEnabling();
		FabricDependencyResolver fabricDependencyResolver = new FabricDependencyResolver(MOD_ID, logger);
		fabricDependencyResolver.loadLibraries();
		fabricDependencyResolver.resolveDependencies();

		PacketEventsServerMod.constructApi(MOD_ID).init();

		injector = Guice.createInjector(new FabricInjector(MOD_ID, this, minecraftServer, fabricDependencyResolver, fLogger));

		fLogger.logPluginInfo();

		injector.getInstance(FileManager.class).reload();

		injector.getInstance(ModuleManager.class).reload();

		try {
			injector.getInstance(Database.class).connect();
		} catch (Exception e) {
			fLogger.warning("Failed to connect database");
			fLogger.warning(e);

			throw new RuntimeException(e);
		}

		injector.getInstance(FPlayerManager.class).reload();
		injector.getInstance(ProxyManager.class).reload();

		injector.getInstance(ListenerManager.class).registerDefaultListeners();

		fLogger.logEnabled();
	}

	public void onDisable() {

	}

	@Override
	public void reload() {}
}