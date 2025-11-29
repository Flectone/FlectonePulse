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
        FlectonePulseAPI.configurePacketEvents();
		PacketEventsServerMod.constructApi(BuildConfig.PROJECT_MOD_ID).init();
	}

	@Override
	public void onInitialize() {
		// initialize custom logger
		Logger logger = LoggerFactory.getLogger(BuildConfig.PROJECT_MOD_ID);
		fLogger = new FLogger(logRecord -> logger.info(logRecord.getMessage()));
        fLogger.logEnabling();

		// set up library resolver for dependency loading
		libraryResolver = new FabricLibraryResolver(logger);
		libraryResolver.addLibraries();
		libraryResolver.resolveRepositories();
		libraryResolver.loadLibraries();

        // create guice injector for dependency injection
        injector = Guice.createInjector(new FabricInjector(this, libraryResolver, fLogger));

		onEnable();
	}

	@Override
	public <T> T get(Class<T> type) {
		if (!isReady()) {
			throw new IllegalStateException("FlectonePulse not initialized yet");
		}

		return injector.getInstance(type);
	}

	@Override
	public boolean isReady() {
		return injector != null;
	}

	@Override
	public void onEnable() {
		if (!isReady()) return;

		injector.getInstance(FlectonePulseAPI.class).onEnable();
	}

	@Override
	public void onDisable() {
		if (!isReady()) return;

		injector.getInstance(FlectonePulseAPI.class).onDisable();
	}

	@Override
	public void reload() throws ReloadException {
		if (!isReady()) return;

		injector.getInstance(FlectonePulseAPI.class).reload();
	}
}