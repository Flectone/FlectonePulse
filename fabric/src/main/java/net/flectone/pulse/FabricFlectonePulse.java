package net.flectone.pulse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.PacketEventsServerMod;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.flectone.pulse.exception.ReloadException;
import net.flectone.pulse.processing.resolver.FabricLibraryResolver;
import net.flectone.pulse.processing.resolver.LibraryResolver;
import net.flectone.pulse.util.logging.FLogger;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Singleton
public class FabricFlectonePulse implements PreLaunchEntrypoint, ModInitializer, FlectonePulse {

	@Setter private MinecraftServer minecraftServer;

	private LibraryResolver libraryResolver;
	private FLogger fLogger;
	private Injector injector;

	@Override
	public void onPreLaunch() {
		// configure packetevents api
		PacketEventsServerMod.constructApi(BuildConfig.PROJECT_MOD_ID).init();
	}

	@Override
	public void onInitialize() {
		// initialize custom logger
		Logger logger = LoggerFactory.getLogger(BuildConfig.PROJECT_MOD_ID);
		fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()));
		fLogger.enableFilter();
		fLogger.logEnabling();

		// set up library resolver for dependency loading
		libraryResolver = new FabricLibraryResolver(logger);
		libraryResolver.addLibraries();
		libraryResolver.resolveRepositories();
		libraryResolver.loadLibraries();

		onEnable();
	}

	@Override
	public void onEnable() {
		try {
			// create guice injector for dependency injection
			injector = Guice.createInjector(new FabricInjector(this, libraryResolver, fLogger));
		} catch (RuntimeException e) {
			fLogger.warning("FAILED TO ENABLE");
			fLogger.warning(e);
			e.printStackTrace();
			return;
		}

		injector.getInstance(FlectonePulseAPI.class).onEnable();
	}

	@Override
	public void onDisable() {
		if (injector == null) return;

		injector.getInstance(FlectonePulseAPI.class).onDisable();
	}

	@Override
	public void reload() throws ReloadException {
		if (injector == null) return;

		injector.getInstance(FlectonePulseAPI.class).reload();
	}
}