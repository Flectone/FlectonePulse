package net.flectone.pulse;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.retrooper.packetevents.PacketEventsServerMod;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.flectone.pulse.controller.InventoryController;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.Module;
import net.flectone.pulse.module.integration.discord.DiscordModule;
import net.flectone.pulse.module.integration.telegram.TelegramModule;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.registry.CommandRegistry;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.resolver.FabricLibraryResolver;
import net.flectone.pulse.resolver.LibraryResolver;
import net.flectone.pulse.scheduler.FabricTaskScheduler;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.sender.ProxySender;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.MetricsService;
import net.flectone.pulse.service.ModerationService;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricFlectonePulse implements ModInitializer, FlectonePulse {

	private final String MOD_ID = "flectonepulse";

	private MinecraftServer minecraftServer;
	private LibraryResolver libraryResolver;

	@Setter
	private boolean disableSilently = false;

	private FLogger fLogger;
	private Injector injector;

	@Override
	public void onInitialize() {
		Logger logger = LoggerFactory.getLogger(MOD_ID);
		fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()));

		fLogger.logEnabling();
		libraryResolver = new FabricLibraryResolver(MOD_ID, logger);
		libraryResolver.addLibraries();
		libraryResolver.resolveRepositories();
		libraryResolver.loadLibraries();

		PacketEventsServerMod.constructApi(MOD_ID).init();

		registerListeners();
	}

	public void onEnable() {
		injector = Guice.createInjector(new FabricInjector(MOD_ID, this, minecraftServer, libraryResolver, fLogger));

		fLogger.logPluginInfo();

		if (injector == null || disableSilently) {
			fLogger.warning("FAILED TO ENABLE");
			fLogger.warning("Report a problem on github https://github.com/Flectone/FlectonePulse/issues");
			fLogger.warning("or in discord https://discord.com/channels/861147957365964810/1271850075064369152");
			return;
		}

		FileManager fileManager = injector.getInstance(FileManager.class);

		fLogger.reload(fileManager.getConfig().getLogFilter());

		injector.getInstance(Module.class).reloadWithChildren();

		try {
			injector.getInstance(Database.class).connect();
		} catch (Exception e) {
			e.printStackTrace();
			fLogger.warning("Failed to connect database");
			fLogger.warning(e);
			return;
		}

		injector.getInstance(FPlayerService.class).reload();
		injector.getInstance(ProxySender.class).reload();

		if (fileManager.getConfig().isMetrics()) {
			injector.getInstance(MetricsService.class).reload();
		}

		injector.getInstance(ListenerRegistry.class).registerDefaultListeners();

		fLogger.logEnabled();
	}

	public void onDisable() {
		if (injector == null || disableSilently) return;

		fLogger.logDisabling();

		injector.getInstance(InventoryController.class).closeAll();

		FPlayerService fPlayerService = injector.getInstance(FPlayerService.class);

		fPlayerService.getFPlayers().forEach(fPlayer -> {
			fPlayer.setOnline(false);
			fPlayerService.saveOrUpdateFPlayer(fPlayer);
		});

		fPlayerService.clear();

//		injector.getInstance(ScoreboardLibrary.class).close();
		injector.getInstance(Database.class).disconnect();
//		injector.getInstance(ObjectiveManager.class).close();
//		injector.getInstance(TeamManager.class).close();
		injector.getInstance(ListenerRegistry.class).unregisterAll();
		PacketEvents.getAPI().terminate();

		injector.getInstance(ProxySender.class).disable();

		injector.getInstance(DiscordModule.class).disconnect();
		injector.getInstance(TwitchModule.class).disconnect();
		injector.getInstance(TelegramModule.class).disconnect();
		injector.getInstance(TaskScheduler.class).reload();

		fLogger.logDisabled();
	}

	@Override
	public void reload() {
		if (injector == null) return;

		fLogger.logReloading();

		injector.getInstance(InventoryController.class).closeAll();

		injector.getInstance(CommandRegistry.class).reload();
		injector.getInstance(ListenerRegistry.class).reload();
		injector.getInstance(TaskScheduler.class).reload();

		FileManager fileManager = injector.getInstance(FileManager.class);
		fileManager.reload();

		fLogger.reload(fileManager.getConfig().getLogFilter());

		try {
			injector.getInstance(Database.class).disconnect();
			injector.getInstance(Database.class).connect();

		} catch (Exception e) {
			fLogger.warning("Failed to connect database");

			throw new RuntimeException(e);
		}

		injector.getInstance(ProxySender.class).reload();
		injector.getInstance(FPlayerService.class).reload();
		injector.getInstance(ModerationService.class).reload();
		injector.getInstance(Module.class).reloadWithChildren();

		if (fileManager.getConfig().isMetrics()) {
			injector.getInstance(MetricsService.class).reload();
		}

		fLogger.logReloaded();
	}

	private void registerListeners() {
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (injector == null) return;
			injector.getInstance(FabricTaskScheduler.class).onTick();
		});

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			minecraftServer = server;
			onEnable();
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			onDisable();
		});
	}
}