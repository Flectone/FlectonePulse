package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.retrooper.packetevents.PacketEventsServerMod;
import net.fabricmc.api.ModInitializer;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.logger.FLogger;
import net.flectone.pulse.manager.*;
import net.flectone.pulse.module.player.message.bubble.BubbleManager;
import net.flectone.pulse.platform.FabricDependency;
import net.flectone.pulse.platform.FabricSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlectonePulseFabric implements ModInitializer, FlectonePulse {

	private final String MOD_ID = "flectonepulse";

	private FLogger fLogger;
	private FabricDependency dependencyResolver;
	private Injector injector;

	@Override
	public void onInitialize() {
		Logger logger = LoggerFactory.getLogger(MOD_ID);
		fLogger = new FLogger(null, logRecord -> logger.info(logRecord.getMessage()), logger::info, logger::warn);

		fLogger.logEnabling();
		dependencyResolver = new FabricDependency(MOD_ID, logger);
		dependencyResolver.loadLibraries();
		dependencyResolver.resolveDependencies();

		PacketEventsServerMod.constructApi(MOD_ID).init();

		injector = Guice.createInjector(new FabricInjector(MOD_ID, this, fLogger));

		fLogger.logPluginInfo();

		injector.getInstance(FabricSender.class).init();
		injector.getInstance(FileManager.class).reload();

		try {
			injector.getInstance(Database.class).connect();
		} catch (Exception e) {
			fLogger.warning("Failed to connect database");
			fLogger.warning(e);

			throw new RuntimeException(e);
		}

		fLogger.logEnabled();
	}

	@Override
	public void reload() {
		fLogger.logReloading();

		injector.getInstance(InventoryManager.class).closeAll();

		injector.getInstance(ThreadManager.class).reload();
		injector.getInstance(ListenerManager.class).reload();
		injector.getInstance(ThreadManager.class).reload();
		injector.getInstance(BubbleManager.class).reload();

		FileManager fileManager = injector.getInstance(FileManager.class);
		fileManager.reload();

		fLogger.reload(fileManager.getConfig().getPlugin().getLogFilter());

		try {
			injector.getInstance(Database.class).disconnect();
			injector.getInstance(Database.class).connect();

		} catch (Exception e) {
			fLogger.warning("Failed to connect database");
			fLogger.warning(e);

			throw new RuntimeException();
		}

		injector.getInstance(ProxyManager.class).reload();
		injector.getInstance(FPlayerManager.class).reload();
		injector.getInstance(ModuleManager.class).reload();

		fLogger.logReloaded();
	}
}